/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * STIG V-220631: Whitelist-based input validation filter.
 * Validates all query parameters against known-good patterns
 * before they reach endpoint resource methods.
 */
@Provider
public class InputValidationFilter implements ContainerRequestFilter {

    private static final int MAX_PARAM_LENGTH = 255;

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Skip validation for management and swagger endpoints
        if (path.startsWith("management") || path.startsWith("swagger")
                || path.startsWith("api-docs")) {
            return;
        }

        MultivaluedMap<String, String> params = requestContext.getUriInfo().getQueryParameters();
        if (params == null || params.isEmpty()) {
            return;
        }

        String clientIp = getClientIp();
        String method = requestContext.getMethod();

        // Validate edition parameter
        List<String> editions = params.get("edition");
        if (editions != null) {
            for (String edition : editions) {
                if (!InputValidator.validateEdition(edition)) {
                    AuditLogger.logInputValidationFailure(clientIp, method, path, "edition");
                    abortBadRequest(requestContext, "Invalid edition parameter");
                    return;
                }
            }
        }

        // Validate format parameter
        List<String> formats = params.get("format");
        if (formats != null) {
            for (String format : formats) {
                if (!InputValidator.validateFormat(format)) {
                    AuditLogger.logInputValidationFailure(clientIp, method, path, "format");
                    abortBadRequest(requestContext, "Invalid format parameter");
                    return;
                }
            }
        }

        // Validate geoname parameter (loose check — specific geoname validation
        // happens downstream in verifyGeoName() per chart type)
        List<String> geonames = params.get("geoname");
        if (geonames != null) {
            for (String geoname : geonames) {
                if (!InputValidator.validateGeonameLoose(geoname)) {
                    AuditLogger.logInputValidationFailure(clientIp, method, path, "geoname");
                    abortBadRequest(requestContext, "Invalid geoname parameter");
                    return;
                }
            }
        }

        // Validate volume parameter
        List<String> volumes = params.get("volume");
        if (volumes != null) {
            for (String volume : volumes) {
                if (!InputValidator.validateVolume(volume)) {
                    AuditLogger.logInputValidationFailure(clientIp, method, path, "volume");
                    abortBadRequest(requestContext, "Invalid volume parameter");
                    return;
                }
            }
        }

        // Reject any parameter that exceeds max length
        for (String key : params.keySet()) {
            List<String> values = params.get(key);
            if (values != null) {
                for (String value : values) {
                    if (value != null && value.length() > MAX_PARAM_LENGTH) {
                        AuditLogger.logInputValidationFailure(clientIp, method, path, key);
                        abortBadRequest(requestContext, "Parameter exceeds maximum length");
                        return;
                    }
                }
            }
        }
    }

    private void abortBadRequest(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
            Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"status\":{\"code\":400,\"message\":\"" + message + "\"}}")
                .type("application/json")
                .build());
    }

    private String getClientIp() {
        if (servletRequest == null) {
            return "unknown";
        }
        return servletRequest.getRemoteAddr();
    }
}
