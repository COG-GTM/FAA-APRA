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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS request filter that implements rate limiting.
 * Implements STIG V-220636 requirements for monitoring and abuse prevention.
 * 
 * @author FAA
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class RateLimitFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String X_RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
    private static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
    private static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";
    private static final String RETRY_AFTER = "Retry-After";
    
    private final Set<String> exemptIps;
    
    public RateLimitFilter() {
        this.exemptIps = new HashSet<>();
        String exemptIpList = SecurityConfig.getRateLimitExemptIps();
        if (exemptIpList != null && !exemptIpList.isEmpty()) {
            Arrays.stream(exemptIpList.split(","))
                .map(String::trim)
                .filter(ip -> !ip.isEmpty())
                .forEach(exemptIps::add);
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!SecurityConfig.isRateLimitingEnabled()) {
            return;
        }
        
        String clientIp = getClientIp(requestContext);
        
        if (isExempt(clientIp)) {
            if (logger.isDebugEnabled()) {
                logger.debug("IP {} is exempt from rate limiting", 
                    InputSanitizer.sanitizeForLog(clientIp));
            }
            return;
        }
        
        RateLimiter rateLimiter = RateLimiter.getInstance();
        
        if (!rateLimiter.isAllowed(clientIp)) {
            String path = requestContext.getUriInfo().getPath();
            
            AuditLogger.getInstance().logRateLimitExceeded(
                clientIp, path, rateLimiter.getMaxRequests());
            
            long resetTime = rateLimiter.getResetTimeSeconds(clientIp);
            
            Response response = Response.status(429)
                .entity("{\"error\":\"Too many requests. Please try again later.\"}")
                .header(X_RATE_LIMIT_LIMIT, rateLimiter.getMaxRequests())
                .header(X_RATE_LIMIT_REMAINING, 0)
                .header(X_RATE_LIMIT_RESET, resetTime)
                .header(RETRY_AFTER, resetTime)
                .type("application/json")
                .build();
            
            requestContext.abortWith(response);
            return;
        }
        
        requestContext.setProperty(X_RATE_LIMIT_LIMIT, rateLimiter.getMaxRequests());
        requestContext.setProperty(X_RATE_LIMIT_REMAINING, 
            rateLimiter.getRemainingRequests(clientIp));
        requestContext.setProperty(X_RATE_LIMIT_RESET, 
            rateLimiter.getResetTimeSeconds(clientIp));
    }
    
    /**
     * Extracts the client IP address from the request.
     * Checks X-Forwarded-For and X-Real-IP headers for proxied requests.
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
    
    /**
     * Checks if an IP address is exempt from rate limiting.
     * 
     * @param ip the IP address to check
     * @return true if the IP is exempt
     */
    private boolean isExempt(String ip) {
        return ip != null && exemptIps.contains(ip);
    }
}
