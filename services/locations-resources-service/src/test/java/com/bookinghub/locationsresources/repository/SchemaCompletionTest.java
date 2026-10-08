package com.bookinghub.locationsresources.repository;

import com.bookinghub.locationsresources.domain.Location;
import com.bookinghub.locationsresources.domain.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the V2 migration (F0 schema completion) applies cleanly on top of V1
 * and that every new column round-trips correctly through its JPA entity field.
 *
 * Covers both Location (colour, description, layout as JSON array) and Resource
 * (type, description, isUnique, restrictLocations) — the full F0-confirmed field
 * sets this plan's migration adds.
 *
 * Uses the running Postgres from docker-compose instead of Testcontainers:
 * this sandbox's Docker daemon reports API version 1.32 while Testcontainers
 * 1.20.4 requires >=1.40 (identical pre-existing incompatibility already hit —
 * and worked around the same way — by Phase 3 plan 03-01's PermissionSeedDataTest
 * / OutboxAndResetTokenRepositoryTest for users-permissions-service). @DataJpaTest
 * wraps each test in a rolled-back transaction, so no cleanup between tests is
 * needed despite sharing the persistent locres_db_test database.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SchemaCompletionTest {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Test
    void locationRoundTripsColourDescriptionAndLayout() {
        Location location = new Location(
                "Main Boardroom",
                "room-blue",
                "#FF0000",
                "test",
                "HQ Building A",
                List.of("boardroom", "lecture")
        );

        Location saved = locationRepository.save(location);
        locationRepository.flush();

        Location found = locationRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Main Boardroom");
        assertThat(found.getCssClass()).isEqualTo("room-blue");
        assertThat(found.getColour()).isEqualTo("#FF0000");
        assertThat(found.getDescription()).isEqualTo("test");
        assertThat(found.getBuilding()).isEqualTo("HQ Building A");
        assertThat(found.getLayout()).containsExactly("boardroom", "lecture");
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getDeletedAt()).isNull();
    }

    @Test
    void resourceRoundTripsTypeDescriptionIsUniqueAndRestrictLocations() {
        UUID someLocationId = UUID.randomUUID();

        Resource resource = new Resource(
                "Projector",
                "Audio Visual",
                "HDMI/VGA capable projector",
                true,
                List.of(someLocationId)
        );

        Resource saved = resourceRepository.save(resource);
        resourceRepository.flush();

        Resource found = resourceRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Projector");
        assertThat(found.getType()).isEqualTo("Audio Visual");
        assertThat(found.getDescription()).isEqualTo("HDMI/VGA capable projector");
        assertThat(found.isUnique()).isTrue();
        assertThat(found.getRestrictLocations()).containsExactly(someLocationId);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
        assertThat(found.getDeletedAt()).isNull();
    }
}
