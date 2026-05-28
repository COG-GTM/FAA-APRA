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
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * STIG V-220631/V-220632 (NIST SI-10): Centralized input validation filter.
 * Validates and sanitizes all query parameters on incoming requests.
 * Rejects requests with parameters exceeding length limits or containing
 * dangerous characters.
 */
@Provider
public class InputValidationFilter implements ContainerRequestFilter {

    private static final int MAX_PARAM_LENGTH = 255;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        MultivaluedMap<String, String> params = requestContext.getUriInfo().getQueryParameters();
        String clientIp = getClientIp(requestContext);

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String paramName = entry.getKey();
            for (String paramValue : entry.getValue()) {
                if (paramValue != null && paramValue.length() > MAX_PARAM_LENGTH) {
                    AuditLogger.getInstance().logValidationFailure(
                        clientIp, paramName, "exceeds_max_length");
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Parameter value exceeds maximum allowed length\"}}")
                            .build());
                    return;
                }

                if (paramValue != null && containsDangerousChars(paramValue)) {
                    AuditLogger.getInstance().logSecurityViolation(
                        clientIp, "dangerous_chars_in_param:" + InputSanitizer.sanitizeForLog(paramName));
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Parameter contains invalid characters\"}}")
                            .build());
                    return;
                }

                if ("edition".equals(paramName) && !InputValidator.validateEdition(paramValue)) {
                    AuditLogger.getInstance().logValidationFailure(clientIp, "edition", paramValue);
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Invalid edition parameter\"}}")
                            .build());
                    return;
                }

                if ("format".equals(paramName) && !InputValidator.validateFormat(paramValue)) {
                    AuditLogger.getInstance().logValidationFailure(clientIp, "format", paramValue);
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Invalid format parameter\"}}")
                            .build());
                    return;
                }

                if ("geoname".equals(paramName) && !InputValidator.validateGeoname(paramValue)) {
                    AuditLogger.getInstance().logValidationFailure(clientIp, "geoname", paramValue);
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Invalid geoname parameter\"}}")
                            .build());
                    return;
                }

                if ("state".equals(paramName) && !InputValidator.validateState(paramValue)) {
                    AuditLogger.getInstance().logValidationFailure(clientIp, "state", paramValue);
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Invalid state parameter\"}}")
                            .build());
                    return;
                }

                if ("volume".equals(paramName) && !InputValidator.validateVolume(paramValue)) {
                    AuditLogger.getInstance().logValidationFailure(clientIp, "volume", paramValue);
                    requestContext.abortWith(
                        Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"status\":{\"code\":400,\"message\":\"Invalid volume parameter\"}}")
                            .build());
                    return;
                }
            }
        }
    }

    private boolean containsDangerousChars(String value) {
        for (char c : value.toCharArray()) {
            if (c == '<' || c == '>' || c == ';' || c == '&' || c == '|'
                || c == '`' || c == '$' || c == '(' || c == ')'
                || c == '\\' || c == '\0') {
                return true;
            }
        }
        return false;
    }

    private String getClientIp(ContainerRequestContext requestContext) {
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return "unknown";
    }
}
