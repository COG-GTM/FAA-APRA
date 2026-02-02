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

import org.apache.commons.text.WordUtils;
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
import gov.faa.ait.apra.bootstrap.ErrorCodes;
import gov.faa.ait.apra.jaxb.ProductCodeList;
import gov.faa.ait.apra.jaxb.ProductSet;
import gov.faa.ait.apra.jaxb.ProductSet.Edition;
import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.cycle.TACCycleClient;
import gov.faa.ait.apra.util.TACSpecialCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vfr/tac")
@Tag(name = "Terminal Area Charts", description = "VFR Terminal Area Chart download and edition information")
/** 
 * This class is used to retrieve the TAC charts
 * @author FAA 
 *
 */

public class TerminalAreaCharts extends BaseService {
	private String city = "";
	private URL downloadURL = null;
	private static final Logger logger = LoggerFactory.getLogger(TerminalAreaCharts.class);

	@GetMapping(value = "/chart", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Terminal Area Chart download link",
		description = "Get Terminal Area Chart download link by edition, format, and geoname. TIFF formatted files are geo-referenced while PDF format is not geo-referenced."
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getTACRelease(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "Format of the requested chart", schema = @Schema(allowableValues = {"tiff", "pdf"}, defaultValue = "pdf"))
			@RequestParam(value = "format", required = false, defaultValue = "pdf") String fmt,
			@Parameter(description = "A US city for which the chart is requested", required = true)
			@RequestParam("geoname") String geo) {
    	ChartCycleElementsJson cycle;
    	
    	logger.info("Received call to retrieve current TAC product release for '{}', '{}', '{}'", ed, fmt, geo);
    	
    	setEdition(ed != null ? ed : CURRENT);
    	setFormat(fmt != null ? fmt : PDF);
    	setCity(geo != null ? geo: EMPTY_STRING);
    	
    	if (EMPTY_STRING.equals(geo)) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    			.body(getErrorResponse(400, "A geographic name (city) must be specified for TAC charts. Received a null city"));
    	}
    	
