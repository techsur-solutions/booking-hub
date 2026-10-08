package com.bookinghub.locationsresources.repository;

import com.bookinghub.locationsresources.domain.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Resource entity.
 *
 * Soft-delete-aware query methods: all read paths exclude soft-deleted rows
 * (deleted_at IS NOT NULL) — plan 04-02's controllers build CRUD on top of these.
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findAllByDeletedAtIsNull();

    Optional<Resource> findByIdAndDeletedAtIsNull(UUID id);
}
