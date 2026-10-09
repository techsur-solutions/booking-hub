package com.bookinghub.customfield.repository;

import com.bookinghub.customfield.domain.CustomField;
import com.bookinghub.customfield.domain.CustomFieldTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the entity mappings are correct (including the V2-added `required`
 * column) AND proves the findApplicableToContext OR-is-null query returns both
 * global and context-specific templates — the exact mechanism plan 04-04's
 * applicability endpoint depends on.
 *
 * Uses the running Postgres from docker-compose instead of Testcontainers, due
 * to Docker API version compatibility issues in the sandbox environment (same
 * deviation already established in Phase 3 plans 03-01/03-05).
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DomainRepositoryTest {

    @Autowired
    private CustomFieldRepository customFieldRepository;

    @Autowired
    private CustomFieldTemplateRepository customFieldTemplateRepository;

    @Test
    void shouldPersistAndRoundTripAllCustomFieldFields() {
        CustomField field = new CustomField("Dietary Requirements", "select", List.of("a", "b"), true);
        CustomField saved = customFieldRepository.save(field);

        CustomField found = customFieldRepository.findByIdAndDeletedAtIsNull(saved.getId())
            .orElseThrow(() -> new AssertionError("CustomField not found"));

        assertThat(found.getLabel()).isEqualTo("Dietary Requirements");
        assertThat(found.getFieldType()).isEqualTo("select");
        assertThat(found.getOptions()).containsExactly("a", "b");
        assertThat(found.isRequired()).isTrue();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getDeletedAt()).isNull();
    }

    @Test
    void findApplicableToContext_returnsBothGlobalAndContextSpecificTemplates() {
        UUID contextId = UUID.randomUUID();

        CustomFieldTemplate global = new CustomFieldTemplate(
            "Global Template " + UUID.randomUUID(), null);
        CustomFieldTemplate contextSpecific = new CustomFieldTemplate(
            "Context Template " + UUID.randomUUID(), contextId);

        customFieldTemplateRepository.save(global);
        customFieldTemplateRepository.save(contextSpecific);

        List<CustomFieldTemplate> applicable = customFieldTemplateRepository.findApplicableToContext(contextId);

        assertThat(applicable)
            .extracting(CustomFieldTemplate::getId)
            .contains(global.getId(), contextSpecific.getId());
    }
}
