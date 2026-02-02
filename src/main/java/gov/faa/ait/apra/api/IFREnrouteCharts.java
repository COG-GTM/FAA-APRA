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
import gov.faa.ait.apra.jaxb.AltitudeCategoryCodeList;
import gov.faa.ait.apra.jaxb.EditionCodeList;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.jaxb.ProductSet.Status;
import gov.faa.ait.apra.path.PathElement;
import gov.faa.ait.apra.path.ProductPath;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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

@RestController
@RequestMapping("/enroute")
@Tag(name = "IFR Enroute Charts", description = "IFR Enroute chart download and edition information")
public class IFREnrouteCharts extends BaseService {
	private static final String MM_DD_YYYY2 = "MM-dd-yyyy";
	private static final String MM_DD_YYYY = "MM/dd/yyyy";
	private static final String PACIFIC = "PACIFIC";
	private static final String CARIBBEAN = "CARIBBEAN";
	private static final String AREA = "AREA";
	private static final String HIGH = "HIGH";
	private static final String LOW = "LOW";
	private static final String US = "US";
	private String seriesType = "";
	private URL downloadURL = null;
	private ProductSet response = null;
	private ChartCycleElementsJson cycle = null;
	private ChartCycleClient client;
	private static final Logger logger = LoggerFactory
			.getLogger(IFREnrouteCharts.class);
	private static final String ALASKA = "Alaska";
	
	/**
	 * Default null constructor that initializes the chart cycle
	 */
	public IFREnrouteCharts() {
		this.client = new ChartCycleClient();
	}
	
