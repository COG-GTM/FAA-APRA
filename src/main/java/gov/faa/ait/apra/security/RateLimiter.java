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
 */
public final class RateLimiter {

    private static final int DEFAULT_MAX_REQUESTS = 100;
    private static final long DEFAULT_WINDOW_MS = 60_000L; // 1 minute

    private static final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();

    private final int maxRequests;
    private final long windowMs;

    public RateLimiter() {
        this(DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_MS);
    }

    public RateLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    /**
     * Check if a request from the given IP should be allowed.
     * Returns true if the request is within rate limits, false otherwise.
     */
    public boolean allowRequest(String clientIp) {
        long now = System.currentTimeMillis();
        RequestCounter counter = counters.compute(clientIp, (key, existing) -> {
            if (existing == null || now - existing.windowStart.get() > windowMs) {
                return new RequestCounter(now);
            }
            return existing;
        });
        return counter.count.incrementAndGet() <= maxRequests;
    }

    /**
     * Periodically clean up expired entries to prevent memory leaks.
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(entry ->
            now - entry.getValue().windowStart.get() > DEFAULT_WINDOW_MS * 2);
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
