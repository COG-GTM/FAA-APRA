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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * STIG V-220629: Rate limiting filter to prevent brute-force
 * and denial-of-service attacks against the API.
 */
@Provider
public class RateLimitFilter implements ContainerRequestFilter {

    private static final RateLimiter rateLimiter = new RateLimiter();

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String clientIp = getClientIp();
        if (!rateLimiter.allowRequest(clientIp)) {
            String path = requestContext.getUriInfo().getRequestUri().getPath();
            AuditLogger.logRateLimitExceeded(clientIp, requestContext.getMethod(), path);
            requestContext.abortWith(
                Response.status(429)
                    .entity("{\"error\":\"Too many requests. Please try again later.\"}")
                    .header("Retry-After", "60")
                    .build());
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
