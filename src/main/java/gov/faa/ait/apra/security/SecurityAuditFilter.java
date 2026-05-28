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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * STIG V-220635: Audit logging filter for all API requests and responses.
 * Logs every request with client IP, method, path, and response status
 * in structured JSON format per NIST AU-2 and AU-3.
 */
@Provider
public class SecurityAuditFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "gov.faa.ait.apra.request.startTime";

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());

        String clientIp = getClientIp();
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getRequestUri().getPath();

        AuditLogger.log("request_received", clientIp, method, path, "success", null);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String clientIp = getClientIp();
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getRequestUri().getPath();
        int statusCode = responseContext.getStatus();

        Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

        AuditLogger.logAccess(clientIp, method, path, statusCode);

        if (statusCode == 401 || statusCode == 403) {
            AuditLogger.logSecurityViolation(clientIp, method, path,
                "unauthorized_access_attempt status=" + statusCode);
        }
    }

    private String getClientIp() {
        if (servletRequest == null) {
            return "unknown";
        }
        String forwarded = servletRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }
}
