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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STIG V-220629: Authentication filter for management endpoints.
 * Restricts access to /management/* paths by requiring a valid
 * API key provided via the X-Management-Key header or by
 * verifying the request originates from localhost (using the
 * direct TCP peer address, not the spoofable X-Forwarded-For header).
 */
@Provider
public class ManagementAuthFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ManagementAuthFilter.class);
    private static final String MANAGEMENT_PATH = "management";
    private static final String AUTH_HEADER = "X-Management-Key";
    private static final String MGMT_KEY_ENV = "APRA_MANAGEMENT_KEY";

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        if (!path.startsWith(MANAGEMENT_PATH)) {
            return;
        }

        // Allow health check without authentication
        if (path.endsWith("/health")) {
            return;
        }

        String directIp = getDirectClientIp();
        String loggableIp = getLoggableClientIp();

        // Allow localhost access for operational tooling.
        // Uses getRemoteAddr() (TCP peer) to prevent X-Forwarded-For spoofing.
        if (isLocalhost(directIp)) {
            AuditLogger.logManagementAccess(loggableIp, path, "success");
            return;
        }

        // Require API key for remote management access
        String providedKey = requestContext.getHeaderString(AUTH_HEADER);
        String expectedKey = System.getenv(MGMT_KEY_ENV);

        if (expectedKey == null || expectedKey.isEmpty()) {
            logger.warn("Management key not configured. Denying remote management access from " + loggableIp);
            AuditLogger.logAuthFailure(loggableIp, path, "management_key_not_configured");
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Access denied\"}")
                    .build());
            return;
        }

        if (providedKey == null || !constantTimeEquals(providedKey, expectedKey)) {
            logger.warn("Invalid management key from " + loggableIp);
            AuditLogger.logAuthFailure(loggableIp, path, "invalid_management_key");
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"error\":\"Access denied\"}")
                    .build());
            return;
        }

        AuditLogger.logManagementAccess(loggableIp, path, "success");
    }

    /**
     * Returns the actual TCP peer address for authentication decisions.
     * This cannot be spoofed by client-supplied headers.
     */
    private String getDirectClientIp() {
        if (servletRequest == null) {
            return "unknown";
        }
        return servletRequest.getRemoteAddr();
    }

    /**
     * Returns the client IP for logging/auditing purposes.
     * Prefers X-Forwarded-For when behind a trusted reverse proxy.
     */
    private String getLoggableClientIp() {
        if (servletRequest == null) {
            return "unknown";
        }
        String forwarded = servletRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return servletRequest.getRemoteAddr();
    }

    private static boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     * Compares all characters regardless of length difference to avoid
     * leaking the expected key length.
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        int maxLen = Math.max(a.length(), b.length());
        int result = a.length() ^ b.length();
        for (int i = 0; i < maxLen; i++) {
            char ca = i < a.length() ? a.charAt(i) : 0;
            char cb = i < b.length() ? b.charAt(i) : 0;
            result |= ca ^ cb;
        }
        return result == 0;
    }
}