	/**
	 * Construct the IFR Enroute charts services with a specific chart cycle client already constructed and passed as a parameter.
	 * @param client the chart cycle client to use for this service
	 */
	public IFREnrouteCharts(ChartCycleClient client) {
		this.client = client;
	}

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get IFR Enroute Charts download link",
		description = "Get IFR Enroute Charts download link by edition, format, geoname, and seriesType. TIFF formatted files are geo-referenced while PDF format is not geo-referenced."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getIFREnrouteRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt,
			@Parameter(description = "Geographic region for requested chart", required = true, schema = @Schema(allowableValues = {"US", "Alaska", "Pacific", "Caribbean"}))
			@RequestParam("geoname") String geo,
			@Parameter(description = "The series type", required = true, schema = @Schema(allowableValues = {"low", "high", "area"}))
			@RequestParam("seriesType") String seriesType) {

		logger.info("Received call to retrieve current IFR Enroute Charts product release for '{}', '{}', '{}', '{}'", ed, fmt, geo, seriesType);
		ObjectFactory of = new ObjectFactory();

		response = of.createProductSet();

		if (!validateRequest(ed, fmt, geo, seriesType)) {
			return ResponseEntity.status(response.getStatus().getCode()).body(response);
		}

		ProductSet ps = getRelease(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get IFR Enroute Charts edition information",
		description = "Get IFR Enroute Charts edition date and edition number by edition type"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getIFREnrouteEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		logger.info("Received call to retrieve current IFR Enroute Charts edition release for '{}'", ed);

		ObjectFactory of = new ObjectFactory();

		response = of.createProductSet();

		if (!this.validateRequest(ed, PDF, ALASKA, LOW)) {
			return ResponseEntity.status(response.getStatus().getCode()).body(response);
		}
		this.setGeoname(null);
		this.setSeriesType(null);
		this.setFormat(null);
		ProductSet ps = getEdition(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	/**
	 * 
	 * @param cycle
	 * @return
	 */

	public ProductSet getRelease(ChartCycleElementsJson cycle) {
		ObjectFactory of = new ObjectFactory();
		Status status = of.createProductSetStatus();
		status.setCode(200);
		status.setMessage("OK");
		this.response.setStatus(status);


		// get set count by format, high-low, and geo area
		int setCount = Config.getEnrouteSetCount(this.getGeoname(), this.getSeriesType(), this.getFormat());
		
		int step = PDF.equalsIgnoreCase(this.getFormat()) ? 2 : 1;
		boolean anyUrlSet = false;
		for(int i=1; i<=setCount; i+=step) {
			ProductPath vfrPath = new ProductPath();
			vfrPath.addPathElement(new PathElement(Config.getEnrouteFolder()));
			ProductSet.Edition ed = of.createProductSetEdition();

			Edition.Product product = new Edition.Product();

			SimpleDateFormat sdfUS = new SimpleDateFormat(MM_DD_YYYY);
			ed.setEditionDate(sdfUS.format(cycle.getChart_effective_date()));
			ed.setEditionNumber(Integer.valueOf(cycle.getChart_cycle_number()));
			ed.setEditionName(EditionCodeList.valueOf(cycle
					.getChart_cycle_period_code()));
			ed.setGeoname(this.getGeoname());
			ed.setFormat(gov.faa.ait.apra.jaxb.FormatCodeList.valueOf(this.getFormat()));
			try {
				SimpleDateFormat sdfUSDash = new SimpleDateFormat(MM_DD_YYYY2);
				PathElement peDir = new PathElement(sdfUSDash.format(cycle
						.getChart_effective_date()));
				vfrPath.addPathElement(peDir);
				
				String fileName = this.buildFileName(this.getGeoname(), this.getFormat(), this.seriesType, i);
				PathElement pe = new PathElement(fileName);
				pe.setFile();
				vfrPath.addPathElement(pe);
	
				downloadURL = new URL(Config.getAeronavHost()
						+ vfrPath.getPathAsString());
				if (!verifyURL(downloadURL)) {
					logger.warn(downloadURL.toExternalForm()
							+ " returned a non 200 response code when completing a HTTP HEAD check.");
					downloadURL = null;
				}
				if(downloadURL != null) {
					anyUrlSet = true;
					product.setUrl(downloadURL.toString());
				}
				ed.setProduct(product);
			} catch (MalformedURLException emalformed) {
				logger.error("getRelease", emalformed);
				response = getErrorResponse(500,
						"Unable to construct a valid URL for the VFR product release.");
			}
			response.getEdition().add(ed);
		}
		
		if(!anyUrlSet) {
			response = getErrorResponse(404, ErrorCodes.ERROR_404);
		}
		return response;

	}
	/**
	 * builds filename (not directory)
	 * @param area
	 * @param format
	 * @param altLevel
	 * @param setIndex
	 * @return
	 */
	private String buildFileName(String geoname, String format, String altLevel, int setIndex) {
		StringBuilder filename = new StringBuilder();
		if(PDF.equalsIgnoreCase(format)) {
			filename.append("d");
		}
		logger.debug("computing filename for "+ geoname + ", "+format+", "+ altLevel +", "+ setIndex);
		if(US.equalsIgnoreCase(geoname) ) {
			if( TIFF.equalsIgnoreCase(format) ) {
				if (LOW.equalsIgnoreCase(altLevel)) {
					filename.append("enr_l").append(String.format("%02d", setIndex));
				} else if (HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("enr_h").append(String.format("%02d", setIndex));
				} else if (US.equalsIgnoreCase(geoname) && TIFF.equalsIgnoreCase(format) && AREA.equalsIgnoreCase(altLevel)) {
					filename.append("enr_a").append(String.format("%02d", setIndex));
				}
			} else if (PDF.equalsIgnoreCase(format) ) {
				if (LOW.equalsIgnoreCase(altLevel)) {
					filename.append("elus").append(setIndex);
				} else if (HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("ehus").append(setIndex);
				} else if (US.equalsIgnoreCase(geoname) && PDF.equalsIgnoreCase(format) && AREA.equalsIgnoreCase(altLevel)) {
					filename.append("area");
				}
			}
		} else if(ALASKA.equalsIgnoreCase(geoname)) {
			if (TIFF.equalsIgnoreCase(format)) {
				if(LOW.equalsIgnoreCase(altLevel)) {
					filename.append("enr_akl").append(String.format("%02d", setIndex));
				} else if(HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("enr_akh").append(String.format("%02d", setIndex));
				}
			} else if(PDF.equalsIgnoreCase(format)) {
				if (LOW.equalsIgnoreCase(altLevel)) {
					filename.append("elak").append(setIndex);
				} else if(HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("ehak").append(setIndex);
				}
			}
		} else if (PACIFIC.equalsIgnoreCase(geoname)) {
			if (TIFF.equalsIgnoreCase(format)) {
				if (HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("enr_p").append(String.format("%02d", setIndex));
				}
			} else if (PDF.equalsIgnoreCase(format) ) {
				if (HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("ephi").append(setIndex);
				}	
			}
		} else if (CARIBBEAN.equalsIgnoreCase(geoname)) {
			if (PDF.equalsIgnoreCase(format)) {
				if (LOW.equalsIgnoreCase(altLevel)) {
					filename.append("elcb").append(setIndex);
				} else if (HIGH.equalsIgnoreCase(altLevel)) {
					filename.append("ehcb").append(setIndex);
				} else if (AREA.equalsIgnoreCase(altLevel) ) {
					if (setIndex == 3) {
						filename.append("elcb3"); // special case where area and low are zipped together
					} else {
						filename.append("elcba").append(setIndex);
					}
				}
			}
		}
		
		filename.append(".zip");
		
		return filename.toString();
	}

	@Override
	public ProductSet buildResponse(ChartCycleElementsJson cycle) {
		ObjectFactory of = new ObjectFactory();
		Status status = of.createProductSetStatus();
		status.setCode(200);
		status.setMessage("OK");

		ProductSet.Edition ed = of.createProductSetEdition();

		Edition.Product product = new Edition.Product();

		SimpleDateFormat formatter = new SimpleDateFormat(MM_DD_YYYY);
		ed.setEditionDate(formatter.format(cycle.getChart_effective_date()));
		ed.setEditionNumber(Integer.valueOf(cycle.getChart_cycle_number()));
		ed.setEditionName(EditionCodeList.valueOf(cycle
				.getChart_cycle_period_code()));
		ed.setGeoname(this.getGeoname());
		if(HIGH.equalsIgnoreCase(this.getSeriesType())){
			ed.setAltitude(AltitudeCategoryCodeList.HIGH);
		}else{
			ed.setAltitude(AltitudeCategoryCodeList.LOW);
		}
		product.setProductName(ProductCodeList.IFR_ENROUTE);

		if (downloadURL != null) {
			product.setUrl(downloadURL.toExternalForm());
		} else {
			status.setCode(404);
			status.setMessage(ErrorCodes.ERROR_404);
			product.setUrl("");
		}
		ed.setProduct(product);
		response.setStatus(status);
		response.getEdition().add(ed);

		return response;

	}

	/**
	 * 
	 * @param cycle
	 * @return
	 */

	public ProductSet getEdition(ChartCycleElementsJson cycle) {
		ObjectFactory of = new ObjectFactory();
		Status status = of.createProductSetStatus();

		ProductSet.Edition ed = of.createProductSetEdition();
		SimpleDateFormat formatter = new SimpleDateFormat(MM_DD_YYYY);
		ed.setEditionDate(formatter.format(cycle.getChart_effective_date()));
		ed.setEditionNumber(Integer.valueOf(cycle.getChart_cycle_number()));
		ed.setEditionName(EditionCodeList.valueOf(cycle
				.getChart_cycle_period_code()));
		response.getEdition().add(ed);
		status.setCode(200);
		status.setMessage("OK");
		response.setStatus(status);

		return response;
	}

	private boolean validateParameters(ChartCycleElementsJson cycle) {

		return cycle.getChart_effective_date() != null;
	}

	private boolean validateRequest(String ed, String fmt, String geo,
			String seriesType) {

		if (ed == null || ed.isEmpty()) {
			this.setEdition(CURRENT);
		} else {
			this.setEdition(ed);
		}

		if (fmt == null || fmt.isEmpty()) {
			this.setFormat("pdf");
		} else {
			this.setFormat(fmt);
		}

		this.setGeoname(geo);

		this.setSeriesType(seriesType);

		if (!verifyEdition()) {
			logger.error("Expected edition 'current or next' not received '"
					+ ed
					+ "' instead. Error response being generated and returned back.");
			response = getIllegalArgumentError();
			return false;
		}

		if (!verifyFormat()) {
			logger.error("Expected format of 'tiff' or 'pdf'. Received format '"
					+ fmt
					+ "' instead. Error response being generated and returned");
			response = getIllegalArgumentError();
			return false;
		}

		if (!verifyGeo()) {
			logger.error("Expected 'geo' value, but it is null or empty '"
					+ geo
					+ "' Geoname which is a city for which the chart is requested.");
			response = this
					.getErrorResponse(
							404,
							"A Geoname value  is either 'US', 'Alaska', 'Pacific' or 'Caribbean', must be specified for IFR Enroute charts.");
			return false;

		}

		if (!verifySeriesType()) {
			logger.error("Expected 'alt' value, but it is null or empty '"
					+ seriesType
					+ "' seriesType the chart is requested which is either 'Low', 'high', or 'area'.");
			response = this
					.getErrorResponse(
							404,
							"A seriesType value  is either 'Low','high', or 'area', must be specified for IFR Enroute charts.");
			return false;

		}

		
		if (CURRENT.equalsIgnoreCase(this.getEdition())) {
			cycle = this.client.getCurrent56DayCycle();
		} else {
			cycle = this.client.getNext56DayCycle();
		}

		if (cycle == null) {
			logger.warn("Unable to locate " + this.getEdition()
					+ " edition chart for geoname " + this.getGeoname());
			response = this
					.getErrorResponse(
							404,
							"Unable to locate " + this.getEdition()
									+ " edition chart for geoname "
									+ this.getGeoname());
			return false;
		}

		if (!validateParameters(cycle)) {
			logger.error("Parameters validation failed in getProductRelease.");
			response = getErrorResponse(404,
					ErrorCodes.ERROR_404);
			return false;
		}

		return true;
	}

	private boolean verifyGeo() {
		return "Alaska".equalsIgnoreCase(this.getGeoname()) || US.equalsIgnoreCase(this.getGeoname()) || CARIBBEAN.equalsIgnoreCase(this.getGeoname()) || PACIFIC.equalsIgnoreCase(this.getGeoname()	);		
	}

	private boolean verifySeriesType() {
		return "Low".equalsIgnoreCase(this.getSeriesType()) || "High".equalsIgnoreCase(this.getSeriesType()) || AREA.equalsIgnoreCase(this.getSeriesType());		
	}

	private void setSeriesType(String seriesType) {
		this.seriesType = seriesType;
	}

	private String getSeriesType() {
		return this.seriesType;
	}

}
