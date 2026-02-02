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
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.util.CycleDateUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dec")
@Tag(name = "Digital Enroute Charts US (DDECUS)", description = "Digital Enroute Charts download and edition information")

/**
 * This is the service to return the URL for the Digital Enroute Charts (DDECUS). The chart set is part of the IFR and DERS chart group
 * published by the FAA.
 * 
 * @author FAA
 *
 */
public class DigitalEnrouteCharts extends BaseService {

	private static final Logger logger = LoggerFactory.getLogger(DigitalEnrouteCharts.class);

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Digital Enroute Chart download link",
		description = "Get Digital Enroute Chart download link by edition type. The DEC US release is distributed as a zip file containing charts."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getDECRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {
		logger.info("Received call to retrieve current DEC product release for edition '{}'.", ed);

		ChartCycleElementsJson cycle = initParameters(ed);

		if (!verifyEdition()) {
			logger.error("Expected edition 'current' or 'next' and received '{}' instead. Error response being generated and returned.", ed);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		ProductSet ps = buildResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Digital Enroute Chart edition information",
		description = "Get DEC edition date and edition number by edition type"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getDECEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {
		ChartCycleElementsJson cycle = initParameters(ed);

		if (!verifyEdition()) {
			logger.error("Expected edition 'current' or 'next' and received '{}' instead. Error response being generated and returned.", ed);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		ProductSet ps = initPositiveResponse();
		Edition edition = initEdition(cycle);
		setCycleNumber(edition);
		ps.getEdition().add(edition);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}    
	
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
		StringBuilder path = new StringBuilder();
		StringBuilder file = new StringBuilder();
    	ProductSet response = initPositiveResponse();
    	ProductSet.Edition ed = initEdition(cycle);
    	setCycleNumber(ed);
    	Edition.Product product = new Edition.Product();
    	product.setProductName(ProductCodeList.DEC);
    	
    	path.append(Config.getAeronavHost()).append(Config.getDECPath()).append("/");
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
		path.append(sdf.format(cycle.getChart_effective_date())).append("/");
		file.append(Config.getDECFilePrefix()).append(".").append(ZIP);
    	path.append(file);
    	product.setChartName(file.toString());
    	product.setUrl(path.toString());
    	
    	try {
    		URL url = new URL(path.toString());
    		if (verifyURL(url)) {
    			product.setUrl(path.toString());
    		}
    	}
    	catch (Exception emalformed) {
    		logger.warn("The DEC url "+path+" is invalid or malformed.", emalformed);
    		response.getStatus().setCode(404);
    		response.getStatus().setMessage(ErrorCodes.ERROR_404);
    		product.setUrl("");
    	}

    	ed.setProduct(product);
    	response.getEdition().add(ed);
    	return response;
	}
	
    private ChartCycleElementsJson initParameters (String ed) {
    	ChartCycleElementsJson cycle;
    	
    	setEdition(ed != null ? ed : CURRENT);
    	setFormat(ZIP);
    	setGeoname("US");
    	
    	if (CURRENT.equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new ChartCycleClient().getCurrent56DayCycle();
    	}
    	else {
    		cycle = new ChartCycleClient().getNext56DayCycle();
    	}    	
    	
    	return cycle;
    }
    
    protected Edition setCycleNumber (Edition edition) {
     	if (NEXT.equalsIgnoreCase(getEdition())) {
    		edition.setEditionNumber(CycleDateUtil.getNextDECCycleNumber());
    	}
    	else {
    		edition.setEditionNumber(CycleDateUtil.getCurrentDECCycleNumber());
    	}
    	   	
    	return edition;
    }
	
}
