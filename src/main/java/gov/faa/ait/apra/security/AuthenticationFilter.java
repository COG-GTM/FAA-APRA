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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS request filter that implements API key authentication.
 * Implements STIG V-220629 requirements for authentication.
 * 
 * Note: Authentication is disabled by default for backward compatibility.
 * Enable via security.auth.enabled=true in security.properties.
 * 
 * @author FAA
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    
    private final ConcurrentHashMap<String, FailedAttemptInfo> failedAttempts;
    
    public AuthenticationFilter() {
        this.failedAttempts = new ConcurrentHashMap<>();
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!SecurityConfig.isAuthenticationEnabled()) {
            return;
        }
        
        String clientIp = getClientIp(requestContext);
        
        if (isLockedOut(clientIp)) {
            logger.warn("Locked out IP {} attempted access", 
                InputSanitizer.sanitizeForLog(clientIp));
            
            Response response = Response.status(429)
                .entity("{\"error\":\"Account temporarily locked due to too many failed attempts\"}")
                .type("application/json")
                .build();
            
            requestContext.abortWith(response);
            return;
        }
        
        String apiKeyHeader = SecurityConfig.getApiKeyHeaderName();
        String apiKey = requestContext.getHeaderString(apiKeyHeader);
        
        if (apiKey == null || apiKey.isEmpty()) {
            handleAuthFailure(requestContext, clientIp, "Missing API key");
            return;
        }
        
        if (!validateApiKey(apiKey)) {
            handleAuthFailure(requestContext, clientIp, "Invalid API key");
            return;
        }
        
        clearFailedAttempts(clientIp);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Authentication successful for IP {}", 
                InputSanitizer.sanitizeForLog(clientIp));
        }
    }
    
    /**
     * Validates the provided API key.
     * In a production environment, this would check against a secure key store.
     * 
     * @param apiKey the API key to validate
     * @return true if the API key is valid
     */
    private boolean validateApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 32) {
            return false;
        }
        
        String expectedKeyHash = System.getenv("APRA_API_KEY_HASH");
        if (expectedKeyHash == null || expectedKeyHash.isEmpty()) {
            logger.warn("APRA_API_KEY_HASH environment variable not set. " +
                "Authentication will fail for all requests.");
            return false;
        }
        
        String providedKeyHash = hashApiKey(apiKey);
        return constantTimeEquals(expectedKeyHash, providedKeyHash);
    }
    
    /**
     * Hashes an API key using SHA-256.
     */
    private String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            return "";
        }
    }
    
    /**
     * Performs constant-time string comparison to prevent timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    /**
     * Handles authentication failure.
     */
    private void handleAuthFailure(ContainerRequestContext requestContext, 
            String clientIp, String reason) {
        
        int attemptCount = recordFailedAttempt(clientIp);
        
        AuditLogger.getInstance().logAuthenticationFailure(
            "unknown", clientIp, reason, attemptCount);
        
        if (attemptCount >= SecurityConfig.getMaxFailedAttempts()) {
            lockOut(clientIp);
            AuditLogger.getInstance().logAccountLockout(
                "IP:" + clientIp, clientIp, attemptCount, 
                SecurityConfig.getLockoutDurationMinutes());
        }
        
        Response response = Response.status(401)
            .entity("{\"error\":\"Authentication required\"}")
            .header("WWW-Authenticate", "ApiKey realm=\"APRA API\"")
            .type("application/json")
            .build();
        
        requestContext.abortWith(response);
    }
    
    /**
     * Records a failed authentication attempt.
     */
    private int recordFailedAttempt(String clientIp) {
        FailedAttemptInfo info = failedAttempts.computeIfAbsent(
            clientIp, k -> new FailedAttemptInfo());
        return info.incrementAndGet();
    }
    
    /**
     * Clears failed attempts for an IP after successful authentication.
     */
    private void clearFailedAttempts(String clientIp) {
        failedAttempts.remove(clientIp);
    }
    
    /**
     * Locks out an IP address.
     */
    private void lockOut(String clientIp) {
        FailedAttemptInfo info = failedAttempts.get(clientIp);
        if (info != null) {
            info.lockOut(SecurityConfig.getLockoutDurationMinutes());
        }
    }
    
    /**
     * Checks if an IP is currently locked out.
     */
    private boolean isLockedOut(String clientIp) {
        FailedAttemptInfo info = failedAttempts.get(clientIp);
        if (info == null) {
            return false;
        }
        if (info.isLockedOut()) {
            return true;
        }
        if (info.isLockoutExpired()) {
            failedAttempts.remove(clientIp);
        }
        return false;
    }
    
    /**
     * Extracts the client IP address from the request.
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
     * Tracks failed authentication attempts and lockout status.
     */
    private static class FailedAttemptInfo {
        private int count;
        private long lockoutUntil;
        
        public synchronized int incrementAndGet() {
            return ++count;
        }
        
        public synchronized void lockOut(int durationMinutes) {
            lockoutUntil = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        }
        
        public synchronized boolean isLockedOut() {
            return lockoutUntil > System.currentTimeMillis();
        }
        
        public synchronized boolean isLockoutExpired() {
            return lockoutUntil > 0 && lockoutUntil <= System.currentTimeMillis();
        }
    }
}
