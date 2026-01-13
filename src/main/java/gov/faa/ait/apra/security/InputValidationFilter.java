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
package gov.faa.ait.apra.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS request filter that validates and sanitizes all input parameters.
 * Implements STIG V-220631 and V-220632 requirements for input validation and sanitization.
 * 
 * @author FAA
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 100)
public class InputValidationFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(InputValidationFilter.class);
    
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            MultivaluedMap<String, String> queryParams = requestContext.getUriInfo().getQueryParameters();
            
            for (Map.Entry<String, java.util.List<String>> entry : queryParams.entrySet()) {
                String paramName = entry.getKey();
                for (String paramValue : entry.getValue()) {
                    if (InputValidator.containsDangerousCharacters(paramValue)) {
                        String clientIp = getClientIp(requestContext);
                        String path = requestContext.getUriInfo().getPath();
                        
                        Map<String, String> inputData = new HashMap<>();
                        inputData.put(paramName, paramValue);
                        
                        AuditLogger.getInstance().logInputValidationFailure(
                            clientIp, path, 
                            "Dangerous characters detected in parameter: " + paramName,
                            inputData);
                        
                        logger.warn("Input validation failed: dangerous characters in parameter {} from IP {}", 
                            InputSanitizer.sanitizeForLog(paramName),
                            InputSanitizer.sanitizeForLog(clientIp));
                        
                        Response response = Response.status(400)
                            .entity("{\"error\":\"Invalid input: parameter contains illegal characters\"}")
                            .type("application/json")
                            .build();
                        
                        requestContext.abortWith(response);
                        return;
                    }
                    
                    if (!InputValidator.validateBasicInput(paramValue, 1000)) {
                        String clientIp = getClientIp(requestContext);
                        String path = requestContext.getUriInfo().getPath();
                        
                        Map<String, String> inputData = new HashMap<>();
                        inputData.put(paramName, "[value too long or empty]");
                        
                        AuditLogger.getInstance().logInputValidationFailure(
                            clientIp, path, 
                            "Parameter exceeds maximum length: " + paramName,
                            inputData);
                        
                        Response response = Response.status(400)
                            .entity("{\"error\":\"Invalid input: parameter exceeds maximum length\"}")
                            .type("application/json")
                            .build();
                        
                        requestContext.abortWith(response);
                        return;
                    }
                }
            }
            
            validateSpecificParameters(requestContext, queryParams);
            
        } catch (Exception e) {
            logger.error("Error in input validation filter", e);
        }
    }
    
    /**
     * Validates specific known parameters with stricter rules.
     */
    private void validateSpecificParameters(ContainerRequestContext requestContext,
            MultivaluedMap<String, String> queryParams) {
        
        String geoname = queryParams.getFirst("geoname");
        if (geoname != null && !geoname.isEmpty()) {
            if (!InputValidator.validateGeoname(geoname)) {
                rejectInvalidParameter(requestContext, "geoname", geoname, 
                    "Invalid geoname format");
                return;
            }
        }
        
        String edition = queryParams.getFirst("edition");
        if (edition != null && !edition.isEmpty()) {
            if (!InputValidator.validateEdition(edition)) {
                rejectInvalidParameter(requestContext, "edition", edition, 
                    "Invalid edition value. Must be 'current' or 'next'");
                return;
            }
        }
        
        String format = queryParams.getFirst("format");
        if (format != null && !format.isEmpty()) {
            if (!InputValidator.validateFormat(format)) {
                rejectInvalidParameter(requestContext, "format", format, 
                    "Invalid format value. Must be 'pdf', 'tiff', or 'zip'");
                return;
            }
        }
    }
    
    /**
     * Rejects a request due to invalid parameter.
     */
    private void rejectInvalidParameter(ContainerRequestContext requestContext,
            String paramName, String paramValue, String reason) {
        
        String clientIp = getClientIp(requestContext);
        String path = requestContext.getUriInfo().getPath();
        
        Map<String, String> inputData = new HashMap<>();
        inputData.put(paramName, InputSanitizer.sanitizeForLog(paramValue));
        
        AuditLogger.getInstance().logInputValidationFailure(clientIp, path, reason, inputData);
        
        logger.warn("Input validation failed for parameter {}: {} from IP {}", 
            paramName, reason, InputSanitizer.sanitizeForLog(clientIp));
        
        Response response = Response.status(400)
            .entity("{\"error\":\"" + reason + "\"}")
            .type("application/json")
            .build();
        
        requestContext.abortWith(response);
    }
    
    /**
     * Extracts the client IP address from the request.
     */
    private String getClientIp(ContainerRequestContext requestContext) {
        String forwardedFor = requestContext.getHeaderString(X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            String[] ips = forwardedFor.split(",");
            return ips[0].trim();
        }
        
        String realIp = requestContext.getHeaderString(X_REAL_IP);
        if (realIp != null && !realIp.isEmpty()) {
            return realIp.trim();
        }
        
        return "unknown";
    }
}
