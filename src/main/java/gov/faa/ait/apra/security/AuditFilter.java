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
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS filter that logs all API requests and responses for audit purposes.
 * Implements STIG V-220635 requirements for audit logging.
 * 
 * @author FAA
 */
@Provider
@Priority(Priorities.USER)
public class AuditFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditFilter.class);
    
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String USER_AGENT = "User-Agent";
    private static final String REQUEST_START_TIME = "audit.request.start.time";
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!SecurityConfig.isAuditLoggingEnabled()) {
            return;
        }
        
        try {
            requestContext.setProperty(REQUEST_START_TIME, System.currentTimeMillis());
            
            String clientIp = getClientIp(requestContext);
            String method = requestContext.getMethod();
            String path = requestContext.getUriInfo().getPath();
            String userAgent = requestContext.getHeaderString(USER_AGENT);
            
            AuditLogger.getInstance().logApiRequest(clientIp, method, path, userAgent);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Audit: {} {} from {}", method, 
                    InputSanitizer.sanitizeForLog(path), 
                    InputSanitizer.sanitizeForLog(clientIp));
            }
        } catch (Exception e) {
            logger.error("Error in audit request filter", e);
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext, 
            ContainerResponseContext responseContext) throws IOException {
        if (!SecurityConfig.isAuditLoggingEnabled()) {
            return;
        }
        
        try {
            String clientIp = getClientIp(requestContext);
            String method = requestContext.getMethod();
            String path = requestContext.getUriInfo().getPath();
            int statusCode = responseContext.getStatus();
            
            long durationMs = 0;
            Object startTime = requestContext.getProperty(REQUEST_START_TIME);
            if (startTime instanceof Long) {
                durationMs = System.currentTimeMillis() - (Long) startTime;
            }
            
            AuditLogger.getInstance().logApiResponse(clientIp, method, path, statusCode, durationMs);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Audit: {} {} -> {} ({}ms)", method, 
                    InputSanitizer.sanitizeForLog(path), statusCode, durationMs);
            }
        } catch (Exception e) {
            logger.error("Error in audit response filter", e);
        }
    }
    
    /**
     * Extracts the client IP address from the request.
     * 
     * @param requestContext the request context
     * @return the client IP address
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
