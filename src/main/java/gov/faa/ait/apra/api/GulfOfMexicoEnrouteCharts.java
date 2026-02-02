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
import java.text.SimpleDateFormat;
import java.util.Locale;

import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_400;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_404;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.ERROR_500;
import static gov.faa.ait.apra.bootstrap.ErrorCodes.RESPONSE_200;

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
import gov.faa.ait.apra.jaxb.ProductSet.Status;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.VFRChartCycleClient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/enroute/gom")
@Tag(name = "Gulf of Mexico IFR Enroute Chart", description = "Gulf of Mexico IFR Enroute chart download and edition information")
public class GulfOfMexicoEnrouteCharts extends BaseService {
	private String geoname;
	private URL westUrl = null;
	private URL centralUrl = null;
	private static final Logger logger = LoggerFactory.getLogger(GulfOfMexicoEnrouteCharts.class);
	private static final String WEST = "WEST";
	private static final String CENTRAL = "CENTRAL";
	private static final String ALL = "ALL";
	private static final String ERRMSG = " Error response being generated and returned.";
	
	public GulfOfMexicoEnrouteCharts() {}
	
	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Gulf of Mexico IFR enroute chart download link",
		description = "Get Gulf of Mexico IFR enroute chart edition date, edition number, and product download URL. The chart is distributed as a zip file."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getGOMRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt,
			@Parameter(description = "Requested Gulf of Mexico geographic area", schema = @Schema(allowableValues = {"west", "central"}))
			@RequestParam(value = "geoname", required = false) String geo) {

		ChartCycleElementsJson cycle;

		logger.info("Received call to retrieve current Gulf of Mexico IFR enroute product release {} {} {}", ed, fmt, geo);
		setFormat(fmt != null ? fmt.toUpperCase(Locale.ENGLISH) : PDF);
		setGeoname(geo != null ? geo : ALL);

		cycle = initParameters(ed);

		if (!verifyFormat()) {
			logger.error("Expected a format of PDF or TIFF, received format {} instead.", getFormat());
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		if (!verifyGeoname()) {
			logger.error("Expected geographic name of 'west' or 'central' and received '{}'{}", geoname, ERRMSG);
			return ResponseEntity.status(400).body(getErrorResponse(400,
					"Geographic area name must be either west, central, or omitted. If both charts are required, leave the geoname null"));
		}

		if (!verifyEdition()) {
			logger.error("Expected edition 'current' or 'next' and received '{}'{}", ed, ERRMSG);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		ProductSet ps = getRelease(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}

	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Gulf of Mexico IFR enroute chart edition information",
		description = "Get Gulf of Mexico IFR enroute chart edition date and edition number"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getGOMEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Requested Gulf of Mexico geographic area", schema = @Schema(allowableValues = {"west", "central"}))
			@RequestParam(value = "geoname", required = false) String geo) {

		ChartCycleElementsJson cycle;
		setGeoname(geo != null ? geo : ALL);
		setFormat(PDF);

		cycle = initParameters(ed);
		if (!verifyGeoname()) {
			logger.error("Expected geographic name of 'west' or 'central' and received '{}'{}", geoname, ERRMSG);
			return ResponseEntity.status(400).body(getErrorResponse(400,
					"Geographic area name must be either west, central, or omitted. If both charts are required, leave the geoname null"));
		}

		if (!verifyEdition()) {
			logger.error("Expected edition 'next' and received '{}'{}", ed, ERRMSG);
			return ResponseEntity.status(400).body(getIllegalArgumentError());
		}

		ProductSet ps = getEdition(cycle);
		return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
	}
    
    /**
     * Get the product release for the Gulf of Mexico charts.
     * @param cycle the publication cycle for the requested release
     * @return a ProductSet object that is serialized as JSON or XML to the caller
     */
    public ProductSet getRelease (ChartCycleElementsJson cycle) {   	
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
		String dateFolder = formatter.format(cycle.getChart_effective_date());
   	
    	logger.info("Building url paths for response for Gulf of Mexico IFR enroute chart type.");
    	
    	if (isWest()) {
    	// Gulf of Mexico WEST path
    		StringBuilder gomPathWest = new StringBuilder(Config.getGOMPath());
    		
			gomPathWest.append("/").append(dateFolder).append("/").append(getWestFileName());
    		
    		try {
    			// Verify the web gulf URL first
    			westUrl = new URL (Config.getAeronavHost()+gomPathWest.toString());
    			if (! verifyURL(westUrl)) {
    				logger.warn(westUrl.toExternalForm()+" returned a non 200 response code when completing a HTTP HEAD check.");
    				westUrl = null;
    			}
    		}
    		catch (MalformedURLException exWest) {
    			logger.warn("Unable to build a valid URL for the Gulf of Mexico west chart.", exWest);
    			westUrl = null;
    		}
    	}
    	
    	if (isCentral()) {
    		// Gulf of Mexico CENTRAL path
    		StringBuilder gomPathCentral = new StringBuilder(Config.getGOMPath());
    		gomPathCentral.append("/").append(dateFolder).append("/").append(getCentralFileName());   	    			
         	
    		try {
    			centralUrl = new URL (Config.getAeronavHost()+gomPathCentral.toString());
    			if (! verifyURL(centralUrl)) {
    				logger.warn(centralUrl.toExternalForm()+" returned a non 200 response code when completing a HTTP HEAD check.");
    				centralUrl = null;
    			}
    		}
    		catch (MalformedURLException exCentral) {
    			logger.warn("Unable to build a valid URL for the Gulf of Mexico central chart.", exCentral);
    		}
    	}
        
        if (westUrl == null && centralUrl == null) {
        	return getErrorResponse (404, ErrorCodes.ERROR_404);
        }
    	
    	return buildResponse(cycle);  	
    }
    
    /**
     * Build a ProductSet response for the edition information. This does not include URLs to products and only includes the edition date 
     * and edition number. 
     * @param cycle - this is the edition cycle for which the edition was requested
     * @return
     */
    public ProductSet getEdition (ChartCycleElementsJson cycle) {
    	logger.info("Building edition response for Gulf of Mexico IFR enroute chart type.");
     	ProductSet response = initPositiveResponse();    	
     	
    	if (isWest()) {
    		Edition ed = initEdition(cycle);
    		ed.setGeoname(WEST);
    		response.getEdition().add(ed);
    	}
    	
    	if (isCentral()) {
    		Edition ed = initEdition(cycle);
    		ed.setGeoname(CENTRAL);
    		response.getEdition().add(ed);   
    	}    	
    	
    	return response;   	
    }   
    
    @Override
    protected ProductSet buildResponse (ChartCycleElementsJson cycle) {
    	logger.info("Building chart response for Gulf of Mexico IFR enroute chart type using current URLs.");
    	ProductSet response = initPositiveResponse();
    	
    	if (isWest()) {
    		Edition edWest = initEdition(cycle);
    		edWest.setGeoname(WEST);
    		Edition.Product productWest = new Edition.Product();
    		productWest.setProductName(ProductCodeList.ENROUTE);
    	
    		if (westUrl != null) {  		
    			productWest.setUrl(westUrl.toExternalForm());     
    			edWest.setProduct(productWest);
    			response.getEdition().add(edWest);
    		}
    	}
    	
    	if (isCentral()) {
    		Edition edCentral = initEdition(cycle);
    		edCentral.setGeoname(CENTRAL);
    		Edition.Product productCentral = new Edition.Product();
    		productCentral.setProductName(ProductCodeList.ENROUTE);    	
    	
    		if (centralUrl != null) {
    			productCentral.setUrl(centralUrl.toExternalForm());
    			edCentral.setProduct(productCentral);
    			response.getEdition().add(edCentral);
    		}
    	}

    	response.setStatus(initStatus());
    	
    	return response;
    	
    }
    
    private Status initStatus () {
    	ObjectFactory of = new ObjectFactory();
    	
    	Status status = of.createProductSetStatus();
    	status.setCode(200);
    	status.setMessage("OK");
    	return status;
    }
    
    private ChartCycleElementsJson initParameters (String ed) {
    	ChartCycleElementsJson cycle;
    	
    	setEdition(ed != null ? ed : CURRENT);
    	
    	if ("current".equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new VFRChartCycleClient("IFR_PGOM").getCurrentCycle();
    	}
    	else {
    		cycle = new VFRChartCycleClient("IFR_PGOM").getNextCycle();
    	}    	
    	
    	return cycle;
    }
    
    private boolean isWest () {
    	if (ALL.equals(getGeoname())) 
    		return true;
    	
    	return WEST.equalsIgnoreCase(getGeoname());
    }
    
    private boolean isCentral () {
    	if (ALL.equals(getGeoname()))
    		return true;
    	
    	return CENTRAL.equalsIgnoreCase(getGeoname());
    }
    
    private boolean verifyGeoname () {
    	return isWest() || isCentral();
    }
    
    
    private String getCentralFileName() {
		if ( TIFF.equalsIgnoreCase(getFormat()) ) {
			return Config.getGOMCentralTIFFFile();   	    			
		}
		else {
			return Config.getGOMCentralPDFFile();   	    			    			
		}    	    	
    }
    
    private String getWestFileName() {
		if ( TIFF.equalsIgnoreCase(getFormat()) ) {
			return Config.getGOMWestTIFFFile();   	    			
		}
		else {
			return Config.getGOMWestPDFFile();   	    			    			
		}    	    	
    }
}
