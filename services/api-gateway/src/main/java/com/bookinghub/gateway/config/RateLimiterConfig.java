package com.bookinghub.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimiterConfig {

    @Value("${rate.limit.per.minute:120}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String clientIp) {
        TokenBucket bucket = buckets.computeIfAbsent(clientIp, k -> new TokenBucket(requestsPerMinute));
        return bucket.tryConsume();
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

        TokenBucket(int requestsPerMinute) {
            this.capacity = requestsPerMinute;
            this.tokens = requestsPerMinute;
            this.refillIntervalMillis = 60_000 / requestsPerMinute; // distribute evenly over the minute
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
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
