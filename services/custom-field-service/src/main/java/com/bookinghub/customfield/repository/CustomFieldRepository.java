package com.bookinghub.customfield.repository;

import com.bookinghub.customfield.domain.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomFieldRepository extends JpaRepository<CustomField, UUID> {

    List<CustomField> findAllByDeletedAtIsNull();

    Optional<CustomField> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Optimized query to fetch all custom fields applicable to a given context
     * in a single query with JOINs, avoiding N+1 problem. Returns distinct fields
     * that are joined to templates applicable to the context (contextId = :contextId OR contextId IS NULL).
     * Uses native SQL because CustomFieldJoin has no @ManyToOne relationships (plain UUIDs only).
     */
    @Query(value = "SELECT DISTINCT cf.* FROM custom_fields cf " +
           "JOIN custom_field_joins cfj ON cfj.custom_field_id = cf.id " +
           "JOIN custom_field_templates cft ON cft.id = cfj.custom_field_template_id " +
           "WHERE (cft.context_id = :contextId OR cft.context_id IS NULL) " +
           "AND cf.deleted_at IS NULL", 
           nativeQuery = true)
    List<CustomField> findApplicableToContext(@Param("contextId") UUID contextId);
}
