package com.bookinghub.customfield.repository;

import com.bookinghub.customfield.domain.CustomFieldTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CustomFieldTemplateRepository extends JpaRepository<CustomFieldTemplate, UUID> {

    List<CustomFieldTemplate> findAll();

    /**
     * The applicability-query backing method plan 04-04 Task 2 uses. Needs a
     * custom @Query since Spring Data can't express "equals X OR is null" as a
     * derived method name cleanly.
     */
    @Query("SELECT t FROM CustomFieldTemplate t WHERE t.contextId = :contextId OR t.contextId IS NULL")
    List<CustomFieldTemplate> findApplicableToContext(@Param("contextId") UUID contextId);
}
