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

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.bootstrap.ErrorCodes;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.WallPlanningChartCycleClient;

import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_400;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_404;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_500;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.RESPONSE_200;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vfr/wallplanning")
@Tag(name = "US VFR Wall Planning Chart", description = "US VFR Wall Planning chart download and edition information")
public class WallPlanningCharts extends BaseService {
	private ProductSet response = null;
	private URL downloadURL = null;
	private ChartCycleElementsJson cycle = null;

	private static final Logger logger = LoggerFactory
			.getLogger(WallPlanningCharts.class);

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get WallPlan Chart release information with download link",
		description = "Get WallPlan Chart release information with download link by edition and format"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getProductRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt) {

		logger.info("Received call to retrieve current WallPlan product release for edition '{}' format '{}'.", ed, fmt);
		ObjectFactory of = new ObjectFactory();

		response = of.createProductSet();

		setEdition(ed != null ? ed : CURRENT);
		setFormat(fmt != null ? fmt : PDF);
		setGeoname("US");

		if (!validateRequest(ed, fmt)) {
			return ResponseEntity.status(response.getStatus().getCode()).body(response);
		}

		ProductSet ps = getRelease(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get WallPlan edition date and edition number",
		description = "Get WallPlan edition date and edition number by edition type and format"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getProductEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt) {

		logger.info("Received call to retrieve current WallPlan product release for edition '{}' format '{}'.", ed, fmt);

		ObjectFactory of = new ObjectFactory();

		response = of.createProductSet();

		setEdition(ed != null ? ed : CURRENT);
		setFormat(fmt != null ? fmt : PDF);
		setGeoname("US");

		if (!validateRequest(ed, fmt)) {
			return ResponseEntity.status(response.getStatus().getCode()).body(response);
		}

		ProductSet ps = getEdition(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	/**
	 * This method return product set
	 * 
	 * @param cycle
	 * @return
	 */
	public ProductSet getRelease(ChartCycleElementsJson cycle) {
		
		StringBuilder wallPlanPath = new StringBuilder(Config.getWallplanUploadFolder());
		
		if (cycle.getChart_effective_date() != null) {
			// the path looks like this:
			// /content/aeronav/grand_canyon_files/US_WallPlan_<cycle_numbe>.zip/US_WallPlan_<cycle_numbe>_P.pdf
			StringBuilder fileName = new StringBuilder("US_WallPlan_");
			fileName.append(cycle.getChart_cycle_number());
			if (PDF.equalsIgnoreCase(this.getFormat())) {
				fileName.append("_P.pdf");
			} else {
				fileName.append(".zip");
			}
			
			wallPlanPath.append("/").append(fileName);

			try {
				downloadURL = new URL(Config.getAeronavHost()
						+ wallPlanPath.toString());

				if (!verifyURL(downloadURL)) {
					logger.warn(downloadURL.toExternalForm()
							+ " returned a non 200 response code when completing a HTTP HEAD check.");
					//downloadURL = null;
				}
			} catch (MalformedURLException emalformed) {
				logger.error("getRelease", emalformed);
				return getErrorResponse(500,
						"Unable to construct a valid URL for the WallPlan product release.");
			}
		}

		return buildResponse(cycle);

	}

	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
		
		ProductSet responseXml = getEdition(cycle);
    	Edition.Product product = new Edition.Product();  	
    	product.setProductName(ProductCodeList.WALLPLANNING);
    	
     	validateAndSetUrl(downloadURL.toExternalForm(), responseXml, product);
    	
    	responseXml.getEdition().get(0).setProduct(product);
    	
		return responseXml;

	}

	/**
	 * This method return product set
	 * 
	 * @param cycle
	 * @return
	 */
	public ProductSet getEdition(ChartCycleElementsJson cycle) {
    	ProductSet responseXml = initPositiveResponse();
    	
    	ProductSet.Edition ed = initEdition(cycle);
    	
    	responseXml.getEdition().add(ed);    	
    	return responseXml;   	
	}

	private boolean validateRequest(String ed, String fmt) {

		if (!verifyEdition()) {
			logger.error("Expected edition 'current or next' not received '"
					+ ed
					+ "' instead. Error response being generated and returned back.");
			response = getIllegalArgumentError();
			return false;
		}
    	if (!verifyFormat()) {
    		logger.error("Expected format of 'tiff' or 'pdf'. Received format '"+fmt+"' instead. Error response being generated and returned");
    		response = getIllegalArgumentError();
    		return false;
    	}

		if (CURRENT.equalsIgnoreCase(this.getEdition())) {
			cycle = new WallPlanningChartCycleClient().getCurrentCycle();
			if (cycle == null) 
				logger.warn("Chart cycle for Wall Planning chart CURRENT edition is NULL!");
		} else {
			cycle = new WallPlanningChartCycleClient().getNextCycle();
			if (cycle == null) 
				logger.warn("Chart cycle for Wall Planning chart CURRENT edition is NULL!");
		}

		if (cycle == null) {
			logger.warn("Unable to locate " + this.getEdition()
					+ " edition chart for " + this.getFormat());
			response = this.getErrorResponse(404, ErrorCodes.ERROR_404);
			return false;
		}

		if (!validateParameters(cycle)) {
			logger.error("Parameters validation failed in getProductRelease.");
			response = getErrorResponse(404, ErrorCodes.ERROR_404);
			return false;
		}

		return true;
	}

	private boolean validateParameters(ChartCycleElementsJson cycle) {
		return cycle.getChart_effective_date() != null;
	}

}
