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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;

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
import gov.faa.ait.apra.bootstrap.USStateReferenceData;
import gov.faa.ait.apra.jaxb.ChangeCodeList;
import gov.faa.ait.apra.jaxb.FormatCodeList;
import gov.faa.ait.apra.jaxb.ObjectFactory;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.jaxb.ProductSet.Edition.Product;
import gov.faa.ait.apra.json.TPPMetadata;

import gov.faa.ait.apra.cycle.ChartCycleClient;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;

import gov.faa.ait.apra.util.TPPMetadataClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dtpp")
@Tag(name = "US Terminal Procedures Publication (TPP)", description = "Terminal Procedures Publication chart download and edition information")
/**
 * This class services requests for the digital terminal procedures publication. Currently, the allowed publication sets are US complete set and state complete set. If a changeset parameter is specified,
 * the service responds with only charts that have changed since the previous release of dTPP
 * @author FAA
 *
 */
public class TerminalProcedureCharts extends BaseService {
	private static final Logger logger = LoggerFactory.getLogger(TerminalProcedureCharts.class);
	private static final String US = "US";

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Terminal Procedure Publication chart download information",
		description = "Get Terminal Procedure Publication chart download information by requesting an edition with geographic area of United States or a valid US State Name. The complete United States Terminal Procedure Publication (TPP) release is distributed as a set of zip files containing charts and verification software."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getTPPRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next", "changeset"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Requested geographic region of Terminal Procedures Publication chart set")
			@RequestParam(value = "geoname", required = false, defaultValue = "US") String geo) {
		ChartCycleElementsJson cycle;

		logger.info("Received call to retrieve current TPP product release for edition '{}'.", ed);

		setGeoname(geo != null ? geo : US);
		setFormat(ZIP);

		if (!verifyGeoname()) {
			logger.error("Expected a geographic name of a US state or just US, but received '{}' instead.", geo);
			return ResponseEntity.status(400).body(getErrorResponse(400, "Geographic name must be a full US state name or 'US'"));
		}
		setEdition(ed != null ? ed : CURRENT);
		cycle = initParameters();

		if (!verifyEdition()) {
			logger.error("Expected edition current, next, or changeset and received '{}' instead.", ed);
			return ResponseEntity.status(400).body(getErrorResponse(400, "Edition must be current, next, or changeset."));
		}

		if ((isChangeFlag() && isUnitedStates()) || (!isUnitedStates())) {
			logger.info("Retrieving individual TPP charts rather than full US set. User asked for a state, volume, or US changes.");
			setFormat(PDF);
			ProductSet ps = getChartProductSet(cycle);
			return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
		}

		ProductSet ps = buildResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Terminal Procedure Publication chart edition information",
		description = "Get Terminal Procedure Publication chart edition information by requesting an edition with geographic area of United States or one of the 50 US states"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getTPPEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Requested geographic region of Terminal Procedures Publication chart set")
			@RequestParam(value = "geoname", required = false, defaultValue = "US") String geo) {
		ChartCycleElementsJson cycle;

		logger.info("Received call to retrieve current TPP product release for edition '{}'.", ed);

		setGeoname(geo != null ? geo : US);
		setEdition(ed != null ? ed : CURRENT);

		if (US.equalsIgnoreCase(getGeoname())) {
			setFormat(ZIP);
		} else {
			setFormat(PDF);
		}

		cycle = initParameters();

		if (!verifyGeoname()) {
			logger.error("Expected a geographic name of a US state or just US, but received '{}' instead.", geo);
			return ResponseEntity.status(400).body(getErrorResponse(400, "Geographic name must be a full US state name or 'US'"));
		}

		if (!verifyEdition()) {
			logger.error("Expected edition 'current' or 'next' and received '{}' instead.", ed);
			return ResponseEntity.status(400).body(getErrorResponse(400, "Edition must be current, next, or changeset."));
		}

		ProductSet ps = getEditionResponse(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
    
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
    	if ("US".equalsIgnoreCase(getGeoname())) {
    		return getUSProductSet(cycle);
    	} 
    	
    	else {
    		return getChartProductSet(cycle);
    	}
	}
    
    private ProductSet getUSProductSet (ChartCycleElementsJson cycle) {
    	ObjectFactory of = new ObjectFactory();
    	String [] pathSet = getUSFilePaths(cycle);
    	ProductSet ps = initPositiveResponse();
    	
    	for (int i = 0; i < pathSet.length; i++) {
    		Edition ed = initEdition(cycle);
        	Product product = of.createProductSetEditionProduct();
        	product.setProductName(ProductCodeList.TPP);        	
        	validateAndSetUrl(Config.getAeronavHost()+pathSet[i], ps, product);	
        	ed.setProduct(product);
        	ps.getEdition().add(ed);
        }
    	
    	return ps;
    }
    
    // Chart paths follow this convention http://aeronav.faa.gov/d-tpp/1607/akto.pdf   
    
    private ProductSet getChartProductSet (ChartCycleElementsJson cycle) {
    	logger.info("Getting the chart product set for "+getEdition()+" "+capitalizeGeoname()+" with change flag = "+isChangeFlag());
    	ObjectFactory of = new ObjectFactory();
    	TPPMetadataClient tppClient = new TPPMetadataClient (cycle, isChangeFlag()); 
    	TPPMetadata [] elements = tppClient.getChartMetadataByState(capitalizeGeoname()).getElements();
    	HashSet <String> processedFiles = new HashSet <> ();
    	int processTotal = 0;
    	
    	if (elements == null || elements.length == 0) 
    		return getErrorResponse(404, ErrorCodes.ERROR_404);
    	
    	logger.info(elements.length+" total charts found for "+getEdition()+" "+capitalizeGeoname()+" with change flag = "+isChangeFlag());
    	
    	String edition = tppClient.getEdition();
    	ProductSet ps = initPositiveResponse();
    	
    	for (int i = 0; i < elements.length; i++) {
    		if (processedFiles.contains(elements[i].getChart_name())) {
    			//skip the chart if we've already processed it
    			continue;
    		}
    		else {
    			// add the chart to our processed list and we build the response
    			processedFiles.add(elements[i].getChart_name());
    			processTotal++;
    		}
    		
    		StringBuilder path = new StringBuilder(Config.getTPPChartPath());
    		Edition ed = initEdition(cycle);
    		ed.setFormat(FormatCodeList.PDF);
    		ed.setGeoname(elements[i].getState_fullname());
    		ed.setVolume(elements[i].getVolume());

        	Product product = of.createProductSetEditionProduct();      	
        	product.setProductName(ProductCodeList.TPP);
        	product.setChartName(elements[i].getChart_name());   
        	
        	if (! isNullValue(elements[i].getAirport_icao_identifier()))
        		product.setIcao(elements[i].getAirport_icao_identifier());
        	
        	if (! isNullValue(elements[i].getAirport_identifier()))
        		product.setAirportId(elements[i].getAirport_identifier());
        	
        	if (! isNullValue(elements[i].getCity_name()))
        		product.setCityName(elements[i].getCity_name());
        	
        	if (! isNullValue(elements[i].getAirport_name())) 
        		product.setAirportName(elements[i].getAirport_name());
        	
    		path.append("/").append(edition);
    		path.append("/").append(elements[i].getPdf_name());
    		
    		product.setUrl(Config.getAeronavHost()+path.toString());
    		
        	setChangeType(product, elements[i].getUseraction());
    		
    		// The HEAD check for TPP files can introduce a significant performance penalty. This is controlled by a flag in the Configuration. 
    		// Recommendation is to enable the flag in DEV only and leave disabled in TEST and PROD unless someone wants to check and verify in TEST
    		
    		if (Config.getTPPCheckFlag()) {
    			logger.warn("URL validation check is enabled for the DTPP product set. This can cause serious performance issues for the DTTP product responses."
    					+ " Consider changing the configuration parameter gov.faa.ait.tpp.check.flag = false and re-deploy.");
    			validateAndSetUrl(Config.getAeronavHost()+path.toString(), ps, product);
    		}
   		
           	ed.setProduct(product);
        	ps.getEdition().add(ed);
    	}
    	
    	processedFiles.clear();
    	logger.info("Processed a total of "+processTotal+" charts for "+this.getGeoname());
    	
       	return ps;
    }   
    
    // This is where we get the full US product set file path that is divided into 5 separate ZIP files for download. The files are named A through E
    private String [] getUSFilePaths (ChartCycleElementsJson cycle) {
    	String [] usPathSet = new String [5];
    	char [] filePart = { 'A', 'B', 'C', 'D', 'E' };
    	
    	GregorianCalendar cal = new GregorianCalendar();
    	cal.setTime(cycle.getChart_effective_date());
    	String year = Integer.toString(cal.get(Calendar.YEAR));
    	   	
    	for (int i = 0; i < filePart.length; i++) {	
    		StringBuilder path = new StringBuilder(Config.getTPPUSPath());
    		StringBuilder fileName = new StringBuilder(Config.getTPPUSPrefix()).append(filePart[i]).append("_").append(year).append(cycle.getChart_cycle_number()).append(".zip");
    		path.append("/").append(fileName);
    		usPathSet[i] = path.toString();
    	}
    	
    	return usPathSet;
    }
    
    private ChartCycleElementsJson initParameters () {
    	ChartCycleElementsJson cycle;
    	   	
    	if (CURRENT.equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new ChartCycleClient().getCurrent28DayCycle();
    	}
    	else {
    		cycle = new ChartCycleClient().getNext28DayCycle();
    	}    	
    	
    	return cycle;
    }
    
    /**
     * Get the edition information response using the specified chart cycle retrieved from the chart cycle resource
     * @param cycle the chart cycle of either current or next 28 day cycle
     * @return
     */
    public ProductSet getEditionResponse (ChartCycleElementsJson cycle) {
    	ProductSet response = initPositiveResponse();
    	response.getEdition().add(initEdition(cycle));
    	return response;   	
    }
    
    private void setChangeType (Product p, String code) {
    	   	
    	if (code == null || "".equals(code)) {
    		// perhaps a "NONE" code should be added? For now, we'll leave it off entirely 
    		return;
    	}

    	if ("C".equalsIgnoreCase(code)) {
    		p.setChange(ChangeCodeList.CHANGED);
    	}
    	else if ("D".equalsIgnoreCase(code)) {
    		p.setChange(ChangeCodeList.DELETED);
    		p.setUrl("");
    	}
    	else if ("A".equalsIgnoreCase(code)) {
    		p.setChange(ChangeCodeList.ADDED);
    		return;
    	}
    }
    
    private boolean isUnitedStates () {
    	return US.equalsIgnoreCase(getGeoname());
    }
    
    private boolean verifyGeoname () {   	
    	if ("US".equalsIgnoreCase(getGeoname())) {
    		return true;
    	}
    		
    	return USStateReferenceData.stateNameExists(getGeoname());   	
    }

    private boolean isNullValue (String value) {
    	
    	return value == null || value.equals(EMPTY_STRING);

    }
    
    @Override
	protected void validateAndSetUrl(String url, ProductSet ps, Product p) {
		try {
			URL downloadURL = new URL(url);
			if (!verifyURL(downloadURL)) {
				logger.warn(downloadURL.toExternalForm()
						+ " returned a non 200 response code when completing a HTTP HEAD check.");
				p.setUrl("");
			} else {
				p.setUrl(downloadURL.toExternalForm());
			}
		} catch (MalformedURLException emalformed) {
			logger.warn("The download URL is not valid", emalformed);
			p.setUrl("");
		}
	}
}
