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
import java.util.Date;

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
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.util.CycleDateUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/nfdc/nasr")
@Tag(name = "NASR 28 Day Subscription", description = "NASR subscription download and edition information")
/** 
 * This class is used to retrieve the NASR 56 day subscription file
 *
 * @author FAA
 */
public class NASRSubscription extends BaseService {
	private static final Logger logger = LoggerFactory.getLogger(NASRSubscription.class);
	private static final String ERROR = " Error response being generated and returned.";
	private Date fromDate;
	private Date toDate;

	public NASRSubscription() { } 
	
	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get the NASR 28 day subscription file",
		description = "Get the National Flight Data Center NASR 28 day subscription file download link"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getNASRSubscription(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {
		logger.info("Received call to retrieve current NFDC NASR subscription release for {}", ed);

		setEdition(ed != null ? ed : CURRENT);
		setFormat("zip");

		if (!verifyEdition()) {
			logger.error("Expected edition current or next and received {}{}", ed, ERROR);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		ChartCycleElementsJson cycle = initParameters();

		ProductSet ps = buildResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get the NASR 28 day subscription file edition information",
		description = "Get the National Flight Data Center NASR 28 day subscription file edition information"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getNASREdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed) {
		logger.info("Received call to retrieve current NASR subscription Chart release for {}", ed);

		setEdition(ed != null ? ed : CURRENT);
		setFormat("zip");

		if (!verifyEdition()) {
			logger.error("Expected edition current or next and received {}{}", ed, ERROR);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		ChartCycleElementsJson cycle = initParameters();
		ProductSet response = initPositiveResponse();
		response.getEdition().add(initEdition(cycle));

		return ResponseEntity.status(response.getStatus().getCode()).body(response);
	}
	
    // https://nfdc.faa.gov/webContent/28DaySub/28DaySubscription_Effective_2017-08-17.zip
    //
	// https://nfdc.faa.gov/webContent/56DaySub/56DySubscription_May_26__2016_-_July_21__2016.zip
    // To the extent possible, we parameterize this path so it can be adjusted without code changes
    // the nfdc host, context path, file prefix, and date formats are all in configuration properties
    // read from disk. The properties used are delivered by Ansible during deployment and read upon 
    // first use
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
    	ObjectFactory of = new ObjectFactory();
    	ProductSet response = initPositiveResponse();
    	
    	ProductSet.Edition ed = initEdition(cycle);
    	Edition.Product product = of.createProductSetEditionProduct();
    	product.setProductName(ProductCodeList.SUBSCRIBER);
    	
    	StringBuilder path = new StringBuilder(Config.getNFDCHost()).append(Config.getNfdcNasrPath());

    	StringBuilder fileName = new StringBuilder("/").append(Config.getNASRFilePrefix());
    	fileName = fileName.append(formatDate(fromDate, Config.getNASRDateFormat())).append(".zip");

    	path = path.append(fileName);
    	
    	if (logger.isInfoEnabled())
    		logger.info("NASR susbscriber file URL created: "+path.toString());
   	
    	try {
    		URL url = new URL(path.toString());
    		
    		if (Config.getNASRCheckFlag())
    			verifyURL(url);
    		
        	product.setUrl(path.toString());

        }
    	catch (Exception exurl) {
    		logger.error("Unable to verify the download url "+path.toString(), exurl);
    		product.setUrl("");
        	response.getStatus().setCode(404);
        	response.getStatus().setMessage(ErrorCodes.ERROR_404);
        }

    	ed.setProduct(product);
    	response.getEdition().add(ed);
    	
		return response;
	}
	
    private ChartCycleElementsJson initParameters () {
    	ChartCycleElementsJson cycle;
    	   	
    	if (CURRENT.equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new ChartCycleClient().getCurrent28DayCycle();
    		fromDate = cycle.getChart_effective_date();
    		toDate = new CycleDateUtil().getNext28Day();
    	}
    	else {
    		cycle = new ChartCycleClient().getNext28DayCycle();
    		fromDate = cycle.getChart_effective_date();
    		toDate = new CycleDateUtil().get28DayCycleDate(2);
    	}    	
    	
    	return cycle;
    }

    private String formatDate (Date unformattedDate, String format) {
    	SimpleDateFormat formatter = new SimpleDateFormat(format);
    	return formatter.format(unformattedDate);
    }
    
}
