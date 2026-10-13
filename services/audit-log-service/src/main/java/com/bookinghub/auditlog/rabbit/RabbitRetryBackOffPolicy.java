package com.bookinghub.auditlog.rabbit;

import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

/**
 * Custom BackOffPolicy producing exactly 3 delay steps (5s, 30s, 2min)
 * across up to 3 retries, matching infra/rabbitmq/README.md's specified backoff schedule.
 *
 * Named decision (retry attempt count): infra/rabbitmq/README.md states "3 attempts with
 * 5s/30s/2min backoff". Read as 3 RETRIES following an initial attempt (the natural English
 * reading, matching 3 distinct backoff values for 3 delay-gaps), RabbitConfig configures
 * Spring Retry's maxAttempts(4) (1 initial + 3 retries). This policy produces the 3 delays
 * between attempts 1→2, 2→3, and 3→4 respectively.
 *
 * Named decision (no Thread.sleep in test): the delays (5s/30s/120s) are real production
 * values. The Tier2FailClosedTest does not exercise the retry path (it tests auth behavior,
 * not message retry), so no test-only sleep-skip override is needed.
 *
 * Identical implementation to notifications-service's plan 06-01 RabbitRetryBackOffPolicy,
 * adapted only to this service's package.
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
            throw new BackOffInterruptedException("Retry backoff interrupted", e);
        }
    }

    private static class Ctx implements BackOffContext {
        int index = 0;
    }
}
