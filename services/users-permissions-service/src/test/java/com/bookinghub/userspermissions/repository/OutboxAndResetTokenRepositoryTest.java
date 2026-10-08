package com.bookinghub.userspermissions.repository;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.domain.PasswordResetToken;
import com.bookinghub.userspermissions.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test proving V2 migration applies cleanly on V1, outbox/reset-token tables
 * work correctly, and case-insensitive email uniqueness is enforced. Uses the
 * running Postgres from docker-compose instead of Testcontainers due to Docker
 * API version compatibility issues in the sandbox environment.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OutboxAndResetTokenRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OutboxEventRepository outboxEventRepository;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Test
    void shouldStoreAndRetrieveOutboxEvent() {
        // Create a user (explicit UUID, no generation)
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "test@example.com", "Test User");
        userRepository.save(user);
        
        // Create outbox event
        OutboxEvent event = new OutboxEvent(
            "user",
            userId,
            "user.events",
            "user.created",
            "{\"userId\":\"" + userId + "\",\"email\":\"test@example.com\"}"
        );
        outboxEventRepository.save(event);
        
        // Retrieve pending events
        List<OutboxEvent> pending = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc("pending");
        
        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getAggregateType()).isEqualTo("user");
        assertThat(pending.get(0).getRoutingKey()).isEqualTo("user.created");
        assertThat(pending.get(0).getStatus()).isEqualTo("pending");
    }
    
    @Test
    void shouldStoreAndRetrievePasswordResetToken() {
        // Create a user
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "reset@example.com", "Reset User");
        userRepository.save(user);
        
        // Create reset token
        String tokenHash = "abc123hash";
        Instant expiresAt = Instant.now().plusSeconds(7200); // 2 hours
        PasswordResetToken token = new PasswordResetToken(userId, tokenHash, expiresAt);
        passwordResetTokenRepository.save(token);
        
        // Retrieve by hash
        PasswordResetToken found = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new AssertionError("Token not found"));
        
        assertThat(found.getUserId()).isEqualTo(userId);
        assertThat(found.getTokenHash()).isEqualTo(tokenHash);
        assertThat(found.isUsed()).isFalse();
        assertThat(found.isExpired()).isFalse();
    }
    
    @Test
    void shouldEnforceCaseInsensitiveEmailUniqueness() {
        // Create first user with lowercase email (use unique email to avoid collision with other tests)
        UUID userId1 = UUID.randomUUID();
        String uniqueEmail = "case-test-" + System.currentTimeMillis() + "@example.com";
        User user1 = new User(userId1, uniqueEmail, "User One");
        userRepository.save(user1);
        
        // Attempt to create second user with same email (different case)
        UUID userId2 = UUID.randomUUID();
        User user2 = new User(userId2, uniqueEmail.toUpperCase(), "User Two");
        
        // Should throw DataIntegrityViolationException due to uq_users_email_lower index
        assertThatThrownBy(() -> {
            userRepository.save(user2);
            userRepository.flush();  // Force immediate constraint check
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
    
    @Test
    void shouldFindUserByCaseInsensitiveEmail() {
        // Create user
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "findme@example.com", "Find Me");
        userRepository.save(user);
        
        // Find by different case
        User found = userRepository.findByEmailIgnoreCase("FINDME@EXAMPLE.COM")
            .orElseThrow(() -> new AssertionError("User not found"));
        
        assertThat(found.getId()).isEqualTo(userId);
        assertThat(found.getEmail()).isEqualTo("findme@example.com");
    }
}
