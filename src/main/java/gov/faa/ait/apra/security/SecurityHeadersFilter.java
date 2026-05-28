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
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * STIG V-220641: Security headers on all HTTP responses.
 * Implements HSTS, X-Frame-Options, X-Content-Type-Options,
 * Content-Security-Policy, and cache control headers per
 * NIST SI-11 requirements.
 */
@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().putSingle(
            "Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        responseContext.getHeaders().putSingle(
            "X-Frame-Options", "DENY");
        responseContext.getHeaders().putSingle(
            "X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().putSingle(
            "X-XSS-Protection", "1; mode=block");
        responseContext.getHeaders().putSingle(
            "Content-Security-Policy", "default-src 'self'");
        responseContext.getHeaders().putSingle(
            "Cache-Control", "no-store, no-cache, must-revalidate");
        responseContext.getHeaders().putSingle(
            "Pragma", "no-cache");
        responseContext.getHeaders().putSingle(
            "X-Permitted-Cross-Domain-Policies", "none");
        responseContext.getHeaders().putSingle(
            "Referrer-Policy", "strict-origin-when-cross-origin");
    }
}
