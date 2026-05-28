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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * STIG V-220635 (NIST AU-2, AU-3): Request/response audit logging filter.
 * Logs all API access with structured JSON entries including client IP,
 * HTTP method, request path, and response status code.
 */
@Provider
public class AuditRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "gov.faa.ait.apra.startTime";
    private static final String CLIENT_IP_PROPERTY = "gov.faa.ait.apra.clientIp";
    private static final String REQUEST_PATH_PROPERTY = "gov.faa.ait.apra.requestPath";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
        requestContext.setProperty(CLIENT_IP_PROPERTY, getClientIp(requestContext));
        requestContext.setProperty(REQUEST_PATH_PROPERTY,
            requestContext.getMethod() + " " + requestContext.getUriInfo().getPath());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        String clientIp = (String) requestContext.getProperty(CLIENT_IP_PROPERTY);
        String requestPath = (String) requestContext.getProperty(REQUEST_PATH_PROPERTY);
        int statusCode = responseContext.getStatus();

        if (clientIp == null) {
            clientIp = "unknown";
        }
        if (requestPath == null) {
            requestPath = "unknown";
        }

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        AuditLogger.getInstance().logAccess(clientIp, method, path, statusCode);
    }

    private String getClientIp(ContainerRequestContext requestContext) {
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return "unknown";
    }
}
