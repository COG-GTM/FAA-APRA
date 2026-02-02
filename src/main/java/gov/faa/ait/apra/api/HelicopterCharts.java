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
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition.Product;

import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_400;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_404;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_500;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.RESPONSE_200;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.util.TableChartClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vfr/helicopter")
@Tag(name = "VFR Helicopter Route Chart", description = "VFR Helicopter Route Chart download and edition information")
public class HelicopterCharts extends AbstractTableDataService {

	private static final String CHART_TYPE_HELICOPTER_VFR = "HELICOPTER_VFR";
	private static HashMap<String, String> cityPathMap = new HashMap<>();
	private static String usGulfCoast = "U.S Gulf Coast";

	private static final Logger logger = LoggerFactory
			.getLogger(HelicopterCharts.class);
	/**
	 * To avoid loading city and paths for each instance of this class calling
	 * following static block and this block calls only once when server stars.
	 */
	static {
		populateCityPaths();
	}

	/**
	 * Default constructor
	 */
	public HelicopterCharts() {
		setClient(new TableChartClient());
	}

	/**
	 * This constructor allows a specific chart client to be used. Mainly for
	 * test purposes.
	 * 
	 * @param client
	 */
	public HelicopterCharts(TableChartClient client) {
		setClient(client);
	}

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get VFR Helicopter Route Chart download link",
		description = "Get VFR Helicopter Route Chart download link by edition and geoname"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getHelicopterRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt,
			@Parameter(description = "Geoname which is a city for which the chart is requested")
			@RequestParam(value = "geoname", required = false) String cityRegion) {

		logger.info("Received call to retrieve current VFR Helicopter Route Chart product release for edition '{}' City '{}'.", ed, cityRegion);
		this.setCity(cityRegion);
    	setEdition(ed != null ? ed : CURRENT);
    	setFormat(fmt != null ? fmt : PDF);

		ProductSet ps = super.buildChart(fmt, ed, CHART_TYPE_HELICOPTER_VFR);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get VFR Helicopter Route Chart edition information",
		description = "Get VFR Helicopter Route Chart edition date and edition number by edition type and geoname"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getHelicopterEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Geoname which is a city for which the chart is requested")
			@RequestParam(value = "geoname", required = false) String cityRegion) {

		logger.info("Received call to retrieve current VFR Helicopter Route Chart product edition for edition '{}'.", ed);

		this.setCity(cityRegion);
		ProductSet ps = super.buildInfo(ed, CHART_TYPE_HELICOPTER_VFR);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/gulf/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get GulfCoast Route Chart download link",
		description = "Get GulfCoast Route Chart download link by edition. The geoname defaults to U.S Gulf Coast"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getGulfCoastRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt) {

		logger.info("Received call to retrieve current VFR GulfCoast Route Chart product release for edition '{}'.", ed);
    	setEdition(ed != null ? ed : CURRENT);
    	setFormat(fmt != null ? fmt : PDF);
    	
		return getHelicopterRelease(ed, fmt, usGulfCoast);
	}

	@GetMapping(value = "/gulf/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get VFR GulfCoast Route Chart edition information",
		description = "Get VFR GulfCoast Route Chart edition date and edition number by edition type"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getGulfCoastEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		logger.info("Received call to retrieve current VFR GulfCoast Route Chart product edition for edition '{}'.", ed);
		return getHelicopterEdition(ed, usGulfCoast);
	}

	private static void populateCityPaths() {
		cityPathMap.put("Baltimore Washington Heli".toLowerCase(Locale.ENGLISH),
				"Balt Wash Heli");
		cityPathMap.put("Dallas Ft. Worth Heli".toLowerCase(Locale.ENGLISH),
				"Dallas-Ft Worth Heli");
		cityPathMap.put(usGulfCoast.toLowerCase(Locale.ENGLISH), "US Gulf Coast Heli");

	}

	@Override
	protected Product createProduct(ChartCycleElementsJson element) {
		gov.faa.ait.apra.jaxb.ObjectFactory of = new gov.faa.ait.apra.jaxb.ObjectFactory();
		Product prod = of.createProductSetEditionProduct();
		prod.setProductName(ProductCodeList.VFR_HELICOPTER);
		StringBuilder productUrl = new StringBuilder();
		productUrl.append(Config.getAeronavHost());
		
		String cityFileName = cityPathMap.get(element.getChart_city_name()
				.toLowerCase());
		if (cityFileName == null) {
			cityFileName = element.getChart_city_name();
		}

		cityFileName = cityFileName.replace(" ", "_");
		
		if (TIFF.equalsIgnoreCase(getFormat())) {
			productUrl.append(Config.getHelicopterTIFFPath()).append("/").append(cityFileName).append("_");
			productUrl.append(element.getChart_cycle_number()).append(".zip");
		}
		else {
			productUrl.append(Config.getHelicopterPDFPath()).append("/").append(cityFileName).append("_");			
			productUrl.append(element.getChart_cycle_number()).append("_P").append(".pdf");
		}

		try {
			if (this.verifyURL(new URL(productUrl.toString()))) {
				prod.setUrl(productUrl.toString());
			}
		} catch (MalformedURLException emalformed) {
			logger.warn("The download URL is not valid", emalformed);
		}
		return prod;
	}

	@Override
	protected boolean verifyGeoName() {
		return true;
	}
	
	

}
