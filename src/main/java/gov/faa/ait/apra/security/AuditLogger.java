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
 * STIG V-220635 (NIST AU-2, AU-3): Structured JSON audit logging.
 * Logs security-relevant events in a machine-parseable JSON format including
 * timestamp, event type, user ID, IP address, action outcome, and details.
 */
public final class AuditLogger {

    private static final Logger auditLog = LoggerFactory.getLogger("gov.faa.ait.apra.audit");
    private static final AuditLogger INSTANCE = new AuditLogger();

    private AuditLogger() { }

    public static AuditLogger getInstance() {
        return INSTANCE;
    }

    /**
     * Log a structured audit event.
     *
     * @param eventType the type of event (e.g., login_success, access_denied)
     * @param userId    the user identifier or "anonymous"
     * @param ipAddress the client IP address
     * @param action    the action being performed
     * @param outcome   success or failure
     * @param details   additional context
     */
    public void log(String eventType, String userId, String ipAddress,
                    String action, String outcome, String details) {
        String safeUserId = InputSanitizer.sanitizeForLog(userId != null ? userId : "anonymous");
        String safeIp = InputSanitizer.sanitizeForLog(ipAddress != null ? ipAddress : "unknown");
        String safeAction = InputSanitizer.sanitizeForLog(action != null ? action : "");
        String safeOutcome = InputSanitizer.sanitizeForLog(outcome != null ? outcome : "unknown");
        String safeDetails = InputSanitizer.sanitizeForLog(details != null ? details : "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = sdf.format(new Date());

        StringBuilder entry = new StringBuilder();
        entry.append("{\"timestamp\":\"").append(timestamp).append("\"");
        entry.append(",\"event_type\":\"").append(safeEventType(eventType)).append("\"");
        entry.append(",\"user_id\":\"").append(safeUserId).append("\"");
        entry.append(",\"ip_address\":\"").append(safeIp).append("\"");
        entry.append(",\"action\":\"").append(safeAction).append("\"");
        entry.append(",\"outcome\":\"").append(safeOutcome).append("\"");
        entry.append(",\"details\":\"").append(safeDetails).append("\"");
        entry.append("}");

        auditLog.info(entry.toString());
    }

    public void logAccess(String ipAddress, String method, String path, int statusCode) {
        log("data_access", "anonymous", ipAddress, method + " " + path,
            statusCode < 400 ? "success" : "failure",
            "status=" + statusCode);
    }

    public void logSecurityViolation(String ipAddress, String reason) {
        log("security_violation", "anonymous", ipAddress, "request_blocked",
            "failure", reason);
    }

    public void logAdminAction(String ipAddress, String action) {
        log("admin_action", "anonymous", ipAddress, action,
            "success", "management_endpoint");
    }

    public void logAuthFailure(String ipAddress, String reason) {
        log("login_failed", "anonymous", ipAddress, "authentication",
            "failure", reason);
    }

    public void logAuthSuccess(String userId, String ipAddress) {
        log("login_success", userId, ipAddress, "authentication",
            "success", "");
    }

    public void logValidationFailure(String ipAddress, String parameter, String value) {
        String safeParam = InputSanitizer.sanitizeForLog(parameter);
        String safeValue = InputSanitizer.sanitizeForLog(value);
        log("security_violation", "anonymous", ipAddress, "input_validation",
            "failure", "invalid_" + safeParam + "=" + safeValue);
    }

    private String safeEventType(String eventType) {
        if (eventType == null) {
            return "unknown";
        }
        return eventType.replaceAll("[^a-zA-Z0-9_]", "");
    }
}
