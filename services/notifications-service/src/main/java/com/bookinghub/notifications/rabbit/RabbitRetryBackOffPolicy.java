package com.bookinghub.notifications.rabbit;

import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

/**
 * Custom backoff policy implementing infra/rabbitmq/README.md's exact retry schedule:
 * "3 retries with 5s/30s/2min backoff".
 *
 * Produces exactly 3 delays (5000ms, 30000ms, 120000ms) across up to 3 retries
 * (after the initial attempt), for a total of 4 attempts per message.
 *
 * Each retry's delay is tracked in a per-retry BackOffContext (Ctx), so this
 * policy is safe for concurrent listener container threads — each message's
 * retry cycle has its own independent delay index.
 */
public class RabbitRetryBackOffPolicy implements BackOffPolicy {

    private static final long[] DELAYS_MS = {5_000L, 30_000L, 120_000L};

    @Override
    public BackOffContext start(RetryContext context) {
        return new Ctx();
    }

    @Override
    public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
        Ctx ctx = (Ctx) backOffContext;
        long delay = DELAYS_MS[Math.min(ctx.index, DELAYS_MS.length - 1)];
        ctx.index++;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackOffInterruptedException("interrupted", e);
        }
    }

    private static class Ctx implements BackOffContext {
        int index = 0;
    }
}
