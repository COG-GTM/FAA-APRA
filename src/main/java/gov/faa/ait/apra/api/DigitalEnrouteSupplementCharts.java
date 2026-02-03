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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.faa.ait.apra.cycle.ChartCycleElementsJson;
import gov.faa.ait.apra.jaxb.ProductSet;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;


@Path("/ders")
@Tag(name="Digital Enroute Supplement (DERS)")

/**
 * As of June 2017, the DERS chart set has been discontinued. 
 * This chart set is no longer available.
 * @author FAA
 *
 */
public class DigitalEnrouteSupplementCharts extends BaseService {

	private static final Logger logger = LoggerFactory.getLogger(DigitalEnrouteSupplementCharts.class);
	
	/**
	 * This is the base chart download URL. A single parameter is provided to retrieve the URL for the current day's edition
	 * @return 404 not found as this has been deprecated
	 */
    @GET
    @Deprecated
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    @Path("/chart")
    @Operation(summary="Get Digital Enroute Supplement download link.", 
    	notes="The Digital Enroute Supplement release is deprecated and publication has been discontinued as of June 2017.",
    	response=ProductSet.class)
	@ApiResponses(value = {@ApiResponse(responseCode = "404", description = DEPRECATED)})
    
    public Response getDERSRelease (
    		@Parameter(name="edition", description="Requested product edition. If omitted, current edition is returned.") @QueryParam("edition") String ed) {
    	
    	logger.info("Received call to retrieve current DERS product release for edition.");
     	ProductSet ps = buildResponse(null);
    	return Response.status(ps.getStatus().getCode()).entity(ps).build();
    }

	/**
	 * This is the base chart download URL. A single parameter is provided to retrieve the URL for the current day's edition
	 * @return 404 not found as this has been deprecated
	 */
    @GET
    @Deprecated
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    @Path("/info")
    @Operation(summary="Get Digital Enroute Supplement edition information.", 
    	notes="The Digital Enroute Supplement release is deprecated and publication has been discontinued as of June 2017.",
    	response=ProductSet.class)
	@ApiResponses(value = {@ApiResponse(responseCode = "404", description = DEPRECATED)})
    
    public Response getDERSEdition (
    		@Parameter(name="edition", description="Requested product edition. If omitted, current edition is returned.") @QueryParam("edition") String ed) {
    	
     	ProductSet ps = buildResponse(null);
    	return Response.status(ps.getStatus().getCode()).entity(ps).build();
    } 
    
    
	@Override
	protected ProductSet buildResponse(ChartCycleElementsJson cycle) {
		return initDeprecatedResponse();
	}

}
