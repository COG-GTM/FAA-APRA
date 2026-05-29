/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * STIG V-220630 (NIST AC-7, AC-12): Zero Trust session management.
 * Sessions are bound to the originating IP address, expire after 15 minutes
 * of inactivity, and session IDs are regenerated on authentication.
 */
public final class SessionManager {

    public static final long SESSION_TIMEOUT_MS = 15L * 60L * 1000L;
    private static final SessionManager INSTANCE = new SessionManager();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final ConcurrentHashMap<String, SessionRecord> sessions = new ConcurrentHashMap<>();

    private SessionManager() { }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /**
     * Create a new IP-bound session.
     */
    public String createSession(String userId, String ipAddress, String userAgent) {
        String sessionId = generateSessionId();
        SessionRecord record = new SessionRecord();
        record.userId = userId;
        record.boundIp = ipAddress;
        record.userAgent = userAgent;
        record.createdAt = System.currentTimeMillis();
        record.lastActivity = System.currentTimeMillis();
        sessions.put(sessionId, record);

        AuditLogger.getInstance().log("session_created", userId, ipAddress,
            "create_session", "success", "");
        return sessionId;
    }

    /**
     * Validate a session: checks expiration and IP binding.
     */
    public boolean validateSession(String sessionId, String requestIp) {
        if (sessionId == null) return false;
        SessionRecord record = sessions.get(sessionId);
        if (record == null) return false;

        long now = System.currentTimeMillis();
        if ((now - record.lastActivity) > SESSION_TIMEOUT_MS) {
            sessions.remove(sessionId);
            AuditLogger.getInstance().log("session_expired", record.userId, requestIp,
                "validate_session", "failure", "timeout");
            return false;
        }

        if (!record.boundIp.equals(requestIp)) {
            AuditLogger.getInstance().log("session_ip_mismatch", record.userId, requestIp,
                "validate_session", "failure",
                "expected=" + InputSanitizer.sanitizeForLog(record.boundIp));
            return false;
        }

        record.lastActivity = now;
        return true;
    }

    /**
     * Destroy a session (logout).
     */
    public void destroySession(String sessionId) {
        SessionRecord record = sessions.remove(sessionId);
        if (record != null) {
            AuditLogger.getInstance().log("logout", record.userId, record.boundIp,
                "destroy_session", "success", "");
        }
    }

    /**
     * Regenerate a session ID (post-authentication).
     */
    public String regenerateSession(String oldSessionId, String ipAddress) {
        SessionRecord record = sessions.remove(oldSessionId);
        if (record == null) return null;
        String newSessionId = generateSessionId();
        record.lastActivity = System.currentTimeMillis();
        sessions.put(newSessionId, record);
        AuditLogger.getInstance().log("session_regenerated", record.userId, ipAddress,
            "regenerate_session", "success", "");
        return newSessionId;
    }

    private String generateSessionId() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static class SessionRecord {
        String userId;
        String boundIp;
        String userAgent;
        long createdAt;
        long lastActivity;
    }
}