    	if (! verifyEdition() || ! verifyFormat()) {
    		logger.error("Received edition {} and format {}. Error response being generated and returned.", ed, fmt);
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getIllegalArgumentError());
    	}
    	   	
     	cycle = initParameters();
    	
     	if (cycle == null) {
     		logger.warn("Unable to locate {} edition chart for {}", this.getEdition(), this.getCity());
     		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getErrorResponse(404, ErrorCodes.ERROR_404));
     	}	
    	
    	return getRelease(cycle);
    }
   
	@GetMapping(value = "/info", produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
	@Operation(
		summary = "Get Terminal Area Chart edition information",
		description = "Get Terminal Area Chart edition date and edition number by edition type and geoname"
	)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = RESPONSE_200, content = @Content(schema = @Schema(implementation = ProductSet.class))),
		@ApiResponse(responseCode = "400", description = ERROR_400),
		@ApiResponse(responseCode = "404", description = ERROR_404),
		@ApiResponse(responseCode = "500", description = ERROR_500)
	})
	public ResponseEntity<ProductSet> getTACEdition(
			@Parameter(description = "Requested product edition", schema = @Schema(allowableValues = {"current", "next"}, defaultValue = "current"))
			@RequestParam(value = "edition", required = false, defaultValue = "current") String ed,
			@Parameter(description = "A US city for which the chart is requested", required = true)
			@RequestParam("geoname") String geo) {
    	ChartCycleElementsJson cycle;
    	
    	setEdition(ed != null ? ed : CURRENT);
    	setFormat(PDF);
    	setCity(geo != null ? geo: EMPTY_STRING);
    	   	
    	if (EMPTY_STRING.equals(geo)) {
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    			.body(getErrorResponse(400, "A geographic name (city) must be specified for TAC charts. Received a null city"));
    	}
    	
    	if (!verifyEdition()) {
    		logger.error("Expected edition current or next and received '{}' instead. Error response being generated and returned.", ed);
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(getIllegalArgumentError());
    	}
    	   	
     	if (CURRENT.equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new TACCycleClient().getCurrentCycle(this.getCity());
    	}
    	else {
    		cycle = new TACCycleClient().getNextCycle(this.getCity());
    	}
    	
     	if (cycle == null) {
     		logger.warn("Unable to locate {} edition chart for {}", this.getEdition(), this.getCity());
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(getErrorResponse(404, ErrorCodes.ERROR_404));
     	}

    	ProductSet ps = getEditionInfo(cycle);
    	return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
    }    
    
    public ResponseEntity<ProductSet> getRelease(ChartCycleElementsJson cycle) {   	
    	int specialCase;
    	StringBuilder tacPath = new StringBuilder(Config.getTACPath());
    	
    	if ("pdf".equalsIgnoreCase(getFormat())) {
    		tacPath = new StringBuilder(Config.getTACPdfPath());
    	}
    	   	
    	specialCase = TACSpecialCase.getSpecialCase(this.getCity());
    	
    	if (specialCase != -1) {
    		String fileName = TACSpecialCase.getTACFileName(specialCase, cycle.getChart_cycle_number(), this.getFormat());
    		tacPath = tacPath.append("/").append(fileName);
    	}
    	else { 
    		StringBuilder fileName = new StringBuilder(this.formatCity()+"_TAC_");
    		fileName.append(cycle.getChart_cycle_number());
    		if ("tiff".equalsIgnoreCase(getFormat())) { 
    			fileName.append(".zip");
    		}
    		else {
     			fileName.append("_P.pdf");
    		}
    		tacPath = tacPath.append("/").append(fileName.toString());
    	}
    	
    	try {
    		downloadURL = new URL (Config.getAeronavHost()+tacPath.toString());
    	}
    	catch (MalformedURLException emalformed) {
    		logger.warn("Unable to verify the download URL", emalformed);
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    			.body(getErrorResponse(500, "Unable to construct a valid URL for the TAC product release."));
    	}
    	
    	ProductSet ps = buildResponse(cycle);    	
    	return ResponseEntity.status(ps.getStatus().getCode()).body(ps);
    }
   
    @Override
    protected ProductSet buildResponse (ChartCycleElementsJson cycle) {
    	ProductSet response = getEditionInfo(cycle);
    	Edition.Product product = new Edition.Product();
    	product.setProductName(ProductCodeList.TAC);
    	validateAndSetUrl(downloadURL.toExternalForm(), response, product);
    	
    	response.getEdition().get(0).setProduct(product);
    	
    	return response;
    }
    
    
    /**
     * Build the TAC edition information based upon the given chart cycle passed to this method
     * @param cycle the chart cycle used to build the edition information
     * @return a ProductSet object that can be serialized as XML or JSON which contains the requested product edition information
     */    
    public ProductSet getEditionInfo (ChartCycleElementsJson cycle) {
    	ProductSet response = initPositiveResponse();
    	
    	ProductSet.Edition ed = initEdition(cycle);
    	ed.setGeoname(getCity());
    	
    	response.getEdition().add(ed);    	
    	return response;   	
    }
    
    /**
     * Set the city name and normalize it to follow punctuation and capitalization rules.
     * @param cityName the name of the city to be set and normalized
     */
    public void setCity (String cityName) {
    	
    	char[] separators = {'-', '_', ' '};
    	
    	if (cityName != null) {
    		this.city = cityName;
    		this.city = this.city.replace('_', ' ');
    		this.city = WordUtils.capitalizeFully(this.city, separators);
    	}
    	else {
    		this.city = "";
    	}
    	
    	
    	// This is a special case because the wordutils will change 'Puerto Rico-IV' into 'Puerto Rico-Iv' while all other strings work. Once again, the AJV5 file 
    	// naming convention strikes
    	if (this.city.startsWith("Puerto")) {
    		this.city = "Puerto Rico-VI";
    	}
    	setGeoname(this.city);
    }
    
    /**
     * Get the normalized city name for this specific request. The city should follow punctuation and capitalization rules if set correctly.
     * @return
     */
    public String getCity() {
    	return new String(this.city);
    }
    

    /**
     * This method exists because the TAC file names replace all space characters with the "_" character. I guess if someone sends in a city with underscores, that will be ok
     	There is also a special case where "Colorado Springs" is simply dropped from the filename on the URL. Finally, who knows what someone will send in, so I'm 
     	going to capitalize each word on the '_' delimeter. If I get junk, then I get junk. There is only so much cleaning I'm willing to do.
    	Strings like denver, baltimore-washington, and dallas-ft worth should work. 
     * @return a properly formatted city string capitalized as the web site produces it
     */
    public String formatCity() {
    	String scratch = this.getCity();
    	char[] separators = {'-', '_', ' '};
    	
    	String retVal = scratch.replace(" ",  "_");
    	retVal = WordUtils.capitalizeFully(retVal, separators);

		logger.info("Converted "+this.getCity()+" to "+retVal);

    	
    	if (logger.isDebugEnabled()) 
    		logger.debug("Converted "+this.getCity()+" to "+retVal);
    	
    	return retVal;
    }
    
    private ChartCycleElementsJson initParameters () {
    	ChartCycleElementsJson cycle;

     	if (CURRENT.equalsIgnoreCase(this.getEdition())) {   		
    		cycle = new TACCycleClient().getCurrentCycle(this.getCity());
    	}
    	else {
    		cycle = new TACCycleClient().getNextCycle(this.getCity());
    	}
    	
    	return cycle;
    }
	
}
