package com.bookinghub.auditlog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookinghub.auditlog.domain.AuditLogEntry;
import com.bookinghub.auditlog.repository.AuditLogEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integration tests for AuditLogController.
 *
 * Tests the GET /audit-log endpoint with various filter combinations and role-based access control.
 * Uses MockMvc for HTTP testing with real database (Testcontainers PostgreSQL).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuditLogControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("audit_db_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogEntryRepository auditLogEntryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @BeforeEach
    void setUp() {
        // Clean up any leftover audit log entries from previous tests
        auditLogEntryRepository.deleteAll();
    }

    // ==================== Scenario 1: No filters, return all rows ====================

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testScenario1_NoFilters_ReturnsAllSeededRows() throws Exception {
        // Seed several audit entries directly via the repository
        UUID entityId1 = UUID.randomUUID();
        UUID entityId2 = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();

        AuditLogEntry entry1 = new AuditLogEntry();
        entry1.setIdempotencyKey(UUID.randomUUID());
        entry1.setEntityType("booking");
        entry1.setActionType("created");
        entry1.setEntityId(entityId1);
        entry1.setActorId(actorId);
        entry1.setOccurredAt(now);
        entry1.setAfterValues("{}");

        AuditLogEntry entry2 = new AuditLogEntry();
        entry2.setIdempotencyKey(UUID.randomUUID());
        entry2.setEntityType("location");
        entry2.setActionType("updated");
        entry2.setEntityId(entityId2);
        entry2.setActorId(actorId);
        entry2.setOccurredAt(now.plusSeconds(60));
        entry2.setAfterValues("{}");

        auditLogEntryRepository.save(entry1);
        auditLogEntryRepository.save(entry2);

        // GET /audit-log with no filters
        mockMvc.perform(get("/audit-log").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].entityType").value("booking"))
                .andExpect(jsonPath("$[1].entityType").value("location"));
    }

    // ==================== Scenario 2: Filter by entity_type ====================

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testScenario2_FilterByEntityType() throws Exception {
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();

        // Seed two entries with different entity types
        AuditLogEntry bookingEntry = new AuditLogEntry();
        bookingEntry.setIdempotencyKey(UUID.randomUUID());
        bookingEntry.setEntityType("booking");
        bookingEntry.setActionType("created");
        bookingEntry.setEntityId(UUID.randomUUID());
        bookingEntry.setActorId(actorId);
        bookingEntry.setOccurredAt(now);
        bookingEntry.setAfterValues("{}");

        AuditLogEntry locationEntry = new AuditLogEntry();
        locationEntry.setIdempotencyKey(UUID.randomUUID());
        locationEntry.setEntityType("location");
        locationEntry.setActionType("created");
        locationEntry.setEntityId(UUID.randomUUID());
        locationEntry.setActorId(actorId);
        locationEntry.setOccurredAt(now);
        locationEntry.setAfterValues("{}");

        auditLogEntryRepository.save(bookingEntry);
        auditLogEntryRepository.save(locationEntry);

        // GET /audit-log?entity_type=booking
        mockMvc.perform(get("/audit-log?entity_type=booking").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityType").value("booking"));
    }

    // ==================== Scenario 3: Invalid date range (from > to) ====================

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testScenario3_InvalidDateRange_Returns400() throws Exception {
        // GET /audit-log?date_from=2026-02-01&date_to=2026-01-01 (inverted)
        mockMvc.perform(get("/audit-log?date_from=2026-02-01&date_to=2026-01-01")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_code").value("AUDIT_LOG_INVALID_DATE_RANGE"));
    }

    // ==================== Scenario 4: No role_audit_viewer → 403 AUDIT_LOG_FORBIDDEN ====================

    @Test
    @WithMockUser(username = "user1") // No role_audit_viewer
    void testScenario4_NoAuditViewerRole_Returns403WithForbiddenCode() throws Exception {
        // GET /audit-log without role_audit_viewer
        mockMvc.perform(get("/audit-log").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error_code").value("AUDIT_LOG_FORBIDDEN"));
    }

    // ==================== Scenario 5: No bearer token → 401 AUTH_UNAUTHENTICATED ====================

    @Test
    void testScenario5_NoAuthToken_Returns401() throws Exception {
        // GET /audit-log without any authentication
        mockMvc.perform(get("/audit-log").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // ==================== Scenario 6: No mutating methods (GET-only controller) ====================

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testScenario6_PutAttempt_Returns405MethodNotAllowed() throws Exception {
        UUID fakeId = UUID.randomUUID();

        // PUT /audit-log/{id} should not be mapped
        mockMvc.perform(put("/audit-log/" + fakeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testScenario6_DeleteAttempt_Returns405MethodNotAllowed() throws Exception {
        UUID fakeId = UUID.randomUUID();

        // DELETE /audit-log/{id} should not be mapped
        mockMvc.perform(delete("/audit-log/" + fakeId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testScenario6_PostAttempt_Returns405MethodNotAllowed() throws Exception {
        // POST /audit-log should not be mapped
        mockMvc.perform(post("/audit-log")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== Additional: Date range filtering ====================

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testDateRangeFiltering() throws Exception {
        // Seed entries with different timestamps
        LocalDate date1 = LocalDate.of(2026, 1, 1);
        LocalDate date2 = LocalDate.of(2026, 1, 15);
        LocalDate date3 = LocalDate.of(2026, 2, 1);

        Instant instant1 = date1.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant instant2 = date2.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant instant3 = date3.atStartOfDay(ZoneId.systemDefault()).toInstant();

        UUID actorId = UUID.randomUUID();

        AuditLogEntry entry1 = createEntry("booking", instant1, actorId);
        AuditLogEntry entry2 = createEntry("location", instant2, actorId);
        AuditLogEntry entry3 = createEntry("resource", instant3, actorId);

        auditLogEntryRepository.save(entry1);
        auditLogEntryRepository.save(entry2);
        auditLogEntryRepository.save(entry3);

        // GET /audit-log?date_from=2026-01-10&date_to=2026-01-20
        mockMvc.perform(get("/audit-log?date_from=2026-01-10&date_to=2026-01-20")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].entityType").value("location"));
    }

    // ==================== Additional: Filter by actor ====================

    @Test
    @WithMockUser(username = "user1", authorities = {"role_audit_viewer"})
    void testFilterByActor() throws Exception {
        UUID actor1 = UUID.randomUUID();
        UUID actor2 = UUID.randomUUID();
        Instant now = Instant.now();

        AuditLogEntry entry1 = createEntryWithActor("booking", now, actor1);
        AuditLogEntry entry2 = createEntryWithActor("location", now, actor2);

        auditLogEntryRepository.save(entry1);
        auditLogEntryRepository.save(entry2);

        // GET /audit-log?actor={actor1}
        mockMvc.perform(get("/audit-log?actor=" + actor1)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].actorId").value(actor1.toString()));
    }

    // ==================== Helper Methods ====================

    private AuditLogEntry createEntry(String entityType, Instant occurredAt, UUID actorId) {
        AuditLogEntry entry = new AuditLogEntry();
        entry.setIdempotencyKey(UUID.randomUUID());
        entry.setEntityType(entityType);
        entry.setActionType("created");
        entry.setEntityId(UUID.randomUUID());
        entry.setActorId(actorId);
        entry.setOccurredAt(occurredAt);
        entry.setAfterValues("{}");
        return entry;
    }

    private AuditLogEntry createEntryWithActor(String entityType, Instant occurredAt, UUID actorId) {
        return createEntry(entityType, occurredAt, actorId);
    }
}
