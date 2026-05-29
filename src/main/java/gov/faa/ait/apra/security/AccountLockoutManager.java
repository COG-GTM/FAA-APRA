/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * STIG V-220629 (NIST IA-2): Account lockout after failed login attempts.
 * Locks out accounts after 5 failed attempts for 15 minutes.
 * Periodically evicts expired records to prevent unbounded memory growth.
 */
public final class AccountLockoutManager {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final long LOCKOUT_DURATION_MS = 15L * 60L * 1000L;
    private static final long EVICTION_INTERVAL_MS = 5L * 60L * 1000L;

    private static final AccountLockoutManager INSTANCE = new AccountLockoutManager();
    private final ConcurrentHashMap<String, LockoutRecord> records = new ConcurrentHashMap<>();

    private AccountLockoutManager() {
        ScheduledExecutorService evictor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "lockout-evictor");
            t.setDaemon(true);
            return t;
        });
        evictor.scheduleAtFixedRate(this::evictExpired,
            EVICTION_INTERVAL_MS, EVICTION_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public static AccountLockoutManager getInstance() {
        return INSTANCE;
    }

    /**
     * Check if an identifier (username or IP) is currently locked out.
     */
    public boolean isLockedOut(String identifier) {
        LockoutRecord record = records.get(identifier);
        if (record == null) return false;
        if (record.lockedUntil > 0 && System.currentTimeMillis() < record.lockedUntil) {
            return true;
        }
        return false;
    }

    /**
     * Record a failed login attempt.
     */
    public void recordFailedAttempt(String identifier) {
        records.compute(identifier, (key, existing) -> {
            if (existing == null) {
                existing = new LockoutRecord();
            }
            if (existing.lockedUntil > 0 && System.currentTimeMillis() >= existing.lockedUntil) {
                existing.failedAttempts = 0;
                existing.lockedUntil = 0;
            }
            existing.failedAttempts++;
            existing.lastAttempt = System.currentTimeMillis();
            if (existing.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                existing.lockedUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
                AuditLogger.getInstance().log("account_locked", identifier, "n/a",
                    "lockout", "failure", "max_attempts_exceeded");
            }
            return existing;
        });
    }

    /**
     * Clear lockout record on successful authentication.
     */
    public void recordSuccess(String identifier) {
        records.remove(identifier);
    }

    public int getFailedAttempts(String identifier) {
        LockoutRecord record = records.get(identifier);
        return record != null ? record.failedAttempts : 0;
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, LockoutRecord>> it = records.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, LockoutRecord> entry = it.next();
            LockoutRecord record = entry.getValue();
            if (record.lockedUntil > 0 && now >= record.lockedUntil) {
                it.remove();
            } else if (record.lockedUntil == 0
                       && now - record.lastAttempt > LOCKOUT_DURATION_MS) {
                it.remove();
            }
        }
    }

    private static class LockoutRecord {
        int failedAttempts;
        long lockedUntil;
        long lastAttempt;
    }
}
