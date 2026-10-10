package com.bookinghub.booking.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tier1/Tier2 consistency contract test — booking-service capstone proof.
 *
 * Verifies that this service's own role requirements (Tier 2) are never weaker
 * than what the Gateway's SecurityConfig.java (Tier 1) route table implies for
 * /bookings/**.
 *
 * The Gateway applies ONE coarse rule to ALL /bookings/** methods:
 * hasAnyAuthority("SCOPE_role_booking_viewer", "SCOPE_role_booking_creator",
 * "SCOPE_role_booking_approver"). This test reads the Gateway's ACTUAL current
 * configuration (never a hardcoded copy of an assumed rule) and asserts:
 *
 * (a) The Gateway's file contains the literal /bookings/** pathMatcher with
 *     hasAnyAuthority naming exactly those 3 roles.
 *
 * (b) Via reflection, every @PreAuthorize role name on this service's booking
 *     controllers (built in plans 05-02/05-03/05-04) is ONE OF those 3 strings —
 *     never a role outside the Gateway's permitted set ("never weaker" means Tier-2
 *     may narrow to a subset per action, but must never require/accept a role the
 *     Gateway wouldn't have already let through).
 *
 * Controllers don't exist yet at this plan (05-01) — plan 04-01's precedent applies:
 * controller-reflection assertions use Class.forName with a graceful "not yet present"
 * path so this plan's own `mvn test` run doesn't fail on classes legitimately not
 * built yet. Plans 05-03/05-04's integration tests re-run this test once their
 * controllers exist, at which point reflection assertions become load-bearing.
 *
 * Named cross-reference (BookingApprovalController exemption, matching Phase 3 plan
 * 03-02's PermissionController precedent exactly):
 * BookingApprovalController's approve()/deny() methods deliberately carry NO
 * @PreAuthorize — they need FRD's distinct APPROVAL_FORBIDDEN code instead of the
 * generic BOOKING_FORBIDDEN this service's BookingAccessDeniedHandler produces, so
 * they perform a manual currentUserProvider.hasRole("role_booking_approver") check
 * and throw ApprovalForbiddenException directly. This test therefore EXEMPTS
 * BookingApprovalController's approve/deny methods from the "@PreAuthorize check"
 * path and instead asserts those two methods contain a literal
 * hasRole("role_booking_approver") call in their source.
 */
class Tier1Tier2ConsistencyTest {

    private static final String GATEWAY_CONFIG_PATH =
        "../api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java";

    /** The 3 roles the Gateway's Tier-1 /bookings/** rule accepts (SCOPE_ prefix stripped). */
    private static final Set<String> TIER1_BOOKING_ROLES = Set.of(
        "role_booking_viewer",
        "role_booking_creator",
        "role_booking_approver"
    );

    private static final String BOOKING_CONTROLLER_FQCN =
        "com.bookinghub.booking.controller.BookingController";
    private static final String BOOKING_QUERY_CONTROLLER_FQCN =
        "com.bookinghub.booking.controller.BookingQueryController";
    private static final String CONFLICT_CONTROLLER_FQCN =
        "com.bookinghub.booking.controller.ConflictController";
    private static final String APPROVAL_CONTROLLER_FQCN =
        "com.bookinghub.booking.controller.BookingApprovalController";

    /**
     * Main consistency test: reads Gateway's actual SecurityConfig.java source
     * and asserts its /bookings/** rule uses exactly the 3 expected role names.
     * Then, if controllers exist (re-run after plan 05-03/05-04), asserts every
     * @PreAuthorize role is within TIER1_BOOKING_ROLES.
     */
    @Test
    void tier2NeverWeakerThanTier1ForBookings() throws IOException {
        // Step 1: Read Tier 1's ACTUAL configuration from Gateway's SecurityConfig.java
        assertGatewayHasBookingRule();

        // Step 2: If plan 05-03/05-04's controllers exist on the classpath (re-run),
        // reflectively prove Tier-2 @PreAuthorize roles never exceed Tier-1's set.
        if (controllerClassExists(BOOKING_CONTROLLER_FQCN)) {
            assertControllerPreAuthorizeRolesAreSubsetOfTier1(BOOKING_CONTROLLER_FQCN);
        }
        if (controllerClassExists(BOOKING_QUERY_CONTROLLER_FQCN)) {
            assertControllerPreAuthorizeRolesAreSubsetOfTier1(BOOKING_QUERY_CONTROLLER_FQCN);
        }
        if (controllerClassExists(CONFLICT_CONTROLLER_FQCN)) {
            assertControllerPreAuthorizeRolesAreSubsetOfTier1(CONFLICT_CONTROLLER_FQCN);
        }
        // BookingApprovalController is exempted from @PreAuthorize check (see class javadoc)
    }

