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


import static gov.faa.ait.apra.bootstrap.ErrorCodes.DEPRECATED;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductSet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/ders")
@Tag(name = "Digital Enroute Supplement (DERS)", description = "Digital Enroute Supplement download and edition information (DEPRECATED)")

/**
 * As of June 2017, the DERS chart set has been discontinued. 
 * This chart set is no longer available.
 * @author FAA
 *
 */
public class DigitalEnrouteSupplementCharts extends BaseService {

	private static final Logger logger = LoggerFactory.getLogger(DigitalEnrouteSupplementCharts.class);
	
	@Deprecated
	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Digital Enroute Supplement download link",
		description = "The Digital Enroute Supplement release is deprecated and publication has been discontinued as of June 2017."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "404", description = DEPRECATED, content = @Content(schema = @Schema(implementation = ProductSet.class)))
	})
	public ResponseEntity<ProductSet> getDERSRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		logger.info("Received call to retrieve current DERS product release for edition.");
		ProductSet ps = buildResponse(null);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@Deprecated
	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Digital Enroute Supplement edition information",
		description = "The Digital Enroute Supplement release is deprecated and publication has been discontinued as of June 2017."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "404", description = DEPRECATED, content = @Content(schema = @Schema(implementation = ProductSet.class)))
	})
	public ResponseEntity<ProductSet> getDERSEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {

		ProductSet ps = buildResponse(null);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
    
    
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
		return initDeprecatedResponse();
	}

}
