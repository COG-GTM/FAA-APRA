/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * Shared utility for resolving the true client IP address.
 * Prefers the TCP connection address (HttpServletRequest.getRemoteAddr)
 * and only trusts X-Forwarded-For when the direct connection comes from
 * a configured trusted proxy. Configurable via APRA_TRUSTED_PROXY_IPS
 * environment variable (comma-separated list of proxy IPs).
 */
public final class ClientIpResolver {

    private static final Set<String> TRUSTED_PROXIES;

    static {
        Set<String> proxies = new HashSet<>();
        String envProxies = System.getenv("APRA_TRUSTED_PROXY_IPS");
        if (envProxies != null && !envProxies.isEmpty()) {
            for (String proxy : envProxies.split(",")) {
                String trimmed = proxy.trim();
                if (!trimmed.isEmpty()) {
                    proxies.add(trimmed);
                }
            }
        }
        TRUSTED_PROXIES = Collections.unmodifiableSet(proxies);
    }

    private ClientIpResolver() { }

    /**
     * Resolve client IP from a JAX-RS request context and servlet request.
     * Only trusts X-Forwarded-For when the direct TCP peer is a trusted proxy.
     */
    public static String resolve(ContainerRequestContext requestContext,
                                  HttpServletRequest servletRequest) {
        String remoteAddr = null;
        if (servletRequest != null) {
            remoteAddr = servletRequest.getRemoteAddr();
        }

        if (remoteAddr != null && !remoteAddr.isEmpty()
                && !TRUSTED_PROXIES.isEmpty()
                && TRUSTED_PROXIES.contains(remoteAddr)) {
            String forwarded = requestContext.getHeaderString("X-Forwarded-For");
            if (forwarded != null && !forwarded.isEmpty()) {
                String[] parts = forwarded.split(",");
                // Walk backward to find the rightmost untrusted IP
                for (int i = parts.length - 1; i >= 0; i--) {
                    String candidate = parts[i].trim();
                    if (!candidate.isEmpty() && !TRUSTED_PROXIES.contains(candidate)) {
                        return candidate;
                    }
                }
            }
        }

        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        return "0.0.0.0";
    }

    /**
     * Resolve client IP from HttpHeaders and servlet request.
     * Used by resource classes that receive @Context HttpHeaders.
     */
    public static String resolve(javax.ws.rs.core.HttpHeaders headers,
                                  HttpServletRequest servletRequest) {
        String remoteAddr = null;
        if (servletRequest != null) {
            remoteAddr = servletRequest.getRemoteAddr();
        }

        if (remoteAddr != null && !remoteAddr.isEmpty()
                && !TRUSTED_PROXIES.isEmpty()
                && TRUSTED_PROXIES.contains(remoteAddr)) {
            if (headers != null) {
                String forwarded = headers.getHeaderString("X-Forwarded-For");
                if (forwarded != null && !forwarded.isEmpty()) {
                    String[] parts = forwarded.split(",");
                    for (int i = parts.length - 1; i >= 0; i--) {
                        String candidate = parts[i].trim();
                        if (!candidate.isEmpty() && !TRUSTED_PROXIES.contains(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }

        if (remoteAddr != null && !remoteAddr.isEmpty()) {
            return remoteAddr;
        }
        return "0.0.0.0";
    }
}
