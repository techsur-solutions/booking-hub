package com.bookinghub.customfield.controller;

import com.bookinghub.customfield.domain.CustomField;
import com.bookinghub.customfield.repository.CustomFieldJoinRepository;
import com.bookinghub.customfield.repository.CustomFieldRepository;
import com.bookinghub.customfield.repository.CustomFieldTemplateRepository;
import com.bookinghub.customfield.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for FieldTemplateController.
 *
 * Proves: a template created with 2 field_ids returns both on GET; a PUT with
 * a different 1-field list REPLACES (not appends to) the join set; DELETE
 * cascades the join rows while leaving the underlying CustomField rows
 * untouched (hard delete on the template, matching the "no deleted_at column"
 * named distinction from plan 04-03).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class FieldTemplateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomFieldRepository customFieldRepository;

    @Autowired
    private CustomFieldTemplateRepository customFieldTemplateRepository;

    @Autowired
    private CustomFieldJoinRepository customFieldJoinRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void setUp() {
        customFieldJoinRepository.deleteAll();
        customFieldTemplateRepository.deleteAll();
        customFieldRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }

    @Test
    void createTemplateWithTwoFields_thenReplaceWithOneField_thenDelete_cascadesJoinsOnly() throws Exception {
        CustomField fieldOne = customFieldRepository.save(
            new CustomField("Field One", "textfield", null, false));
        CustomField fieldTwo = customFieldRepository.save(
            new CustomField("Field Two", "textfield", null, false));
        CustomField fieldThree = customFieldRepository.save(
            new CustomField("Field Three (untouched)", "textfield", null, false));

        // Create a template with 2 field_ids
        String createBody = """
                {
                    "name": "Event Template",
                    "field_ids": ["%s", "%s"]
                }
                """.formatted(fieldOne.getId(), fieldTwo.getId());

        String createResponse = mockMvc.perform(post("/field-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.field_ids", org.hamcrest.Matchers.hasSize(2)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String templateId = com.jayway.jsonpath.JsonPath.read(createResponse, "$.id");

        // GET /field-templates/{id} returns both fields
        mockMvc.perform(get("/field-templates/" + templateId)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.field_ids", org.hamcrest.Matchers.hasSize(2)));

        // PUT with a different 1-field list — REPLACE semantics, not additive
        String updateBody = """
                {
                    "name": "Event Template",
                    "field_ids": ["%s"]
                }
                """.formatted(fieldThree.getId());

        mockMvc.perform(put("/field-templates/" + templateId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.field_ids", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.field_ids[0]").value(fieldThree.getId().toString()));

        // DELETE → 204
        mockMvc.perform(delete("/field-templates/" + templateId)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isNoContent());

        // Underlying custom_field_joins rows are gone too (cascade)
        List<UUID> remainingJoinTemplateIds = customFieldJoinRepository.findAll().stream()
            .map(join -> join.getCustomFieldTemplateId())
            .toList();
        assertThat(remainingJoinTemplateIds).doesNotContain(UUID.fromString(templateId));

        // A separately-created CustomField's own row is untouched
        assertThat(customFieldRepository.findByIdAndDeletedAtIsNull(fieldThree.getId())).isPresent();
        assertThat(customFieldRepository.findByIdAndDeletedAtIsNull(fieldOne.getId())).isPresent();
        assertThat(customFieldRepository.findByIdAndDeletedAtIsNull(fieldTwo.getId())).isPresent();
    }
}
