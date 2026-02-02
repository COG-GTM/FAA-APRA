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
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gov.faa.ait.apra.bootstrap.Config;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ChangeCodeList;
import gov.faa.ait.apra.jaxb.EditionCodeList;
import gov.faa.ait.apra.jaxb.FormatCodeList;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/ddof")
@Tag(name = "Daily Digital Obstacle File (DDOF)", description = "Daily Digital Obstacle File download and edition information")

public class DailyDigitalObstacleFile extends BaseService {
	private static final Logger logger = LoggerFactory.getLogger(DailyDigitalObstacleFile.class);

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Daily Digital Obstacle File download link",
		description = "The Daily Digital Obstacle File release is distributed as a zip file containing the latest obstacle information from the FAA database."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getDDOFRelease() {
		logger.info("Received call to retrieve current DDOF product release for edition.");
		setFormat(ZIP);
		ProductSet ps = getRelease();
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Daily Digital Obstacle File edition information",
		description = "The Daily Digital Obstacle File is released by the FAA on a daily basis."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getDDOFEdition() {
		logger.info("Received call to retrieve current DDOF edition information.");
		setFormat("ZIP");
		ProductSet response = initPositiveResponse();
		ProductSet.Edition ed = initEdition(null);
		response.getEdition().add(ed);

		ed = initEdition(null);
		response.getEdition().add(ed);

		return ResponseEntity.status(response.getStatus().getCode()).body(response);
	}    
    
	private ProductSet getRelease() {
		return buildResponse(null);
	}

	// http://tod.faa.gov/tod/DOF_DAILY_CHANGE_UPDATE.ZIP
	
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
    	ProductSet response = initPositiveResponse();
    	
    	addProduct(Config.getDDOFFile(), response);
    	addProduct(Config.getDDOFDailyChangeFile(), response);
    	
    	return response;
	}
	
	private void addProduct (String name, ProductSet ps) {
		StringBuilder downloadURL = new StringBuilder();
		ProductSet.Edition ed = initEdition(null);
		Edition.Product product = new Edition.Product();
		product.setProductName(ProductCodeList.DDOF);
		product.setChange(ChangeCodeList.CHANGED);
    	downloadURL.append(Config.getDDOFHost()).append(Config.getDDOFPath()).append("/").append(name);
    	try {
    		product.setChartName(name);
    		URL url = new URL (downloadURL.toString());
    		product.setUrl(url.toString());
        }
       	catch (MalformedURLException emalformed) {
    		logger.warn("The DDOF download URL is not valid", emalformed);
    		product.setUrl("");
     	}		
        ed.setProduct(product);
        ps.getEdition().add(ed);   
        
    	return;
	}
	
	@Override
    protected Edition initEdition (ChartCycleElementsJson cycle) {
		Date today = new Date (System.currentTimeMillis());
    	ObjectFactory of = new ObjectFactory();

    	ProductSet.Edition ed = of.createProductSetEdition();
	   	
    	SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    	ed.setEditionDate(formatter.format(today));
    	ed.setEditionNumber(1);
    	ed.setEditionName(EditionCodeList.DAILY);
    	ed.setFormat(FormatCodeList.fromValue(getFormat()));
    	ed.setGeoname("US");
    	   	
    	return ed;
    }

}
