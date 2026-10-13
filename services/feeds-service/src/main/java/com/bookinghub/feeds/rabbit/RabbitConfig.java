package com.bookinghub.feeds.rabbit;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Collections;
import java.util.Map;

/**
 * RabbitMQ listener container factory for feeds-service.
 *
 * Configures the default 'rabbitListenerContainerFactory' bean so plan 07-02's
 * 2 @RabbitListener methods (feeds.booking.q, feeds.location.q) automatically
 * pick it up without needing an explicit containerFactory= attribute.
 *
 * Retry policy (identical to Phase 6's audit-log-service / notifications-service):
 * - maxAttempts(4): 1 initial attempt + 3 retries
 * - Backoff: 5s -> 30s -> 2min (RabbitRetryBackOffPolicy)
 * - Non-retryable: MissingIdempotencyKeyException -> routes straight to DLQ
 * - Final recovery: RejectAndDontRequeueRecoverer — causes broker-level
 *   basic.reject(requeue=false), which the already-declared x-dead-letter-exchange
 *   on each feeds queue routes to <queue>.dlx -> <queue>.dlq
 *   (per infra/rabbitmq/definitions.json's feeds.booking.q / feeds.location.q
 *   definitions — NO new RabbitMQ topology declared here).
 *
 * Named decision (no custom recoverer): unlike notifications-service (which tracks
 * delivery status in a DB table), the feeds projection has no per-message status
 * to track — a single dead-lettered event is a self-contained record of the gap,
 * surfaced via DLQ-depth monitoring per Y3-integrations.md. RejectAndDontRequeueRecoverer
 * is the correct recoverer; same posture as audit-log-service.
 *
 * Named decision (no publisher config): feeds-service is a PURE consumer — it never
 * publishes events, so no RabbitTemplate / outbox / message-converter publisher
 * config is needed here (also matching audit-log-service's consumer-only config).
 *
 * Named decision (NO security anywhere): feeds-service is the intentionally-public
 * service (F9.6). This config adds no auth of any kind — it only wires the consumer
 * retry/DLQ posture.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // On rejection, never requeue — let the broker's x-dead-letter-exchange route it.
        factory.setDefaultRequeueRejected(false);

        // Non-retryable exceptions: a message missing its idempotency key (or otherwise
        // structurally un-processable) can never succeed, so skip straight to DLQ without
        // wasting retry attempts.
        //
        // Classification subtlety (the reason this is a singleton map on the SUPERTYPE,
        // not an Exception->true blanket): Spring AMQP wraps a listener throwable in a
        // ListenerExecutionFailedException before the retry template classifies it. A
        // blanket {Exception->true} shadows a specific {MissingIdempotencyKeyException->false}
        // entry, because BinaryExceptionClassifier matches the WRAPPER's own hierarchy
        // (ListenerExecutionFailedException -> ... -> Exception == true) and returns BEFORE
        // ever inspecting the wrapped cause. The message is then retried through the full
        // 5s/30s/2min backoff instead of dead-lettering — defeating must-have truth #4.
        //
        // Fix: classify ONLY AmqpRejectAndDontRequeueException (MissingIdempotencyKeyException's
        // supertype) as non-retryable, with defaultValue=true so every OTHER exception stays
        // retryable, and traverseCauses=true so the classifier walks from the wrapper down to
        // the real cause (which is-a AmqpRejectAndDontRequeueException) and correctly returns
        // false -> immediate reject -> DLQ, no backoff.
        Map<Class<? extends Throwable>, Boolean> nonRetryable =
                Collections.singletonMap(AmqpRejectAndDontRequeueException.class, false);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(4, nonRetryable, true, true);
        // maxAttempts=4: 1 initial + 3 retries (5s/30s/2min backoff), matching
        // infra/rabbitmq/README.md read as "3 retries".

        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder.stateless()
                .retryPolicy(retryPolicy)
                .backOffPolicy(new RabbitRetryBackOffPolicy())
                .recoverer(new RejectAndDontRequeueRecoverer())
                // Final-exhaustion path: reject without requeue -> broker's existing
                // x-dead-letter-exchange on each feeds queue routes to <queue>.dlq
                // per infra/rabbitmq/definitions.json — no new topology declared here.
                .build();

        factory.setAdviceChain(interceptor);
        return factory;
    }
}
