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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rate limiter implementing STIG V-220636 requirements for monitoring and abuse prevention.
 * Uses a sliding window algorithm to limit requests per IP address.
 * 
 * @author FAA
 */
public class RateLimiter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    
    private final int maxRequests;
    private final long windowMillis;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestLog;
    private final ScheduledExecutorService cleanupExecutor;
    
    private static volatile RateLimiter instance;
    
    /**
     * Creates a rate limiter with specified limits.
     * 
     * @param maxRequests maximum number of requests allowed in the window
     * @param windowSeconds time window in seconds
     */
    public RateLimiter(int maxRequests, int windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
        this.requestLog = new ConcurrentHashMap<>();
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RateLimiter-Cleanup");
            t.setDaemon(true);
            return t;
        });
        this.cleanupExecutor.scheduleAtFixedRate(this::cleanupOldEntries, 
            windowSeconds, windowSeconds, TimeUnit.SECONDS);
    }
    
    /**
     * Gets the singleton instance with default settings (100 requests per 60 seconds).
     * 
     * @return the rate limiter instance
     */
    public static RateLimiter getInstance() {
        if (instance == null) {
            synchronized (RateLimiter.class) {
                if (instance == null) {
                    instance = new RateLimiter(
                        SecurityConfig.getRateLimitMaxRequests(),
                        SecurityConfig.getRateLimitWindowSeconds()
                    );
                }
            }
        }
        return instance;
    }
    
    /**
     * Checks if a request from the given identifier (IP address) is allowed.
     * 
     * @param identifier the client identifier (typically IP address)
     * @return true if the request is allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        long windowStart = now - windowMillis;
        
        ConcurrentLinkedQueue<Long> timestamps = requestLog.computeIfAbsent(
            identifier, k -> new ConcurrentLinkedQueue<>());
        
        while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
            timestamps.poll();
        }
        
        if (timestamps.size() >= maxRequests) {
            logger.warn("Rate limit exceeded for identifier: {}", 
                InputSanitizer.sanitizeForLog(identifier));
            return false;
        }
        
        timestamps.offer(now);
        return true;
    }
    
    /**
     * Gets the number of remaining requests for an identifier.
     * 
     * @param identifier the client identifier
     * @return number of remaining requests in the current window
     */
    public int getRemainingRequests(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return 0;
        }
        
        ConcurrentLinkedQueue<Long> timestamps = requestLog.get(identifier);
        if (timestamps == null) {
            return maxRequests;
        }
        
        long windowStart = System.currentTimeMillis() - windowMillis;
        int count = 0;
        for (Long ts : timestamps) {
            if (ts >= windowStart) {
                count++;
            }
        }
        
        return Math.max(0, maxRequests - count);
    }
    
    /**
     * Gets the time in seconds until the rate limit resets for an identifier.
     * 
     * @param identifier the client identifier
     * @return seconds until reset, or 0 if not rate limited
     */
    public long getResetTimeSeconds(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return 0;
        }
        
        ConcurrentLinkedQueue<Long> timestamps = requestLog.get(identifier);
        if (timestamps == null || timestamps.isEmpty()) {
            return 0;
        }
        
        Long oldest = timestamps.peek();
        if (oldest == null) {
            return 0;
        }
        
        long resetTime = oldest + windowMillis - System.currentTimeMillis();
        return Math.max(0, resetTime / 1000);
    }
    
    /**
     * Cleans up old entries from the request log.
     */
    private void cleanupOldEntries() {
        long windowStart = System.currentTimeMillis() - windowMillis;
        
        requestLog.forEach((identifier, timestamps) -> {
            while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
                timestamps.poll();
            }
            if (timestamps.isEmpty()) {
                requestLog.remove(identifier);
            }
        });
        
        if (logger.isDebugEnabled()) {
            logger.debug("Rate limiter cleanup completed. Active identifiers: {}", 
                requestLog.size());
        }
    }
    
    /**
     * Shuts down the rate limiter cleanup executor.
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Gets the maximum requests allowed per window.
     * 
     * @return max requests
     */
    public int getMaxRequests() {
        return maxRequests;
    }
    
    /**
     * Gets the window size in seconds.
     * 
     * @return window size in seconds
     */
    public long getWindowSeconds() {
        return windowMillis / 1000;
    }
}
