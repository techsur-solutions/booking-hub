package com.bookinghub.settings.security;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tier1/Tier2 consistency contract test for settings-service.
 *
 * Verifies that this service's own role requirements (Tier 2) for /settings
 * are never weaker than what the Gateway's SecurityConfig.java (Tier 1) route
 * table implies for the same path+HTTP-method combinations.
 *
 * This test reads the Gateway's ACTUAL current configuration (never a
 * hardcoded copy of an assumed rule) and this service's actual @PreAuthorize
 * annotations via reflection on SettingsController (plan 04-06), failing the
 * build if Tier 2 is ever weaker than Tier 1 for the same route.
 *
 * The Gateway-side assertions run unconditionally in this plan (04-05) since
 * the Gateway's SecurityConfig.java already exists (Phase 2 plan 02-10). The
 * SettingsController reflection assertions are skipped gracefully (via
 * Assumptions.assumeTrue) until plan 04-06 creates that class — at which
 * point this same test method exercises them in full, exactly as the plan's
 * done-criteria describes ("written and ready... fully exercised once plan
 * 04-06's controller exists").
 */
class Tier1Tier2ConsistencyTest {

    private static final String GATEWAY_CONFIG_PATH =
        "../api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java";

    private static final String SETTINGS_CONTROLLER_CLASS =
        "com.bookinghub.settings.controller.SettingsController";

    @Test
    void testTier1Tier2ConsistencyForSettingsReadWriteSplit() throws Exception {
        // Step 1: Read Tier 1's ACTUAL configuration from Gateway's SecurityConfig.java
        String gatewayContent = readGatewaySecurityConfig();

        // Step 1a: Assert GET /settings/** is "authenticated" (no specific role) at Tier 1
        Pattern getPattern = Pattern.compile(
            "\\.pathMatchers\\(HttpMethod\\.GET,\\s*\"/settings/\\*\\*\"\\)\\s*\\.authenticated\\(\\)"
        );
        assertThat(getPattern.matcher(gatewayContent).find())
            .as("Gateway must have GET /settings/** mapped to .authenticated() (no specific role)")
            .isTrue();

        // Step 1b: Assert PUT /settings/** requires role_settings_admin at Tier 1
        Pattern putPattern = Pattern.compile(
            "\\.pathMatchers\\(HttpMethod\\.PUT,\\s*\"/settings/\\*\\*\"\\)\\s*\\.hasAuthority\\(\"SCOPE_role_settings_admin\"\\)"
        );
        assertThat(putPattern.matcher(gatewayContent).find())
            .as("Gateway must have PUT /settings/** mapped to .hasAuthority(\"SCOPE_role_settings_admin\")")
            .isTrue();

        // Step 2: Read Tier 2's actual configuration via reflection on SettingsController.
        // SettingsController does not exist until plan 04-06 — skip gracefully until then,
        // rather than fail, per this plan's done-criteria ("fully exercised once plan
        // 04-06's controller exists").
        Class<?> controllerClass;
        try {
            controllerClass = Class.forName(SETTINGS_CONTROLLER_CLASS);
        } catch (ClassNotFoundException e) {
            Assumptions.assumeTrue(false,
                "SettingsController not yet created (plan 04-06) — Gateway-side assertions "
                + "above passed; Tier-2 reflection assertions will run once that class exists.");
            return;
        }

        Method getSettingsMethod = findMethodByName(controllerClass, "getSettings");
        Method updateSettingsMethod = findMethodByName(controllerClass, "updateSettings");

        assertThat(getSettingsMethod)
            .as("SettingsController must declare a getSettings() method")
            .isNotNull();
        assertThat(updateSettingsMethod)
            .as("SettingsController must declare an updateSettings() method")
            .isNotNull();

        // GET: no @PreAuthorize beyond the class-level authenticated() baseline
        PreAuthorize getPreAuth = getSettingsMethod.getAnnotation(PreAuthorize.class);
        assertThat(getPreAuth)
            .as("getSettings() should carry NO @PreAuthorize (authenticated-only, matching Tier 1's GET rule)")
            .isNull();

        // PUT: @PreAuthorize must require role_settings_admin — never weaker than Tier 1
        PreAuthorize putPreAuth = updateSettingsMethod.getAnnotation(PreAuthorize.class);
        assertThat(putPreAuth)
            .as("updateSettings() must carry @PreAuthorize")
            .isNotNull();
        assertThat(putPreAuth.value())
            .as("updateSettings()'s @PreAuthorize must require role_settings_admin (found: %s)", putPreAuth.value())
            .contains("role_settings_admin");
    }

    /**
     * Reads Gateway's SecurityConfig.java source text.
     * Fails explicitly (not silently) if the file cannot be found, since the
     * whole point of this test is reading the Gateway's ACTUAL current
     * configuration rather than a hardcoded assumption.
     */
    private String readGatewaySecurityConfig() throws IOException {
        Path configPath = Path.of(GATEWAY_CONFIG_PATH);

        if (!Files.exists(configPath)) {
            fail("Gateway SecurityConfig.java not found at expected path: %s. "
                 + "If repo structure changed, update GATEWAY_CONFIG_PATH in this test.",
                 configPath.toAbsolutePath());
        }

        return Files.readString(configPath);
    }

    private Method findMethodByName(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
