package com.bookinghub.customfield.repository;

import com.bookinghub.customfield.domain.CustomFieldJoin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomFieldJoinRepository extends JpaRepository<CustomFieldJoin, UUID> {

    List<CustomFieldJoin> findByCustomFieldTemplateId(UUID templateId);

    /**
     * Used by plan 04-04's template-update "replace the field_ids set" logic.
     */
    void deleteByCustomFieldTemplateId(UUID templateId);
}
