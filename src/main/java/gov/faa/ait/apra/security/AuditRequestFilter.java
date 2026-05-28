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
 * STIG V-220635 (NIST AU-2, AU-3): Request/response audit logging filter.
 * Logs all API access with structured JSON entries including client IP,
 * HTTP method, request path, response status code, and request duration.
 */
@Provider
public class AuditRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_PROPERTY = "gov.faa.ait.apra.startTime";
    private static final String CLIENT_IP_PROPERTY = "gov.faa.ait.apra.clientIp";

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        requestContext.setProperty(START_TIME_PROPERTY, System.currentTimeMillis());
        requestContext.setProperty(CLIENT_IP_PROPERTY, ClientIpResolver.resolve(requestContext, servletRequest));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        String clientIp = (String) requestContext.getProperty(CLIENT_IP_PROPERTY);
        int statusCode = responseContext.getStatus();

        if (clientIp == null) {
            clientIp = ClientIpResolver.resolve(requestContext, servletRequest);
        }

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();

        Long startTime = (Long) requestContext.getProperty(START_TIME_PROPERTY);
        long durationMs = startTime != null ? System.currentTimeMillis() - startTime : -1;

        AuditLogger.getInstance().logAccess(clientIp, method, path, statusCode, durationMs);
    }

}
