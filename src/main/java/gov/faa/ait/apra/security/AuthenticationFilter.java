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
import java.nio.charset.StandardCharsets;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STIG V-220629 (NIST IA-2, IA-5): Authentication filter for management endpoints.
 * Protects administrative endpoints (/management/*) with API key authentication.
 * Public data endpoints remain unauthenticated per the API's design.
 * Health endpoint is excluded from authentication for load balancer probes.
 * Fail-closed: management endpoints are denied if no API key is configured.
 * Integrates with AccountLockoutManager for brute-force protection.
 */
@Provider
@PreMatching
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String MANAGEMENT_PATH = "management";
    private static final String HEALTH_PATH = "management/health";
    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        if (!path.startsWith(MANAGEMENT_PATH)) {
            return;
        }

        // Health endpoint remains unauthenticated for load balancer probes
        if (path.equals(HEALTH_PATH) || path.startsWith(HEALTH_PATH + "/")) {
            return;
        }

        String clientIp = getClientIp(requestContext);
        String safeClientIp = InputSanitizer.sanitizeForLog(clientIp);
        String apiKey = requestContext.getHeaderString(API_KEY_HEADER);
        String configuredKey = getConfiguredApiKey();

        // Check IP-based lockout before processing credentials
        if (AccountLockoutManager.getInstance().isLockedOut(clientIp)) {
            AuditLogger.getInstance().logAuthFailure(clientIp, "account_locked_out");
            logger.warn("Management endpoint access denied: IP locked out {}", safeClientIp);
            requestContext.abortWith(
                Response.status(429)
                    .entity("{\"status\":{\"code\":429,\"message\":\"Too many failed attempts. Try again later.\"}}")
                    .build());
            return;
        }

        // Fail-closed: if no API key is configured, deny all management access
        if (configuredKey == null || configuredKey.isEmpty()) {
            AuditLogger.getInstance().logAuthFailure(clientIp, "api_key_not_configured");
            logger.error("Management endpoint access denied: APRA_MANAGEMENT_API_KEY not configured");
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"status\":{\"code\":403,\"message\":\"Management endpoints not configured\"}}")
                    .build());
            return;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            AccountLockoutManager.getInstance().recordFailedAttempt(clientIp);
            AuditLogger.getInstance().logAuthFailure(clientIp, "missing_api_key");
            logger.warn("Management endpoint access denied: missing API key from {}", safeClientIp);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":{\"code\":401,\"message\":\"Authentication required\"}}")
                    .build());
            return;
        }

        if (!constantTimeEquals(apiKey, configuredKey)) {
            AccountLockoutManager.getInstance().recordFailedAttempt(clientIp);
            AuditLogger.getInstance().logAuthFailure(clientIp, "invalid_api_key");
            logger.warn("Management endpoint access denied: invalid API key from {}", safeClientIp);
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"status\":{\"code\":403,\"message\":\"Access denied\"}}")
                    .build());
            return;
        }

        AccountLockoutManager.getInstance().recordSuccess(clientIp);
        AuditLogger.getInstance().logAuthSuccess("admin", clientIp);
        AuditLogger.getInstance().logAdminAction(clientIp, "management_access:" + path);
    }

    private String getClientIp(ContainerRequestContext requestContext) {
        String forwarded = requestContext.getHeaderString("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return "unknown";
    }

    private String getConfiguredApiKey() {
        return System.getenv("APRA_MANAGEMENT_API_KEY");
    }

    /**
     * Constant-time string comparison to prevent timing attacks (STIG V-220629).
     * Iterates over the longer of the two inputs to avoid leaking length information.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        int result = 0;
        int maxLen = Math.max(aBytes.length, bBytes.length);
        for (int i = 0; i < maxLen; i++) {
            byte aByte = i < aBytes.length ? aBytes[i] : 0;
            byte bByte = i < bBytes.length ? bBytes[i] : 0;
            result |= aByte ^ bByte;
        }
        if (aBytes.length != bBytes.length) {
            result |= 1;
        }
        return result == 0;
    }
}