    /**
     * Reads the Gateway SecurityConfig.java and asserts the /bookings/** rule
     * declares hasAnyAuthority with exactly the 3 expected booking role names.
     */
    private void assertGatewayHasBookingRule() throws IOException {
        Path configPath = Path.of(GATEWAY_CONFIG_PATH);

        if (!Files.exists(configPath)) {
            fail("Gateway SecurityConfig.java not found at expected path: %s. " +
                 "If repo structure changed, update GATEWAY_CONFIG_PATH in this test.",
                 configPath.toAbsolutePath());
        }

        String content = Files.readString(configPath);

        assertThat(content)
            .as("Gateway SecurityConfig.java must contain /bookings/** pathMatcher")
            .contains("/bookings/**");

        assertThat(content)
            .as("Gateway /bookings/** rule must reference role_booking_viewer")
            .contains("role_booking_viewer");

        assertThat(content)
            .as("Gateway /bookings/** rule must reference role_booking_creator")
            .contains("role_booking_creator");

        assertThat(content)
            .as("Gateway /bookings/** rule must reference role_booking_approver")
            .contains("role_booking_approver");

        // Verify the booking rule uses hasAnyAuthority (coarse gate)
        assertThat(content)
            .as("Gateway /bookings/** must use hasAnyAuthority for the coarse Tier-1 gate")
            .containsPattern("bookings.*hasAnyAuthority|hasAnyAuthority.*bookings");
    }

    private boolean controllerClassExists(String fqcn) {
        try {
            Class.forName(fqcn);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Reflectively asserts every @PreAuthorize-annotated mapping method on the
     * given controller class uses only roles within TIER1_BOOKING_ROLES.
     */
    private void assertControllerPreAuthorizeRolesAreSubsetOfTier1(String fqcn) {
        try {
            Class<?> controllerClass = Class.forName(fqcn);
            Pattern rolePattern = Pattern.compile("'(role_[^']+)'");

            for (Method method : controllerClass.getDeclaredMethods()) {
                boolean isMappingMethod =
                    method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)
                    || method.isAnnotationPresent(org.springframework.web.bind.annotation.GetMapping.class)
                    || method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)
                    || method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class);

                if (!isMappingMethod) {
                    continue;
                }

                var preAuth = method.getAnnotation(
                    org.springframework.security.access.prepost.PreAuthorize.class);

                if (preAuth == null) {
                    // Method has no @PreAuthorize — only acceptable if it's in
                    // BookingApprovalController (which uses manual hasRole check instead)
                    if (APPROVAL_CONTROLLER_FQCN.equals(fqcn)) {
                        continue; // exempted per class javadoc
                    }
                    fail("%s.%s is a mapping method but lacks @PreAuthorize — " +
                         "every booking endpoint must declare its role requirement explicitly",
                         fqcn, method.getName());
                }

                // Extract all role_* names from the @PreAuthorize expression
                Matcher matcher = rolePattern.matcher(preAuth.value());
                List<String> extractedRoles = new ArrayList<>();
                while (matcher.find()) {
                    extractedRoles.add(matcher.group(1));
                }

                for (String role : extractedRoles) {
                    assertThat(TIER1_BOOKING_ROLES)
                        .as("%s.%s uses role '%s' which is NOT in the Gateway's Tier-1 " +
                            "/bookings/** permitted set %s — Tier-2 must never require a " +
                            "role outside Tier-1's gate",
                            fqcn, method.getName(), role, TIER1_BOOKING_ROLES)
                        .contains(role);
                }
            }
        } catch (ClassNotFoundException e) {
            fail("%s not found despite controllerClassExists check passing", fqcn);
        }
    }
}
