package com.bookinghub.userspermissions.repository;

import com.bookinghub.userspermissions.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PasswordResetToken entity.
 * 
 * Provides lookup by token hash for password reset submission validation.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    /**
     * Find password reset token by its hash.
     * Used during POST /auth/password-reset/complete to validate token,
     * check expiry, and enforce single-use.
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
