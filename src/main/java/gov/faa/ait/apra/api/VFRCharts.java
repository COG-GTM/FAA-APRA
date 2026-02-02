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
import gov.faa.ait.apra.jaxb.EditionCodeList;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.jaxb.ProductSet.Status;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.VFRChartCycleClient;

import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_400;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_404;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_500;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.RESPONSE_200;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;

import org.apache.commons.text.WordUtils;
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
@RequestMapping("/vfr/grandcanyon")
@Tag(name = "Grand Canyon VFR Chart", description = "Grand Canyon VFR chart download and edition information")
public class VFRCharts extends BaseService {
	private URL downloadURL = null;
	private ProductSet response = null;
	private ChartCycleElementsJson cycle = null;

	private static final Logger logger = LoggerFactory
			.getLogger(VFRCharts.class);

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get VFR Grand Canyon chart download link",
		description = "Get VFR Grand Canyon chart edition information and download link"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getGrandCanyonRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		logger.info("Received call to retrieve current VFR product release for edition '{}'.", ed);
		this.setGeoname("Grand_Canyon");
		ObjectFactory of = new ObjectFactory();

		response = of.createProductSet();

		if (!validateRequest(ed)) {
			return ResponseEntity.status(response.getStatus().getCode()).body(response);
		}

		ProductSet ps = getRelease(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get VFR Grand Canyon chart edition information",
		description = "Get VFR edition date and edition number by edition type"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getGrandCanyonEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		logger.info("Received call to retrieve current VFR product edition for edition '{}'.", ed);

		this.setGeoname("Grand_Canyon");
		ObjectFactory of = new ObjectFactory();

		response = of.createProductSet();

		if (!validateRequest(ed)) {
			return ResponseEntity.status(response.getStatus().getCode()).body(response);
		}

		ProductSet ps = getEdition(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	/**
	 * This method builds the chart release URL using the cycle infomration and edition
	 * requested.
	 * 
	 * @param cycle the airspace cycle information for the desired release
	 * @return
	 */

	public ProductSet getRelease(ChartCycleElementsJson cycle) {
		StringBuilder vfrPath = new StringBuilder(Config.getVFRUploadFolder());
		
		try {

			// the path looks like this:
			// /content/aeronav/grand_canyon_files/Grand_Canyon_<cycle_numbe>.zip
			StringBuilder fileName = new StringBuilder(getGeoname());
			fileName.append("_");
			fileName.append(cycle.getChart_cycle_number());
			fileName.append(".zip");
			
			vfrPath.append("/").append(fileName);

			downloadURL = new URL(Config.getAeronavHost()
					+ vfrPath.toString());
			if (!verifyURL(downloadURL)) {
				logger.warn(downloadURL.toExternalForm()
						+ " returned a non 200 response code when completing a HTTP HEAD check.");
				downloadURL = null;
			}
		} catch (MalformedURLException emalformed) {
			logger.error("getRelease", emalformed);
			response = getErrorResponse(500,
					"Unable to construct a valid URL for the VFR product release.");
		}

		return buildResponse(cycle);

	}

	@Override
	public ProductSet buildResponse(ChartCycleElementsJson cycle) {
		ObjectFactory of = new ObjectFactory();
		Status status = of.createProductSetStatus();
		status.setCode(200);
		status.setMessage("OK");

		ProductSet.Edition ed = of.createProductSetEdition();

		Edition.Product product = new Edition.Product();

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		ed.setEditionDate(formatter.format(cycle.getChart_effective_date()));
		ed.setEditionNumber(Integer.valueOf(cycle.getChart_cycle_number()));
		ed.setEditionName(EditionCodeList.valueOf(cycle
				.getChart_cycle_period_code()));
		ed.setGeoname(this.getGeoname());
		product.setProductName(ProductCodeList.VFR);

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
	 * Get the edition information block as a response including the date, format, 
	 * and edition information.
	 * 
	 * @param cycle the airspace cycle information for the edition to be returned
	 * @return
	 */

	public ProductSet getEdition(ChartCycleElementsJson cycle) {
		ObjectFactory of = new ObjectFactory();
		Status status = of.createProductSetStatus();

		ProductSet.Edition ed = of.createProductSetEdition();

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		ed.setEditionDate(formatter.format(cycle.getChart_effective_date()));
		ed.setEditionNumber(Integer.valueOf(cycle.getChart_cycle_number()));
		ed.setEditionName(EditionCodeList.valueOf(cycle
				.getChart_cycle_period_code()));
		ed.setGeoname(this.getGeoname());
		response.getEdition().add(ed);
		status.setCode(200);
		status.setMessage("OK");
		response.setStatus(status);

		return response;
	}

	private boolean validateParameters(ChartCycleElementsJson cycle) {
		if (logger.isDebugEnabled()) {
			logger.debug("Cycle city = "+cycle.getChart_city_name()+" this.formatCity() = "+this.formatCity());
			logger.debug("Chart cycle effective date is "+cycle.getChart_effective_date());
			logger.debug("Validation result is "+(cycle.getChart_effective_date() != null && cycle.getChart_city_name().equalsIgnoreCase(this.formatCity())));
		}
		return cycle.getChart_effective_date() != null
				&& cycle.getChart_city_name().equalsIgnoreCase(this.formatCity());
	}

	private boolean validateRequest(String ed) {

		if (ed == null || ed.isEmpty()) {
			this.setEdition(CURRENT);
		} else {
			this.setEdition(ed);
		}

		if (!verifyEdition()) {
			logger.error("Expected edition 'current or next' not received '"
					+ ed
					+ "' instead. Error response being generated and returned back.");
			response = getIllegalArgumentError();
			return false;
		}

		if (CURRENT.equalsIgnoreCase(this.getEdition())) {
			cycle = new VFRChartCycleClient().getCurrentCycle();
		} else {
			cycle = new VFRChartCycleClient().getNextCycle();
		}

		if (cycle == null) {
			logger.warn("Chart cycle infomration was NULL for "+this.getEdition()+" "+this.getGeoname());
			logger.warn("Unable to locate " + this.getEdition()
					+ " edition chart for " + this.getGeoname());
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

	private String formatCity() {
		String scratch = new String(this.getGeoname());
		char[] separators = { '-', '_', ' ' };

		String retVal = scratch.replace(" ", "_");
		retVal = WordUtils.capitalizeFully(retVal, separators);

		logger.info("Converted " + this.getGeoname() + " to " + retVal);

		if (logger.isDebugEnabled())
			logger.debug("Converted " + this.getGeoname() + " to " + retVal);

		return retVal;
	}
}
