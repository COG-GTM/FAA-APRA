/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 * 
 * APRA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package gov.faa.ait.apra.api;

import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_400;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_404;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_500;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.RESPONSE_200;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.bootstrap.ErrorCodes;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/ifr/oceanic")
@Tag(name = "Oceanic Route Charts", description = "Oceanic Route chart download and edition information")
/** 
 * This class is used to retrieve the Oceanic Route charts
 * @author FAA
 *
 */
public class OceanicRouteCharts extends BaseService {
	private static final Logger logger = LoggerFactory.getLogger(OceanicRouteCharts.class);
	private static final String NARC = "NARC";
	private static final String PORC = "PORC";
	private static final String WATRS = "WATRS";
	private static final String ERROR = " Error response being generated and returned.";

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Oceanic Route Chart download link",
		description = "Get Oceanic Route Chart download link by edition, format, and geoname. TIFF formatted files are geo-referenced while PDF format is not geo-referenced."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getOceanicRouteChart(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt,
			@Parameter(description = "A geographic area for which the chart is requested", required = true, schema = @Schema(allowableValues = {"NARC", "PORC", "WATRS"}))
			@RequestParam("geoname") String geo) {

		logger.info("Received call to retrieve current Oceanic Route Chart release for '{}', '{}', '{}'", ed, fmt, geo);

		setEdition(ed != null ? ed : CURRENT);
		setGeoname(geo != null ? geo : PORC);
		setFormat(fmt != null ? fmt : PDF);

		if (!verifyEdition()) {
			logger.error("Expected edition 'next' and received '{}'{}", ed, ERROR);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		if (!verifyFormat()) {
			logger.error("Expected format of 'tiff' or 'pdf'. Received format '{}'{}", fmt, ERROR);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		if (!verifyGeoname()) {
			logger.error("Expected geographic area of NARC, PORC, or WATRS. Received geoname of '{}'{}", geo, ERROR);
			return ResponseEntity.status(400).body(getErrorResponse(400, "Expected geographic area of NARC, PORC, or WATRS. Received geoname of " + geo));
		}

		ChartCycleElementsJson cycle = initParameters();

		ProductSet ps = buildResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Oceanic Route Chart edition information",
		description = "Get Oceanic Route Chart edition information by edition type. All oceanic charts are released on a regular 56 day cycle."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getOceanicRouteEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		setEdition(ed != null ? ed : CURRENT);
		setGeoname("ALL");
		setFormat("PDF");
		ChartCycleElementsJson cycle = initParameters();
		ProductSet ps = getEditionResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
    
    
    // http://aeronav.faa.gov/enroute/05-26-2016/narc_tif.zip
    // http://aeronav.faa.gov/enroute/05-26-2016/narc_pdf.zip
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
    	ObjectFactory of = new ObjectFactory();
    	ProductSet response = initPositiveResponse();
    	
    	ProductSet.Edition ed = initEdition(cycle);
    	Edition.Product product = of.createProductSetEditionProduct();
    	product.setProductName(ProductCodeList.IFR_OCEANIC);
    	
    	StringBuilder path = new StringBuilder(Config.getAeronavHost()).append("/enroute")
    			.append("/").append(formatDate(cycle.getChart_effective_date(), "MM-dd-yyyy"));
    	StringBuilder fileName = new StringBuilder(getGeoname().toLowerCase());
    	
    	if (TIFF.equalsIgnoreCase(getFormat())) {
    		fileName.append("_tif.zip");
    	}
    	else {
    		fileName.append("_pdf.zip");
    	}
    		
    	product.setChartName(fileName.toString());
    	path = path.append("/").append(fileName);
    	try {
    		URL url = new URL(path.toString());
    		verifyURL(url);
        	product.setUrl(path.toString());

        }
    	catch (Exception exurl) {
    		logger.error("Unable to verify the download url "+path.toString(), exurl);
    		product.setUrl("");
        	response.getStatus().setCode(404);
        	response.getStatus().setMessage(ErrorCodes.ERROR_404);
        }

    	ed.setProduct(product);
    	response.getEdition().add(ed);
    	
    	return response;
	}
	
    /**
     * Get the edition information response using the specified chart cycle retrieved from the chart cycle resource
     * @param cycle the chart cycle of either current or next 56 day cycle
     * @return
     */
    public ProductSet getEditionResponse (ChartCycleElementsJson cycle) {
    	ProductSet response = initPositiveResponse();
    	response.getEdition().add(initEdition(cycle));
    	return response;   	
    }

	private boolean verifyGeoname () {
		if (NARC.equalsIgnoreCase(getGeoname()) || PORC.equalsIgnoreCase(getGeoname()) || WATRS.equalsIgnoreCase(getGeoname()) ) {
			return true;
		}
		
		return false;
	}
	
    private ChartCycleElementsJson initParameters () {
    	ChartCycleElementsJson cycle;
    	   	
    	if (CURRENT.equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new ChartCycleClient().getCurrent56DayCycle();
    	}
    	else {
    		cycle = new ChartCycleClient().getNext56DayCycle();
    	}    	
    	
    	return cycle;
    }
    
    private String formatDate (Date unformattedDate, String format) {
    	SimpleDateFormat formatter = new SimpleDateFormat(format);
    	return formatter.format(unformattedDate);
    }
}
