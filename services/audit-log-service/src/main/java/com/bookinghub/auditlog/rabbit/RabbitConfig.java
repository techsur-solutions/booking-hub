package com.bookinghub.auditlog.rabbit;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ listener container factory for audit-log-service.
 *
 * Configures the default 'rabbitListenerContainerFactory' bean so plan 06-04's
 * 7 @RabbitListener methods (one per audit queue) automatically pick it up without
 * needing an explicit containerFactory= attribute.
 *
 * Retry policy (identical to notifications-service plan 06-01):
 * - maxAttempts(4): 1 initial attempt + 3 retries
 * - Backoff: 5s → 30s → 2min (RabbitRetryBackOffPolicy)
 * - Non-retryable: MissingIdempotencyKeyException → routes straight to DLQ
 * - Final recovery: RejectAndDontRequeueRecoverer — causes broker-level
 *   basic.reject(requeue=false), which the already-declared x-dead-letter-exchange
 *   on each queue routes to <queue>.dlx → <queue>.dlq
 *   (per infra/rabbitmq/definitions.json's 7 audit.*.q queue definitions —
 *   no new RabbitMQ topology declared here).
 *
 * Named decision (no per-row status tracking / no custom recoverer): unlike
 * notifications-service (which tracks delivery status in notification_deliveries),
 * audit_log_entries is a pure append-only immutable fact table with no "pending"
 * concept. The dead-letter message itself is the complete record of the processing gap,
 * consistent with F11 §Error States' "surfaced via observability/alerting, not a
 * client-facing error" disposition. RejectAndDontRequeueRecoverer is the correct
 * recoverer for this service — no custom recoverer needed.
 *
 * Named decision (7 queues sharing this factory): audit-log-service receives events
 * from all 7 business domains (booking, location, resource, user, role, settings,
 * permission). Each domain has its own queue in infra/rabbitmq/definitions.json. All
 * 7 @RabbitListener methods in plan 06-04 share this single container factory bean —
 * Spring AMQP's default factory-name lookup ("rabbitListenerContainerFactory") ensures
 * this works with zero explicit configuration on each listener.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // Non-retryable exceptions: a message missing its idempotency key can never
        // succeed, so skip straight to DLQ without wasting retry attempts.
        //
        // IMPORTANT — why the map holds ONLY the non-retryable entry, and why defaultValue=true:
        // Spring AMQP wraps whatever a listener throws in a ListenerExecutionFailedException
        // before the retry interceptor sees it. Spring Retry's BinaryExceptionClassifier
        // classifies that WRAPPER first and only walks to the cause when the wrapper's own
        // classification equals the classifier's default. An earlier shape of this config also
        // mapped Exception -> true; the wrapper's closest match was then that Exception entry,
        // so the classifier answered "retryable" and never looked at the cause — making the
        // MissingIdempotencyKeyException -> false entry unreachable dead code, and sending every
        // malformed message through the full 5s+30s+2min backoff before it dead-lettered.
        //
        // With defaultValue=true, the wrapper matches nothing explicitly, classifies as the
        // default, and the classifier therefore TRAVERSES to the real cause — where
        // MissingIdempotencyKeyException -> false correctly fires and the message is rejected
        // immediately. Unmatched (genuinely transient) failures still default to retryable,
        // which is the behaviour the Exception -> true entry was there to express.
        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(MissingIdempotencyKeyException.class, false);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(4, retryable, true, true);
        // maxAttempts=4: 1 initial + 3 retries, matching "3 attempts with 5s/30s/2min backoff"
        // from infra/rabbitmq/README.md (reading "3 retries" not "3 total attempts")

        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder.stateless()
                .retryPolicy(retryPolicy)
                .backOffPolicy(new RabbitRetryBackOffPolicy())
                .recoverer(new RejectAndDontRequeueRecoverer())
                // Final-exhaustion path: reject without requeue -> broker's existing
                // x-dead-letter-exchange on each audit queue routes to <queue>.dlq
                // per infra/rabbitmq/definitions.json — no new topology declared here.
                .build();

        factory.setAdviceChain(interceptor);
        return factory;
    }
}
