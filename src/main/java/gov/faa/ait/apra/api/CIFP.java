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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
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

@RestController
@RequestMapping("/cifp")
@Tag(name = "Coded Instrument Flight Procedures (CIFP)", description = "CIFP chart download and edition information")
public class CIFP extends BaseService {

	private URL downloadURL = null;
	private static final Logger logger = LoggerFactory.getLogger(CIFP.class);
	
	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get CIFP chart download link",
		description = "Get CIFP chart download link by edition type of current or next. If edition is left blank or null, the default edition of current is used. The CIFP release is distributed as a zip file containing charts and verification software."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getCIFPRelease(
			@Parameter(description = "Requested product edition. If omitted, current edition is returned.", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {
		ChartCycleElementsJson cycle;
		
		logger.info("Received call to retrieve current CIFP product release for edition '{}'.", ed);
		
		cycle = initParameters(ed);
		   	
		if (!verifyEdition()) {
			logger.error("Expected edition 'current' or 'next' and received '{}' instead. Error response being generated and returned.", ed);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getIllegalArgumentError());
		}
		
		ProductSet ps = getRelease(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
   
	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get CIFP edition information",
		description = "Get CIFP edition date and edition number by edition type of current or next. If the edition is left blank or null, the default edition of current is used."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getCIFPEdition(
			@Parameter(description = "Requested product edition. If omitted, current edition is returned.", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {
		ChartCycleElementsJson cycle;
		
		cycle = initParameters(ed);
		
		if (!verifyEdition()) {
			logger.error("Expected edition 'next' and received '{}' instead. Error response being generated and returned.", ed);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getIllegalArgumentError());
		}
		ProductSet ps = getEdition(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}    
    
    private ChartCycleElementsJson initParameters (String ed) {
    	ChartCycleElementsJson cycle;
    	
    	setEdition(ed != null ? ed : CURRENT);
    	setFormat(ZIP);
    	setGeoname(EMPTY_STRING);
    	
    	if ("current".equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new ChartCycleClient().getCurrent28DayCycle();
    	}
    	else {
    		cycle = new ChartCycleClient().getNext28DayCycle();
    	}    	
    	
    	return cycle;
    }
    
    /**
     * Get the product release information for the specified release cycle
     * @param cycle a publication release cycle
     * @return the product set object representing the charts in the specified release
     */
    public ProductSet getRelease (ChartCycleElementsJson cycle) {   
    	StringBuilder cifpPath = new StringBuilder();
    	cifpPath = cifpPath.append(Config.getCIFPPath()).append("/").append(Config.getCIFPFilePrefix());
    	// the path looks like this: /Upload_313-d/cifp/cifp_<year><cycle>.zip
    	
    	GregorianCalendar cal = new GregorianCalendar();
    	cal.setTime(cycle.getChart_effective_date());
    	cifpPath = cifpPath.append(cal.get(Calendar.YEAR)).append(cycle.getChart_cycle_number()).append(".zip");
    	
    	try {
    		downloadURL = new URL (Config.getAeronavHost()+cifpPath.toString());
    	}
    	catch (MalformedURLException emalformed) {
    		logger.warn("The download URL is not valid", emalformed);
    		return getErrorResponse (500, "Unable to construct a valid URL for the CIFP product release.");
    	}
    	
    	return buildResponse(cycle);
    	 	
    }
    
    @Override
    protected ProductSet buildResponse (ChartCycleElementsJson cycle) {
    	ProductSet response = getEdition(cycle);
    	Edition.Product product = new Edition.Product();
    	product.setProductName(ProductCodeList.CIFP);
    	
    	validateAndSetUrl(downloadURL.toExternalForm(), response, product);

    	response.getEdition().get(0).setProduct(product);

    	return response;
    }
    
    /**
     * Build a ProductSet response for the edition information. This does not include URLs to products and only includes the edition date 
     * and edition number. 
     * @param cycle - this is the edition cycle for which the edition was requested
     * @return
     */
    public ProductSet getEdition (ChartCycleElementsJson cycle) {
    	ProductSet response = initPositiveResponse();
    	ProductSet.Edition ed = initEdition(cycle);
    	response.getEdition().add(ed);
    	return response;   	
    }
}
