package com.bookinghub.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableScheduling
public class RateLimiterConfig {

    @Value("${rate.limit.per.minute:120}")
    private int requestsPerMinute;

    private static final long BUCKET_IDLE_TIMEOUT_MILLIS = 3600_000; // 1 hour

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientIp) {
        TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket(requestsPerMinute));
        return bucket.tryConsume();
    }

    /**
     * Cleanup idle buckets every 10 minutes to prevent memory leak.
     * Removes buckets that haven't been accessed in over 1 hour.
     */
    @Scheduled(fixedRate = 600_000) // 10 minutes
    public void cleanupIdleBuckets() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> 
            now - entry.getValue().getLastAccessTime() > BUCKET_IDLE_TIMEOUT_MILLIS
        );
    }

    /**
     * Simple token bucket implementation for in-memory rate limiting.
     * Refills tokens at a fixed rate (requestsPerMinute tokens per minute).
     */
    static class TokenBucket {
        private final int capacity;
        private final long refillIntervalMillis;
        private int tokens;
        private long lastRefillTimestamp;
        private long lastAccessTime;

        TokenBucket(int requestsPerMinute) {
            this.capacity = requestsPerMinute;
            this.tokens = requestsPerMinute;
            this.refillIntervalMillis = 60_000 / requestsPerMinute; // distribute evenly over the minute
            this.lastRefillTimestamp = System.currentTimeMillis();
            this.lastAccessTime = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            this.lastAccessTime = System.currentTimeMillis();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        long getLastAccessTime() {
            return lastAccessTime;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTimestamp;
            int tokensToAdd = (int) (timePassed / refillIntervalMillis);
            
            if (tokensToAdd > 0) {
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTimestamp = now - (timePassed % refillIntervalMillis);
            }
        }
    }
}
