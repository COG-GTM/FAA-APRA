/*
 * Federal Aviation Administration (FAA) public work 
 * 
 * As a work of the United States Government, this project is in the 
 * public domain within the United States. Additionally, we waive copyright 
 * and related rights in the work worldwide 
 * through the Creative Commons 0 (CC0) 1.0 Universal public domain dedication
 */
package gov.faa.ait.apra.security;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * STIG V-220629/V-220630: Rate limiter to prevent brute-force attacks
 * and denial-of-service. Implements a sliding window counter per client IP.
 * Stale entries are cleaned up probabilistically to bound memory usage.
 */
public final class RateLimiter {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000L; // 1 minute
    private static final int CLEANUP_INTERVAL = 100; // run cleanup every ~100 requests
    private static final AtomicLong requestCount = new AtomicLong(0);

    private static final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();

    /**
     * Check if a request from the given IP should be allowed.
     * Returns true if the request is within rate limits, false otherwise.
     * Periodically triggers cleanup to prevent unbounded memory growth.
     */
    public boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();

        if (requestCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            cleanup(now);
        }

        RequestCounter counter = counters.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStart.get() > WINDOW_MS) {
                return new RequestCounter(now);
            }
            return existing;
        });
        return counter.count.incrementAndGet() <= MAX_REQUESTS;
    }

    /**
     * Remove expired entries to prevent memory leaks.
     */
    static void cleanup(long now) {
        counters.entrySet().removeIf(entry ->
            now - entry.getValue().windowStart.get() > WINDOW_MS * 2);
    }

    private static class RequestCounter {
        final AtomicLong windowStart;
        final AtomicInteger count;

        RequestCounter(long startTime) {
            this.windowStart = new AtomicLong(startTime);
            this.count = new AtomicInteger(0);
        }
    }
}
