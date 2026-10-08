package com.bookinghub.userspermissions.repository;

import com.bookinghub.userspermissions.domain.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test proving V2 migration's 17-row permission seed applies correctly
 * and confirms the F0 audit inventory. Uses the running Postgres from
 * docker-compose instead of Testcontainers due to Docker API version
 * compatibility issues in the sandbox environment.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PermissionSeedDataTest {
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Test
    void shouldSeedExactly17Permissions() {
        List<Permission> all = permissionRepository.findAll();
        assertThat(all).hasSize(17);
    }
    
    @Test
    void shouldMarkAccessResourcesUnconfirmed() {
        Permission accessResources = permissionRepository.findByLegacyFlag("accessresources")
            .orElseThrow(() -> new AssertionError("accessresources flag not found"));
        
        assertThat(accessResources.isConfirmed()).isFalse();
        assertThat(accessResources.getGatedActions()).contains("ambiguity");
    }
    
    @Test
    void shouldMapAccessCalendarCorrectly() {
        Permission accessCalendar = permissionRepository.findByLegacyFlag("accessCalendar")
            .orElseThrow(() -> new AssertionError("accessCalendar flag not found"));
        
        assertThat(accessCalendar.getKeycloakRole()).isEqualTo("role_calendar_viewer");
        assertThat(accessCalendar.isConfirmed()).isTrue();
    }
    
    @Test
    void shouldIncludeDeadFlagsAsUnconfirmed() {
        Permission allowiCal = permissionRepository.findByLegacyFlag("allowiCal")
            .orElseThrow(() -> new AssertionError("allowiCal flag not found"));
        Permission allowRSS = permissionRepository.findByLegacyFlag("allowRSS")
            .orElseThrow(() -> new AssertionError("allowRSS flag not found"));
        
        assertThat(allowiCal.isConfirmed()).isFalse();
        assertThat(allowRSS.isConfirmed()).isFalse();
        assertThat(allowiCal.getKeycloakRole()).contains("reserved");
        assertThat(allowRSS.getKeycloakRole()).contains("reserved");
    }
}
