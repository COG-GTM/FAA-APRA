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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition.Product;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.util.TableChartClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/ifr/planning")
@Tag(name = "IFR Planning Charts", description = "IFR Planning chart download and edition information")
public class UsIfrVfrPlanning extends AbstractTableDataService {

	private static final String GEONAME_CONUS = "CONUS";
	private static final String CHART_TYPE_IFR_PLANNING = "IFR_PLANNING";
	private static final Logger logger = LoggerFactory.getLogger(UsIfrVfrPlanning.class);
	
	/**
	 * Default constructor
	 * Sets city to CONUS 
	 */
	public UsIfrVfrPlanning() {
		setClient(new TableChartClient());
		this.setCity(GEONAME_CONUS);
	}
	
	/**
	 * This constructor allows a specific chart client to be used.  Mainly for test purposes.
	 * @param client
	 */
	public UsIfrVfrPlanning(TableChartClient client) {
		setClient(client);
		this.setCity(GEONAME_CONUS);
	}

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get IFR planning download link by edition and format",
		description = "TIFF formatted files are geo-referenced while PDF format is not geo-referenced. The specific chart returned by this operation is the IFR PLANNING chart."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getIfrPlanningChart(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String edition,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String format) {

		ProductSet ps = super.buildChart(format, edition, CHART_TYPE_IFR_PLANNING);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Planning Chart edition date and edition number by edition type",
		description = "Get Planning Chart edition date and edition number by edition type"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getIfrPlanningInfo(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String edition) {

		ProductSet ps = super.buildInfo(edition, CHART_TYPE_IFR_PLANNING);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@Override
	protected Product createProduct(ChartCycleElementsJson element) {
		SimpleDateFormat sdfUSA = new SimpleDateFormat("MM-dd-yyyy");
		gov.faa.ait.apra.jaxb.ObjectFactory of = new gov.faa.ait.apra.jaxb.ObjectFactory();
		Product prod = of.createProductSetEditionProduct();
		prod.setProductName(ProductCodeList.IFR_PLANNING);
		StringBuilder productUrl = new StringBuilder();
		productUrl.append(Config.getAeronavHost())
			.append("/")
			.append(Config.getEnrouteFolder())
			.append("/IFR_Planning/")
			.append(sdfUSA.format(element.getChart_effective_date()) );
		productUrl.append("/").append("US_IFR_Planning");
		if("PDF".equalsIgnoreCase(this.getFormat())) {
			productUrl.append("_pdf.zip");
		} else if ("TIFF".equalsIgnoreCase(this.getFormat()) || "ZIP".equalsIgnoreCase(this.getFormat())) {
			productUrl.append("_tif.zip");
		}
		try {
			if(this.verifyURL(new URL(productUrl.toString()))) {
				prod.setUrl(productUrl.toString());
			}
		} catch (MalformedURLException emalformed) {
    		logger.warn("The download URL is not valid", emalformed);
		}	
		return prod;
	}

	@Override
	protected boolean verifyGeoName() {
		return true;  // CONUS is hard-coded 
	}

}
