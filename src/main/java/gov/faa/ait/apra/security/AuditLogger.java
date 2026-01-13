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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit logger implementing STIG V-220635 requirements for security event logging.
 * Logs security events in JSON format with integrity hashes.
 * 
 * @author FAA
 */
public class AuditLogger {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY");
    
    private static volatile AuditLogger instance;
    private final String appName;
    
    /**
     * Audit event types as defined by STIG V-220635.
     */
    public enum EventType {
        AUTH_SUCCESS,
        AUTH_FAILURE,
        AUTH_LOCKOUT,
        SESSION_CREATE,
        SESSION_EXPIRE,
        SESSION_DESTROY,
        DATA_ACCESS,
        DATA_MODIFY,
        PERMISSION_DENIED,
        RATE_LIMIT_EXCEEDED,
        INPUT_VALIDATION_FAILURE,
        SECURITY_VIOLATION,
        ADMIN_ACTION,
        API_REQUEST,
        API_RESPONSE,
        ERROR
    }
    
    /**
     * Creates an audit logger with the specified application name.
     * 
     * @param appName the application name for log entries
     */
    public AuditLogger(String appName) {
        this.appName = appName;
    }
    
    /**
     * Gets the singleton instance of the audit logger.
     * 
     * @return the audit logger instance
     */
    public static AuditLogger getInstance() {
        if (instance == null) {
            synchronized (AuditLogger.class) {
                if (instance == null) {
                    instance = new AuditLogger(SecurityConfig.getApplicationName());
                }
            }
        }
        return instance;
    }
    
    /**
     * Logs an API request event.
     * 
     * @param ipAddress client IP address
     * @param method HTTP method
     * @param path request path
     * @param userAgent user agent string
     */
    public void logApiRequest(String ipAddress, String method, String path, String userAgent) {
        Map<String, Object> details = new TreeMap<>();
        details.put("method", method);
        details.put("path", InputSanitizer.sanitizeForLog(path));
        details.put("user_agent", InputSanitizer.sanitizeForLog(userAgent));
        
        logEvent(EventType.API_REQUEST, null, ipAddress, "API request received", details);
    }
    
    /**
     * Logs an API response event.
     * 
     * @param ipAddress client IP address
     * @param method HTTP method
     * @param path request path
     * @param statusCode response status code
     * @param durationMs request duration in milliseconds
     */
    public void logApiResponse(String ipAddress, String method, String path, 
            int statusCode, long durationMs) {
        Map<String, Object> details = new TreeMap<>();
        details.put("method", method);
        details.put("path", InputSanitizer.sanitizeForLog(path));
        details.put("status_code", statusCode);
        details.put("duration_ms", durationMs);
        
        logEvent(EventType.API_RESPONSE, null, ipAddress, "API response sent", details);
    }
    
    /**
     * Logs a data access event.
     * 
     * @param userId user ID (if authenticated)
     * @param ipAddress client IP address
     * @param resource the resource being accessed
     * @param action the action performed (read, list, etc.)
     */
    public void logDataAccess(String userId, String ipAddress, String resource, String action) {
        Map<String, Object> details = new TreeMap<>();
        details.put("resource", InputSanitizer.sanitizeForLog(resource));
        details.put("action", action);
        
        logEvent(EventType.DATA_ACCESS, userId, ipAddress, 
            "Data access: " + action + " on " + resource, details);
    }
    
    /**
     * Logs a rate limit exceeded event.
     * 
     * @param ipAddress client IP address
     * @param endpoint the endpoint that was rate limited
     * @param requestCount number of requests in the window
     */
    public void logRateLimitExceeded(String ipAddress, String endpoint, int requestCount) {
        Map<String, Object> details = new TreeMap<>();
        details.put("endpoint", InputSanitizer.sanitizeForLog(endpoint));
        details.put("request_count", requestCount);
        
        logEvent(EventType.RATE_LIMIT_EXCEEDED, null, ipAddress, 
            "Rate limit exceeded", details);
        
        securityLogger.warn("Rate limit exceeded for IP {} on endpoint {}", 
            InputSanitizer.sanitizeForLog(ipAddress), 
            InputSanitizer.sanitizeForLog(endpoint));
    }
    
    /**
     * Logs an input validation failure event.
     * 
     * @param ipAddress client IP address
     * @param endpoint the endpoint where validation failed
     * @param validationError description of the validation error
     * @param inputData the invalid input data (sanitized)
     */
    public void logInputValidationFailure(String ipAddress, String endpoint, 
            String validationError, Map<String, String> inputData) {
        Map<String, Object> details = new TreeMap<>();
        details.put("endpoint", InputSanitizer.sanitizeForLog(endpoint));
        details.put("validation_error", validationError);
        if (inputData != null) {
            Map<String, String> sanitizedInput = new TreeMap<>();
            inputData.forEach((k, v) -> sanitizedInput.put(k, InputSanitizer.sanitizeForLog(v)));
            details.put("input_data", sanitizedInput);
        }
        
        logEvent(EventType.INPUT_VALIDATION_FAILURE, null, ipAddress, 
            "Input validation failed: " + validationError, details);
        
        securityLogger.warn("Input validation failure from IP {}: {}", 
            InputSanitizer.sanitizeForLog(ipAddress), validationError);
    }
    
