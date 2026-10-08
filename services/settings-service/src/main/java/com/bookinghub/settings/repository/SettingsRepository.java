package com.bookinghub.settings.repository;

import com.bookinghub.settings.domain.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the singleton Settings entity.
 *
 * No custom query methods needed beyond the inherited findById(1) — the
 * singleton invariant means there is exactly one row to ever read or write.
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Integer> {
}
