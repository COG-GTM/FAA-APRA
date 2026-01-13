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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security configuration class for APRA security settings.
 * Loads security-related configuration from properties file.
 * 
 * @author FAA
 */
public final class SecurityConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private static final Properties securityProps = new Properties();
    
    private static final String CONFIG_PATH = "/opt/apra/conf/security.properties";
    
    private static final int DEFAULT_RATE_LIMIT_MAX_REQUESTS = 100;
    private static final int DEFAULT_RATE_LIMIT_WINDOW_SECONDS = 60;
    private static final int DEFAULT_SESSION_TIMEOUT_MINUTES = 15;
    private static final int DEFAULT_MAX_FAILED_ATTEMPTS = 5;
    private static final int DEFAULT_LOCKOUT_DURATION_MINUTES = 15;
    private static final boolean DEFAULT_SECURITY_HEADERS_ENABLED = true;
    private static final boolean DEFAULT_RATE_LIMITING_ENABLED = true;
    private static final boolean DEFAULT_AUDIT_LOGGING_ENABLED = true;
    private static final boolean DEFAULT_AUTHENTICATION_ENABLED = false;
    
    private SecurityConfig() {
    }
    
    static {
        loadConfig();
    }
    
    /**
     * Loads security configuration from the properties file.
     */
    public static void loadConfig() {
        try {
            File configFile = new File(CONFIG_PATH);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    securityProps.load(fis);
                    logger.info("Security configuration loaded from {}", CONFIG_PATH);
                }
            } else {
                logger.info("Security configuration file not found at {}. Using defaults.", CONFIG_PATH);
            }
        } catch (IOException e) {
            logger.warn("Failed to load security configuration. Using defaults.", e);
        }
    }
    
    /**
     * Gets the maximum number of requests allowed per rate limit window.
     * 
     * @return max requests per window
     */
    public static int getRateLimitMaxRequests() {
        return getIntProperty("security.ratelimit.max.requests", DEFAULT_RATE_LIMIT_MAX_REQUESTS);
    }
    
    /**
     * Gets the rate limit window size in seconds.
     * 
     * @return window size in seconds
     */
    public static int getRateLimitWindowSeconds() {
        return getIntProperty("security.ratelimit.window.seconds", DEFAULT_RATE_LIMIT_WINDOW_SECONDS);
    }
    
    /**
     * Gets the session timeout in minutes.
     * 
     * @return session timeout in minutes
     */
    public static int getSessionTimeoutMinutes() {
        return getIntProperty("security.session.timeout.minutes", DEFAULT_SESSION_TIMEOUT_MINUTES);
    }
    
    /**
     * Gets the maximum number of failed login attempts before lockout.
     * 
     * @return max failed attempts
     */
    public static int getMaxFailedAttempts() {
        return getIntProperty("security.auth.max.failed.attempts", DEFAULT_MAX_FAILED_ATTEMPTS);
    }
    
    /**
     * Gets the account lockout duration in minutes.
     * 
     * @return lockout duration in minutes
     */
    public static int getLockoutDurationMinutes() {
        return getIntProperty("security.auth.lockout.duration.minutes", DEFAULT_LOCKOUT_DURATION_MINUTES);
    }
    
    /**
     * Checks if security headers are enabled.
     * 
     * @return true if security headers should be added to responses
     */
    public static boolean isSecurityHeadersEnabled() {
        return getBooleanProperty("security.headers.enabled", DEFAULT_SECURITY_HEADERS_ENABLED);
    }
    
    /**
     * Checks if rate limiting is enabled.
     * 
     * @return true if rate limiting is enabled
     */
    public static boolean isRateLimitingEnabled() {
        return getBooleanProperty("security.ratelimit.enabled", DEFAULT_RATE_LIMITING_ENABLED);
    }
    
    /**
     * Checks if audit logging is enabled.
     * 
     * @return true if audit logging is enabled
     */
    public static boolean isAuditLoggingEnabled() {
        return getBooleanProperty("security.audit.enabled", DEFAULT_AUDIT_LOGGING_ENABLED);
    }
    
    /**
     * Checks if authentication is enabled.
     * Note: Authentication is disabled by default for backward compatibility.
     * 
     * @return true if authentication is required
     */
    public static boolean isAuthenticationEnabled() {
        return getBooleanProperty("security.auth.enabled", DEFAULT_AUTHENTICATION_ENABLED);
    }
    
    /**
     * Gets the Content-Security-Policy header value.
     * 
     * @return CSP header value
     */
    public static String getContentSecurityPolicy() {
        return securityProps.getProperty("security.headers.csp", 
            "default-src 'self'; frame-ancestors 'none'; form-action 'self'");
    }
    
    /**
     * Gets the audit log directory path.
     * 
     * @return audit log directory
     */
    public static String getAuditLogDirectory() {
        return securityProps.getProperty("security.audit.log.directory", "/var/log/apra");
    }
    
    /**
     * Gets the application name for audit logging.
     * 
     * @return application name
     */
    public static String getApplicationName() {
        return securityProps.getProperty("security.audit.app.name", "faa-apra");
    }
    
    /**
     * Gets a list of IP addresses that are exempt from rate limiting.
     * 
     * @return comma-separated list of exempt IPs
     */
    public static String getRateLimitExemptIps() {
        return securityProps.getProperty("security.ratelimit.exempt.ips", "");
    }
    
    /**
     * Gets the API key header name for authentication.
     * 
     * @return API key header name
     */
    public static String getApiKeyHeaderName() {
        return securityProps.getProperty("security.auth.apikey.header", "X-API-Key");
    }
    
    private static int getIntProperty(String key, int defaultValue) {
        String value = securityProps.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer value for {}: {}. Using default: {}", 
                    key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    private static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = securityProps.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value.trim());
        }
        return defaultValue;
    }
}
