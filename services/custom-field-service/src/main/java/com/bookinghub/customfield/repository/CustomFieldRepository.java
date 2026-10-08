package com.bookinghub.customfield.repository;

import com.bookinghub.customfield.domain.CustomField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomFieldRepository extends JpaRepository<CustomField, UUID> {

    List<CustomField> findAllByDeletedAtIsNull();

    Optional<CustomField> findByIdAndDeletedAtIsNull(UUID id);
}