    /**
     * Logs a security violation event.
     * 
     * @param ipAddress client IP address
     * @param violationType type of security violation
     * @param details additional details about the violation
     */
    public void logSecurityViolation(String ipAddress, String violationType, 
            Map<String, Object> details) {
        if (details == null) {
            details = new TreeMap<>();
        }
        details.put("violation_type", violationType);
        
        logEvent(EventType.SECURITY_VIOLATION, null, ipAddress, 
            "Security violation: " + violationType, details);
        
        securityLogger.error("Security violation from IP {}: {}", 
            InputSanitizer.sanitizeForLog(ipAddress), violationType);
    }
    
    /**
     * Logs an authentication success event.
     * 
     * @param userId the authenticated user ID
     * @param ipAddress client IP address
     * @param sessionId the created session ID
     */
    public void logAuthenticationSuccess(String userId, String ipAddress, String sessionId) {
        Map<String, Object> details = new TreeMap<>();
        details.put("session_id", sessionId);
        
        logEvent(EventType.AUTH_SUCCESS, userId, ipAddress, 
            "Authentication successful", details);
    }
    
    /**
     * Logs an authentication failure event.
     * 
     * @param username the attempted username
     * @param ipAddress client IP address
     * @param reason the reason for failure
     * @param attemptNumber the number of failed attempts
     */
    public void logAuthenticationFailure(String username, String ipAddress, 
            String reason, int attemptNumber) {
        Map<String, Object> details = new TreeMap<>();
        details.put("username", InputSanitizer.sanitizeForLog(username));
        details.put("reason", reason);
        details.put("attempt_number", attemptNumber);
        
        logEvent(EventType.AUTH_FAILURE, null, ipAddress, 
            "Authentication failed: " + reason, details);
        
        securityLogger.warn("Authentication failure for user {} from IP {}: {} (attempt {})", 
            InputSanitizer.sanitizeForLog(username), 
            InputSanitizer.sanitizeForLog(ipAddress), 
            reason, attemptNumber);
    }
    
    /**
     * Logs an account lockout event.
     * 
     * @param username the locked username
     * @param ipAddress client IP address
     * @param failedAttempts number of failed attempts
     * @param lockoutDurationMinutes lockout duration in minutes
     */
    public void logAccountLockout(String username, String ipAddress, 
            int failedAttempts, int lockoutDurationMinutes) {
        Map<String, Object> details = new TreeMap<>();
        details.put("username", InputSanitizer.sanitizeForLog(username));
        details.put("failed_attempts", failedAttempts);
        details.put("lockout_duration_minutes", lockoutDurationMinutes);
        
        logEvent(EventType.AUTH_LOCKOUT, null, ipAddress, 
            "Account locked due to failed attempts", details);
        
        securityLogger.error("Account lockout for user {} from IP {} after {} failed attempts", 
            InputSanitizer.sanitizeForLog(username), 
            InputSanitizer.sanitizeForLog(ipAddress), 
            failedAttempts);
    }
    
    /**
     * Logs an error event.
     * 
     * @param ipAddress client IP address
     * @param errorMessage the error message
     * @param errorType the type of error
     */
    public void logError(String ipAddress, String errorMessage, String errorType) {
        Map<String, Object> details = new TreeMap<>();
        details.put("error_type", errorType);
        details.put("error_message", InputSanitizer.sanitizeForLog(errorMessage));
        
        logEvent(EventType.ERROR, null, ipAddress, "Error occurred: " + errorType, details);
    }
    
    /**
     * Core logging method that creates a structured JSON log entry.
     * 
     * @param eventType the type of event
     * @param userId the user ID (may be null)
     * @param ipAddress the client IP address
     * @param message the log message
     * @param details additional event details
     */
    private void logEvent(EventType eventType, String userId, String ipAddress, 
            String message, Map<String, Object> details) {
        
        if (!SecurityConfig.isAuditLoggingEnabled()) {
            return;
        }
        
        String eventId = UUID.randomUUID().toString();
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"event_id\":\"").append(eventId).append("\",");
        jsonBuilder.append("\"timestamp\":\"").append(timestamp).append("\",");
        jsonBuilder.append("\"app_name\":\"").append(appName).append("\",");
        jsonBuilder.append("\"event_type\":\"").append(eventType.name()).append("\",");
        
        if (userId != null) {
            jsonBuilder.append("\"user_id\":\"")
                .append(InputSanitizer.sanitizeForLog(userId)).append("\",");
        }
        
        if (ipAddress != null) {
            jsonBuilder.append("\"ip_address\":\"")
                .append(InputSanitizer.sanitizeForLog(ipAddress)).append("\",");
        }
        
        jsonBuilder.append("\"message\":\"")
            .append(InputSanitizer.sanitizeForLog(message)).append("\"");
        
        if (details != null && !details.isEmpty()) {
            jsonBuilder.append(",\"details\":");
            jsonBuilder.append(mapToJson(details));
        }
        
        String contentForHash = jsonBuilder.toString();
        String integrityHash = computeIntegrityHash(contentForHash);
        
        jsonBuilder.append(",\"integrity_hash\":\"").append(integrityHash).append("\"");
        jsonBuilder.append("}");
        
        auditLogger.info(jsonBuilder.toString());
    }
    
    /**
     * Converts a map to a JSON string.
     */
    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(InputSanitizer.sanitizeForLog((String) value)).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                json.append(mapToJson(nestedMap));
            } else {
                json.append("\"").append(String.valueOf(value)).append("\"");
            }
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * Computes SHA-256 integrity hash for log entry verification.
     */
    private String computeIntegrityHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return "hash_error";
        }
    }
}
