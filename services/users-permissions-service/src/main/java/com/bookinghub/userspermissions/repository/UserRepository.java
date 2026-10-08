package com.bookinghub.userspermissions.repository;

import com.bookinghub.userspermissions.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity.
 * 
 * Adds findByEmailIgnoreCase to support case-insensitive email uniqueness
 * enforcement (backed by V2 migration's uq_users_email_lower index).
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email (case-insensitive).
     * Backs the case-insensitive email uniqueness decision from F0 OQ #21.
     */
    Optional<User> findByEmailIgnoreCase(String email);
}
