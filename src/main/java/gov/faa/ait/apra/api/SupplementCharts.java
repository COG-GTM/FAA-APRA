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
import gov.faa.ait.apra.jaxb.FormatCodeList;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.jaxb.ProductSet.Edition.Product;
import gov.faa.ait.apra.json.SupplementMetadata;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.util.SupplementMetadataClient;

import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_400;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_404;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_500;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.RESPONSE_200;

import java.text.SimpleDateFormat;

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
@RequestMapping("/supplement")
@Tag(name = "Supplement Chart", description = "Supplement chart download and edition information")
public class SupplementCharts extends BaseService {
	private static final Logger logger = LoggerFactory
			.getLogger(SupplementCharts.class);
	private static final String US = "US";
	private static final String ERRMSGSUFFIX = " instead. Error response being generated and returned.";
	private String volume = "";
	private static final String CSALL = "CS_ALL_";

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Supplement chart download information",
		description = "Get Supplement chart download information by requesting an edition with a valid US volume. The US complete set is returned as a ZIP file while all other volumes consist of individual PDF files."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getSupplementRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Requested volume of Supplement chart set", schema = @Schema(allowableValues = {"NORTHWEST", "SOUTHWEST", "NORTH CENTRAL", "SOUTH CENTRAL", "EAST CENTRAL", "SOUTHEAST", "NORTHEAST", "PACIFIC", "ALASKA"}))
			@RequestParam(value = "volume", required = false) String vol) {
		ChartCycleElementsJson cycle;

		logger.info("Received call to retrieve current Supplement release for edition '{}' volume '{}'.", ed, vol);

		if (vol == null || vol.isEmpty()) {
			this.setVolume(US);
			setFormat(ZIP);
		} else {
			this.setVolume(vol);
		}

		if (ed == null || ed.isEmpty()) {
			this.setEdition(CURRENT);
		} else {
			this.setEdition(ed);
		}

		cycle = initParameters();

		if (!verifyEdition()) {
			logger.error("Expected edition current, next, or changeset and received '{}'{}", ed, ERRMSGSUFFIX);
			return ResponseEntity.status(400).body(getErrorResponse(400,
					"Edition must be current, next, or changeset."));
		}

		if (!isUnitedStates()) {
			logger.info("Retrieving individual Supplement charts rather than full US set. User asked for a volume or US changes.");
			setFormat(PDF);
			ProductSet ps = getChartProductSet(cycle);
			return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
		}

		ProductSet ps = buildResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Supplement chart edition information",
		description = "Get Supplement chart edition information by requesting an edition and volume"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getSupplementEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Requested volume of Supplement chart set", schema = @Schema(allowableValues = {"NORTHWEST", "SOUTHWEST", "NORTH CENTRAL", "SOUTH CENTRAL", "EAST CENTRAL", "SOUTHEAST", "NORTHEAST", "PACIFIC", "ALASKA"}))
			@RequestParam(value = "volume", required = false) String vol) {
		ChartCycleElementsJson cycle;

		logger.info("Received call to retrieve current SUPPLEMENT product release for edition '{}'.", ed);

		if (vol == null || vol.isEmpty()) {
			this.setVolume(US);
			setFormat(ZIP);
		} else {
			this.setVolume(vol);
			setFormat(PDF);
		}

		if (ed == null || ed.isEmpty()) {
			this.setEdition(CURRENT);
		} else {
			this.setEdition(ed);
		}

		cycle = initParameters();

		if (!verifyEdition()) {
			logger.error("Expected edition 'current' or 'next' and received '{}' instead. Error response being generated and returned.", ed);
			return ResponseEntity.status(400).body(getErrorResponse(400,
					"Edition must be current, next, or changeset."));
		}

		if (!isUnitedStates()) {
			logger.info("Retrieving individual Supplement charts rather than full US set. User asked for a volume or US changes.");
			ProductSet ps = getEditionResponse(cycle);
			return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
		}

		ProductSet ps = getUSEditionResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
		StringBuilder path = new StringBuilder(Config.getSUPUSPath());
		ObjectFactory of = new ObjectFactory();
		ProductSet ps = initPositiveResponse();
		Edition ed = initEdition(cycle);
		Product product = of.createProductSetEditionProduct();
		path.append("/").append(CSALL);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		path.append(
				formatter.format(cycle.getChart_effective_date())).append(".zip");
		product.setProductName(ProductCodeList.SUPPLEMENT);
		validateAndSetUrl(Config.getAeronavHost() + path.toString(), ps, product);
		ed.setProduct(product);
		ps.getEdition().add(ed);

		return ps;
	}

	private boolean getChartEditiontSet(ChartCycleElementsJson cycle) {

		SupplementMetadataClient supplementClient = new SupplementMetadataClient(
				cycle);
		SupplementMetadata[] elements = supplementClient
				.getChartMetadataByVolume(getVolume()).getElements();

		if (elements == null || elements.length == 0){
			return false;
		}
		return true;
	}
	
	private ProductSet getChartProductSet(ChartCycleElementsJson cycle) {
		String wcf = " with change flag = ";
		logger.info("Getting the chart product set for " + getEdition() + " "
				+ capitalizeGeoname() + wcf + isChangeFlag());
		ObjectFactory of = new ObjectFactory();
		SupplementMetadataClient supplementClient = new SupplementMetadataClient(
				cycle);
		SupplementMetadata[] elements = supplementClient
				.getChartMetadataByVolume(getVolume()).getElements();

		if (elements == null || elements.length == 0)
			return getErrorResponse(404, ErrorCodes.ERROR_404);

		logger.info(elements.length + " total charts found for " + getEdition()
				+ " " + capitalizeGeoname() + wcf + isChangeFlag());

		ProductSet ps = initPositiveResponse();

		for (int i = 0; i < elements.length; i++) {
			StringBuilder path = new StringBuilder(Config.getSUPChartPath());
			Edition ed = initEdition(cycle);
			ed.setFormat(FormatCodeList.PDF);
			ed.setGeoname(elements[i].getState());
			ed.setVolume(elements[i].getVolumeName());

			Product product = of.createProductSetEditionProduct();
			product.setProductName(ProductCodeList.SUPPLEMENT);
			SimpleDateFormat formatter = new SimpleDateFormat("ddMMMyyyy");
			path.append("/").append(
					formatter.format(cycle.getChart_effective_date()));
			path.append("/").append(elements[i].getPdf());

			product.setUrl(Config.getAeronavHost() + path.toString());

			if (Config.getSUPCheckFlag()) {
				logger.warn("URL validation check is enabled for the Supplement product set. This can cause serious performance issues for the Supplement product responses."
						+ " Consider changing the configuration parameter gov.faa.ait.sup.check.flag = false and re-deploy.");
				validateAndSetUrl(Config.getAeronavHost() + path.toString(),
						ps, product);
			}

			ed.setProduct(product);
			ps.getEdition().add(ed);
		}

		return ps;
	}

	private ChartCycleElementsJson initParameters() {
		ChartCycleElementsJson cycle;

		if (CURRENT.equalsIgnoreCase(this.getEdition())) {
			cycle = new ChartCycleClient().getCurrent56DayCycle();
		} else {
			cycle = new ChartCycleClient().getNext56DayCycle();
		}

		return cycle;
	}

	/**
	 * Get the edition information response using the specified chart cycle
	 * retrieved from the chart cycle resource
	 * 
	 * @param cycle
	 *            the chart cycle of either current or next 28 day cycle
	 * @return
	 */
	public ProductSet getEditionResponse(ChartCycleElementsJson cycle) {
		ProductSet response = initPositiveResponse();
		if(getChartEditiontSet(cycle)){
			Edition ed = initEdition(cycle);
			ed.setVolume(getVolume());
			response.getEdition().add(ed);
		}else{
			response = this.getErrorResponse(404, ErrorCodes.ERROR_404);
		}
		return response;
	}
	
	private ProductSet getUSEditionResponse(ChartCycleElementsJson cycle) {
		ProductSet response = initPositiveResponse();
		Edition ed = initEdition(cycle);
		ed.setVolume(getVolume());
		response.getEdition().add(ed);		
		return response;
	}

	private boolean isUnitedStates() {
		return US.equalsIgnoreCase(getVolume());
	}

	private void setVolume(String vol) {
		this.volume = vol;
	}

	private String getVolume() {
		return this.volume;
	}
}
