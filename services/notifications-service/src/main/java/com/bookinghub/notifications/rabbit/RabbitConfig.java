package com.bookinghub.notifications.rabbit;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ listener container factory configuration.
 *
 * Implements infra/rabbitmq/README.md's exact retry policy:
 * "3 attempts with 5s/30s/2min backoff" — read as 3 RETRIES following
 * an initial attempt (natural English reading, gives exactly 3 distinct
 * backoff values for exactly 3 delay-gaps) → maxAttempts(4).
 *
 * DLQ routing: The DatabaseTrackingMessageRecoverer (plan 06-02) looks up and
 * marks the notification_deliveries row as 'dead_lettered', then delegates to
 * RejectAndDontRequeueRecoverer for the actual broker-level reject-to-DLQ.
 * The already-declared queue arguments (x-dead-letter-exchange on
 * notifications.booking.q and notifications.passwordreset.q, defined in
 * infra/rabbitmq/definitions.json) route the rejected message to
 * {@code <queue>.dlx} → {@code <queue>.dlq}. No new RabbitMQ topology is declared here.
 *
 * Non-retryable exceptions: MissingIdempotencyKeyException and
 * UnresolvableRecipientException skip all retry cycles and go straight to DLQ
 * (registered with retryable=false in SimpleRetryPolicy's exception map).
 *
 * Bean name: MUST be "rabbitListenerContainerFactory" (Spring AMQP's default lookup
 * name) so plan 06-02's @RabbitListener methods pick it up without needing an
 * explicit containerFactory= attribute.
 */
@Configuration
public class RabbitConfig {

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DatabaseTrackingMessageRecoverer databaseTrackingMessageRecoverer) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        // Exception map: false = non-retryable (skip straight to DLQ), true = retryable
        Map<Class<? extends Throwable>, Boolean> retryable = new HashMap<>();
        retryable.put(MissingIdempotencyKeyException.class, false);
        retryable.put(UnresolvableRecipientException.class, false);
        retryable.put(Exception.class, true);

        // 4 total attempts: 1 initial + 3 retries; traverseCauses=true so cause chain
        // is inspected (AmqpRejectAndDontRequeueException wrapping is common in AMQP)
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(4, retryable, true);

        RetryOperationsInterceptor interceptor = RetryInterceptorBuilder.stateless()
                .retryPolicy(retryPolicy)
                .backOffPolicy(new RabbitRetryBackOffPolicy())
                // Final exhaustion / non-retryable path: DatabaseTrackingMessageRecoverer
                // marks the row dead_lettered, then delegates reject-without-requeue to DLQ
                .recoverer(databaseTrackingMessageRecoverer)
                .build();

        factory.setAdviceChain(interceptor);
        return factory;
    }
}
