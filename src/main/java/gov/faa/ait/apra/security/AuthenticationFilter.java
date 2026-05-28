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
import java.util.Base64;

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
 */
@Provider
@PreMatching
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final String MANAGEMENT_PATH = "management";
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTH_HEADER = "Authorization";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        if (!path.startsWith(MANAGEMENT_PATH)) {
            return;
        }

        String clientIp = getClientIp(requestContext);
        String apiKey = requestContext.getHeaderString(API_KEY_HEADER);
        String configuredKey = getConfiguredApiKey();

        if (configuredKey != null && !configuredKey.isEmpty()) {
            if (apiKey == null || apiKey.isEmpty()) {
                AuditLogger.getInstance().logAuthFailure(clientIp, "missing_api_key");
                logger.warn("Management endpoint access denied: missing API key from {}", clientIp);
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"status\":{\"code\":401,\"message\":\"Authentication required\"}}")
                        .build());
                return;
            }

            if (!constantTimeEquals(apiKey, configuredKey)) {
                AuditLogger.getInstance().logAuthFailure(clientIp, "invalid_api_key");
                logger.warn("Management endpoint access denied: invalid API key from {}", clientIp);
                requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                        .entity("{\"status\":{\"code\":403,\"message\":\"Access denied\"}}")
                        .build());
                return;
            }

            AuditLogger.getInstance().logAuthSuccess("admin", clientIp);
        }

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
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
