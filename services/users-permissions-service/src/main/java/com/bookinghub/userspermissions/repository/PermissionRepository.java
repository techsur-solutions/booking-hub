package com.bookinghub.userspermissions.repository;

import com.bookinghub.userspermissions.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission entity.
 * 
 * Provides lookup by legacy flag and Keycloak role for permission enforcement.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    
    /**
     * Find permission by legacy flag name.
     * Used to look up the Keycloak role mapping for a given legacy permission.
     */
    Optional<Permission> findByLegacyFlag(String legacyFlag);
    
    /**
     * Find all permissions mapped to a Keycloak role.
     * Used for reverse lookup and role-based permission enumeration.
     */
    List<Permission> findByKeycloakRole(String keycloakRole);
}
