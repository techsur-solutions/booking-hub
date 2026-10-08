package com.bookinghub.userspermissions.controller;

import com.bookinghub.userspermissions.domain.OutboxEvent;
import com.bookinghub.userspermissions.repository.OutboxEventRepository;
import com.bookinghub.userspermissions.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for PermissionController.
 * 
 * Verifies:
 * 1. Admin caller can list all permissions
 * 2. Admin caller can get role permissions
 * 3. Admin caller gets 400 PERMISSION_FLAG_UNDEFINED when assigning an unconfirmed flag
 * 4. Admin caller successfully assigns a confirmed flag and creates an OutboxEvent
 * 5. Non-admin caller gets 403 PERMISSIONS_FORBIDDEN specifically (not AUTH_FORBIDDEN)
 * 
 * Proves F7.4/F0-mandated deny-by-default enforcement against the REAL seeded
 * accessresources (confirmed=false) flag from plan 03-01's seed data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PermissionControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        // Disable RabbitMQ for this test (OutboxPublisher @Scheduled won't run)
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private OutboxEventRepository outboxEventRepository;
    
    /**
     * Scenario 1: Admin caller can list all permissions.
     * GET /api/permissions → 200 with all 17 seeded rows.
     */
    @Test
    void testAdminCanListPermissions() throws Exception {
        mockMvc.perform(get("/api/permissions")
                .with(jwt().authorities(() -> "role_permissions_admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(17)); // 17 rows from seed
    }
    
    /**
     * Scenario 2: Admin caller can get role permissions.
     * GET /api/roles/role_calendar_viewer/permissions → 200 with accessCalendar flag.
     */
    @Test
    void testAdminCanGetRolePermissions() throws Exception {
        mockMvc.perform(get("/api/roles/role_calendar_viewer/permissions")
                .with(jwt().authorities(() -> "role_permissions_admin")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roleName").value("role_calendar_viewer"))
            .andExpect(jsonPath("$.permissionFlags").isArray())
            .andExpect(jsonPath("$.permissionFlags[0]").value("accessCalendar"));
    }
    
    /**
     * Scenario 3: Admin caller gets 400 PERMISSION_FLAG_UNDEFINED when assigning
     * an unconfirmed flag.
     * 
     * PUT /api/roles/role_location_admin/permissions with accessresources (confirmed=false)
     * → 400 PERMISSION_FLAG_UNDEFINED.
     * 
     * Proves deny-by-default enforcement against the REAL unconfirmed flag from
     * plan 03-01's seed data, not a synthetic/hypothetical flag.
     */
    @Test
    void testUnconfirmedFlagRejected() throws Exception {
        mockMvc.perform(put("/api/roles/role_location_admin/permissions")
                .with(jwt().authorities(() -> "role_permissions_admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionFlags\": [\"accessresources\"]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error_code").value("PERMISSION_FLAG_UNDEFINED"))
            .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("not confirmed")));
    }
    
    /**
     * Scenario 4: Admin caller successfully assigns a confirmed flag and creates
     * an OutboxEvent.
     * 
     * PUT /api/roles/role_location_admin/permissions with accessCalendar (confirmed=true)
     * → 200, OutboxEvent row created with routing_key="permission.updated".
     */
    @Test
    void testConfirmedFlagAssigned() throws Exception {
        // Clear outbox to isolate this test's event
        outboxEventRepository.deleteAll();
        
        mockMvc.perform(put("/api/roles/role_location_admin/permissions")
                .with(jwt().authorities(() -> "role_permissions_admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionFlags\": [\"accessCalendar\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roleName").value("role_location_admin"))
            .andExpect(jsonPath("$.permissionFlags[0]").value("accessCalendar"));
        
        // Assert OutboxEvent created
        List<OutboxEvent> events = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc("pending");
        assertThat(events).hasSize(1);
        
        OutboxEvent event = events.get(0);
        assertThat(event.getExchange()).isEqualTo("permission.events");
        assertThat(event.getRoutingKey()).isEqualTo("permission.updated");
        assertThat(event.getPayload()).contains("role_location_admin");
        assertThat(event.getPayload()).contains("accessCalendar");
    }
    
    /**
     * Scenario 5: Non-admin caller gets 403 PERMISSIONS_FORBIDDEN specifically
     * (not AUTH_FORBIDDEN).
     * 
     * Verifies manual hasRole("role_permissions_admin") checks throw
     * PermissionsForbiddenException directly, bypassing the generic
     * @PreAuthorize/AccessDeniedException path.
     */
    @Test
    void testNonAdminGetsForbidden() throws Exception {
        // Caller with role_user_admin (not role_permissions_admin)
        mockMvc.perform(get("/api/permissions")
                .with(jwt().authorities(() -> "role_user_admin")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error_code").value("PERMISSIONS_FORBIDDEN"));
        
        mockMvc.perform(get("/api/roles/role_calendar_viewer/permissions")
                .with(jwt().authorities(() -> "role_user_admin")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error_code").value("PERMISSIONS_FORBIDDEN"));
        
        mockMvc.perform(put("/api/roles/role_location_admin/permissions")
                .with(jwt().authorities(() -> "role_user_admin"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"permissionFlags\": [\"accessCalendar\"]}"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error_code").value("PERMISSIONS_FORBIDDEN"));
    }
}
