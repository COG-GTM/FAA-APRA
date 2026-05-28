/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STIG V-220635: Structured JSON audit logging for security events.
 * Logs authentication, authorization, data access, and security violations
 * in a structured format per NIST AU-2 and AU-3 requirements.
 */
public final class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("gov.faa.ait.apra.audit");
    private static final String APP_NAME = "APRA";

    private AuditLogger() { }

    /**
     * Log a security-relevant event in structured JSON format.
     *
     * @param eventType   the type of event (e.g., "api_access", "auth_failure")
     * @param ipAddress   the client IP address
     * @param method      the HTTP method
     * @param path        the request path
     * @param outcome     "success" or "failure"
     * @param details     additional detail string
     */
    public static void log(String eventType, String ipAddress, String method,
                           String path, String outcome, String details) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("{\"timestamp\":\"").append(getTimestamp())
          .append("\",\"app\":\"").append(APP_NAME)
          .append("\",\"event_type\":\"").append(escapeJson(eventType))
          .append("\",\"ip_address\":\"").append(escapeJson(ipAddress))
          .append("\",\"http_method\":\"").append(escapeJson(method))
          .append("\",\"path\":\"").append(escapeJson(path))
          .append("\",\"outcome\":\"").append(escapeJson(outcome))
          .append("\"");
        if (details != null && !details.isEmpty()) {
            sb.append(",\"details\":\"").append(escapeJson(details)).append("\"");
        }
        sb.append("}");
        auditLog.info(sb.toString());
    }

    public static void logAccess(String ipAddress, String method, String path, int statusCode) {
        logAccess(ipAddress, method, path, statusCode, -1);
    }

    public static void logAccess(String ipAddress, String method, String path, int statusCode, long durationMs) {
        String outcome = statusCode < 400 ? "success" : "failure";
        String details = "status_code=" + statusCode;
        if (durationMs >= 0) {
            details += ",duration_ms=" + durationMs;
        }
        log("api_access", ipAddress, method, path, outcome, details);
    }

    public static void logSecurityViolation(String ipAddress, String method, String path, String violation) {
        log("security_violation", ipAddress, method, path, "failure", violation);
    }

    public static void logInputValidationFailure(String ipAddress, String method, String path, String paramName) {
        log("input_validation_failure", ipAddress, method, path, "failure",
            "invalid_parameter=" + paramName);
    }

    public static void logManagementAccess(String ipAddress, String action, String outcome) {
        log("admin_action", ipAddress, "GET", "/management/" + action, outcome, null);
    }

    public static void logAuthFailure(String ipAddress, String path, String reason) {
        log("auth_failure", ipAddress, "GET", path, "failure", reason);
    }

    public static void logRateLimitExceeded(String ipAddress, String method, String path) {
        log("rate_limit_exceeded", ipAddress, method, path, "failure", "request_throttled");
    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                     .replace("\"", "\\\"")
                     .replace("\n", "\\n")
                     .replace("\r", "\\r")
                     .replace("\t", "\\t");
    }
}
