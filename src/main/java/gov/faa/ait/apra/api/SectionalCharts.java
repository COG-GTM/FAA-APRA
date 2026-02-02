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
import java.util.Arrays;

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
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vfr/sectional")
@Tag(name = "Sectional Charts", description = "VFR Sectional chart download and edition information")
public class SectionalCharts extends AbstractTableDataService {

	private static final String CHART_TYPE_SECTIONAL = "SECTIONAL";

	private static final Logger logger = LoggerFactory.getLogger(SectionalCharts.class);
	
	public SectionalCharts() {
		setClient(new TableChartClient());
	}
	
	public SectionalCharts(TableChartClient client) {
		setClient(client);
	}
	
	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Sectional Chart download link",
		description = "Get Sectional Chart download link by edition, format, and geoname. TIFF formatted files are geo-referenced while PDF format is not geo-referenced.",
		externalDocs = @ExternalDocumentation(description = "FAA Sectional Charts", url = "http://www.faa.gov/air_traffic/flight_info/aeronav/digital_products/vfr/")
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getSectionalChart(
			@Parameter(description = "Geoname which is a city for which the chart is requested", required = true)
			@RequestParam("geoname") String cityRegion,
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String edition, 
			@Parameter(description = "Format of the requested chart. TIFF is georeferenced and PDF is not georeferenced", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String format) {

		this.setCity(cityRegion);
		ProductSet ps = super.buildChart(format, edition, CHART_TYPE_SECTIONAL);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
	
	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Sectional Chart edition information",
		description = "Get Sectional Chart edition date and edition number by edition type and geoname"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getSectionalInfo(
			@Parameter(description = "Geoname which is a city for which the chart is requested", required = true)
			@RequestParam("geoname") String cityRegion,
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String edition) {
		this.setCity(cityRegion);
		ProductSet ps = super.buildInfo(edition, CHART_TYPE_SECTIONAL);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
	
	protected Product createProduct(ChartCycleElementsJson element) {
		gov.faa.ait.apra.jaxb.ObjectFactory of = new gov.faa.ait.apra.jaxb.ObjectFactory();
		Product prod = of.createProductSetEditionProduct();
		prod.setProductName(ProductCodeList.SECTIONAL);
		StringBuilder productUrl = new StringBuilder();
		productUrl.append(Config.getAeronavHost()).append(Config.getAeronavSectionalFolder());
		
		logger.info("Starting call to create sectional product.");
		
		if("PDF".equalsIgnoreCase(this.getFormat())) {
			productUrl.append("/PDFs");
		}
		String cityFileName = element.getChart_city_name().replace(" ", "_");
		productUrl.append("/").append(cityFileName).append("_").append(element.getChart_cycle_number());
		if("PDF".equalsIgnoreCase(this.getFormat())) {
			productUrl.append("_P.pdf");
		} else if ("TIFF".equalsIgnoreCase(this.getFormat()) || "ZIP".equalsIgnoreCase(this.getFormat())) {
			productUrl.append(".zip");
		}
		try {
			logger.info("HEAD check flag is "+Config.getTPPCheckFlag());
			
			if (Config.getSectioanlCheckFlag()) {
				if (this.verifyURL(new URL(productUrl.toString()))) {
					
					if (logger.isInfoEnabled()) {
						logger.info("HEAD check succeeeded for Sectional product URL: "+productUrl.toString());
					}
					prod.setUrl(productUrl.toString());
				}
				else {
					if (logger.isWarnEnabled()) {
						logger.warn("HEAD check failed for Sectional product URL: "+productUrl.toString());
					}
					prod.setUrl("");
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("HEAD check not executed for Sectional product URL: "+productUrl.toString());
				}
				prod.setUrl(productUrl.toString());
			}
		} catch (MalformedURLException emalformed) {
    		logger.warn("The download URL "+productUrl.toString()+" is not valid", emalformed);
		}	
		
		logger.info("Ending call to create sectional product.");
		
		return prod;
	}

	@Override
	protected boolean verifyGeoName() {
		String[] validCities = {"Albuquerque",
				"Anchorage",
				"Atlanta",
				"Bethel",
				"Billings",
				"Brownsville",
				"Cape Lisburne",
				"Charlotte",
				"Cheyenne",
				"Chicago",
				"Cincinnati",
				"Cold Bay",
				"Dallas-Ft Worth",
				"Dawson",
				"Denver",
				"Detroit",
				"Dutch Harbor",
				"El Paso",
				"Fairbanks",
				"Great Falls",
				"Green Bay",
				"Halifax",
				"Hawaiian Islands",
				"Houston",
				"Jacksonville",
				"Juneau",
				"Kansas City",
				"Ketchikan",
				"Klamath Falls",
				"Kodiak",
				"Lake Huron",
				"Las Vegas",
				"Los Angeles",
				"McGrath",
				"Memphis",
				"Miami",
				"Montreal",
				"New Orleans",
				"New York",
				"Nome",
				"Omaha",
				"Phoenix",
				"Point Barrow",
				"Salt Lake City",
				"San Antonio",
				"San Francisco",
				"Seattle",
				"Seward",
				"St Louis",
				"Twin Cities",
				"Washington",
				"Western Aleutian Islands",
				"Whitehorse",
				"Wichita"};
		/*
		if(Arrays.asList(validCities).stream().filter(value -> value.equalsIgnoreCase(this.getCity())).count()==1)
			return true;
		else 
			return false;
			*/
		
		return Arrays.asList(validCities).stream().filter(value -> value.equalsIgnoreCase(this.getCity())).count()==1;
	}

}
