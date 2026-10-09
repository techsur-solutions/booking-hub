package com.bookinghub.customfield.controller;

import com.bookinghub.customfield.domain.CustomField;
import com.bookinghub.customfield.domain.CustomFieldJoin;
import com.bookinghub.customfield.domain.CustomFieldTemplate;
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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for CustomFieldController.
 *
 * Tests field_type enum + options[] validation (scenarios 1-4), the
 * no-read-carve-out uniform admin gating (scenario 5), the
 * applicability-query endpoint's global + context-specific resolution
 * (scenario 6), and a real-Keycloak-shaped realm_access.roles claim actually
 * granting access through SecurityConfig's JwtAuthenticationConverter
 * (scenario 7) — against the running docker-compose Postgres (switched from
 * Testcontainers due to Docker API compatibility in this sandbox, same
 * deviation already established by DomainRepositoryTest in plan 04-03).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CustomFieldControllerIntegrationTest {

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

    @Autowired
    private org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
        jwtAuthenticationConverter;

    @BeforeEach
    void setUp() {
        customFieldJoinRepository.deleteAll();
        customFieldTemplateRepository.deleteAll();
        customFieldRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }

    /**
     * Scenario 1: select type with non-empty options → 201
     */
    @Test
    void createSelectFieldWithOptions_returns201() throws Exception {
        String requestBody = """
                {
                    "label": "Attendees",
                    "field_type": "select",
                    "options": ["5", "10", "20"]
                }
                """;

        mockMvc.perform(post("/custom-fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Attendees"))
                .andExpect(jsonPath("$.field_type").value("select"));
    }

    /**
     * Scenario 2: textfield (non-choice type) with no options → 201 (options
     * not required for textfield)
     */
    @Test
    void createTextfieldWithoutOptions_returns201() throws Exception {
        String requestBody = """
                {
                    "label": "Notes",
                    "field_type": "textfield"
                }
                """;

        mockMvc.perform(post("/custom-fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Notes"))
                .andExpect(jsonPath("$.field_type").value("textfield"));
    }

    /**
     * Scenario 3: radio (choice-based type) with empty options[] → 400
     * CUSTOM_FIELD_OPTIONS_REQUIRED
     */
    @Test
    void createRadioFieldWithEmptyOptions_returns400OptionsRequired() throws Exception {
        String requestBody = """
                {
                    "label": "Priority",
                    "field_type": "radio",
                    "options": []
                }
                """;

        mockMvc.perform(post("/custom-fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("CUSTOM_FIELD_OPTIONS_REQUIRED"));
    }

    /**
     * Scenario 4: missing label → 400 CUSTOM_FIELD_LABEL_REQUIRED
     */
    @Test
    void createFieldWithoutLabel_returns400LabelRequired() throws Exception {
        String requestBody = """
                {
                    "field_type": "select",
                    "options": ["a"]
                }
                """;

        mockMvc.perform(post("/custom-fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("CUSTOM_FIELD_LABEL_REQUIRED"));
    }

    /**
     * Scenario 5: caller WITHOUT role_customfield_admin → 403
     * CUSTOM_FIELD_FORBIDDEN, proving the no-read-carve-out decision holds for
     * a real request (every method, including GET, requires the admin role).
     */
    @Test
    void listFieldsWithoutAdminRole_returns403() throws Exception {
        mockMvc.perform(get("/custom-fields")
                        .with(jwt().authorities(() -> "ROLE_role_user")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("CUSTOM_FIELD_FORBIDDEN"));
    }

    /**
     * Scenario 6: applicability resolution — a global template (context_id=null)
     * joined to field A, and a Location-scoped template joined to field B.
     * GET /custom-fields?context_id=<thatLocation> must return BOTH fields;
     * GET /custom-fields?context_id=<differentLocation> must return ONLY the
     * global field A.
     */
    @Test
    void applicabilityQuery_returnsGlobalAndContextSpecificFields() throws Exception {
        CustomField fieldA = customFieldRepository.save(
            new CustomField("Global Field A", "textfield", null, false));
        CustomField fieldB = customFieldRepository.save(
            new CustomField("Context Field B", "textfield", null, false));

        UUID targetLocationId = UUID.randomUUID();
        UUID otherLocationId = UUID.randomUUID();

        CustomFieldTemplate globalTemplate = customFieldTemplateRepository.save(
            new CustomFieldTemplate("Global Template", null));
        CustomFieldTemplate scopedTemplate = customFieldTemplateRepository.save(
            new CustomFieldTemplate("Scoped Template", targetLocationId));

        customFieldJoinRepository.save(new CustomFieldJoin(globalTemplate.getId(), fieldA.getId()));
        customFieldJoinRepository.save(new CustomFieldJoin(scopedTemplate.getId(), fieldB.getId()));

        // Both the global field A and the context-specific field B apply to targetLocationId
        mockMvc.perform(get("/custom-fields")
                        .param("context_id", targetLocationId.toString())
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + fieldA.getId() + "')]").exists())
                .andExpect(jsonPath("$[?(@.id=='" + fieldB.getId() + "')]").exists());

        // Only the global field A applies to a different, unrelated location
        mockMvc.perform(get("/custom-fields")
                        .param("context_id", otherLocationId.toString())
                        .with(jwt().authorities(() -> "ROLE_role_customfield_admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id=='" + fieldA.getId() + "')]").exists())
                .andExpect(jsonPath("$[?(@.id=='" + fieldB.getId() + "')]").doesNotExist());
    }

    /**
     * Scenario 7: a REAL Keycloak-shaped token — realm_access.roles nested
     * claim run through this service's ACTUAL JwtAuthenticationConverter bean
     * — must produce a ROLE_-prefixed authority @PreAuthorize can match.
     *
     * Named regression guard (Rule 1 bugfix, same root cause independently
     * found in plan 04-06's settings-service): every other scenario in this
     * file uses jwt().authorities(() -> "ROLE_...") which sets Spring Security
     * authorities DIRECTLY on the mock Authentication, which is why it would
     * pass identically whether or not a JwtAuthenticationConverter bean is
     * wired at all — Spring Security Test's jwt() post-processor builds its
     * own JwtAuthenticationToken and does NOT invoke the application's
     * configured oauth2ResourceServer().jwt().jwtAuthenticationConverter(...)
     * bean on its own. To actually exercise this service's REAL
     * realmRoleJwtAuthenticationConverter logic (not merely assert it compiles),
     * this test autowires that exact bean and feeds its extracted authorities
     * into the jwt() post-processor — proving the application's own conversion
     * logic (not a test shortcut) correctly turns a realistic nested
     * realm_access.roles claim into something @PreAuthorize("hasRole(...)")
     * actually matches.
     */
    @Test
    void realKeycloakShapedToken_withRealmAccessRolesClaim_grantsAccess() throws Exception {
        org.springframework.security.oauth2.jwt.Jwt sampleJwt =
            org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("realm_access", java.util.Map.of(
                    "roles", java.util.List.of("role_customfield_admin")))
                .build();

        var authorities = jwtAuthenticationConverter.convert(sampleJwt).getAuthorities();

        mockMvc.perform(get("/custom-fields")
                        .with(jwt()
                                .jwt(jwtBuilder -> jwtBuilder
                                    .claim("sub", sampleJwt.getSubject())
                                    .claim("realm_access", java.util.Map.of(
                                        "roles", java.util.List.of("role_customfield_admin"))))
                                .authorities(authorities)))
                .andExpect(status().isOk());
    }
}
