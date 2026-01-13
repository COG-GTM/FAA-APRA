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

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS response filter that adds security headers to all responses.
 * Implements STIG V-220641 requirements for security headers.
 * 
 * @author FAA
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class SecurityHeadersFilter implements ContainerResponseFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityHeadersFilter.class);
    
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    private static final String X_FRAME_OPTIONS = "X-Frame-Options";
    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    private static final String X_XSS_PROTECTION = "X-XSS-Protection";
    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    private static final String REFERRER_POLICY = "Referrer-Policy";
    private static final String PERMISSIONS_POLICY = "Permissions-Policy";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String PRAGMA = "Pragma";
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
            ContainerResponseContext responseContext) throws IOException {
        
        if (!SecurityConfig.isSecurityHeadersEnabled()) {
            return;
        }
        
        try {
            addSecurityHeaders(responseContext);
        } catch (Exception e) {
            logger.error("Error adding security headers", e);
        }
    }
    
    /**
     * Adds all required security headers to the response.
     * 
     * @param responseContext the response context
     */
    private void addSecurityHeaders(ContainerResponseContext responseContext) {
        responseContext.getHeaders().putSingle(STRICT_TRANSPORT_SECURITY, 
            "max-age=31536000; includeSubDomains");
        
        responseContext.getHeaders().putSingle(X_FRAME_OPTIONS, "DENY");
        
        responseContext.getHeaders().putSingle(X_CONTENT_TYPE_OPTIONS, "nosniff");
        
        responseContext.getHeaders().putSingle(X_XSS_PROTECTION, "1; mode=block");
        
        responseContext.getHeaders().putSingle(CONTENT_SECURITY_POLICY, 
            SecurityConfig.getContentSecurityPolicy());
        
        responseContext.getHeaders().putSingle(REFERRER_POLICY, "strict-origin-when-cross-origin");
        
        responseContext.getHeaders().putSingle(PERMISSIONS_POLICY, 
            "geolocation=(), microphone=(), camera=()");
        
        responseContext.getHeaders().putSingle(CACHE_CONTROL, 
            "no-store, no-cache, must-revalidate, proxy-revalidate");
        
        responseContext.getHeaders().putSingle(PRAGMA, "no-cache");
        
        if (logger.isDebugEnabled()) {
            logger.debug("Security headers added to response");
        }
    }
}
