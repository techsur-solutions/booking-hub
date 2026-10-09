package com.bookinghub.locationsresources.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tier1/Tier2 consistency contract test - this plan's capstone proof.
 *
 * Verifies that this service's own role requirements (Tier 2) are never weaker
 * than what the Gateway's SecurityConfig.java (Tier 1) route table implies for
 * /locations/** and /resources/**.
 *
 * This test reads the Gateway's ACTUAL current configuration (never a hardcoded
 * copy of an assumed rule) and this service's actual @PreAuthorize annotations
 * via reflection, failing the build if Tier 2 is ever weaker than Tier 1 for the
 * same route.
 *
 * NOTE: plan 04-01 establishes this test file and the Gateway-source-reading
 * mechanism. The reflective @PreAuthorize check against LocationController/
 * ResourceController is written against classes that don't exist until plan
 * 04-02 lands its controllers — this test's controller-reflection assertions
 * use Class.forName with a graceful "not yet present" path so this plan's own
 * `mvn test` run doesn't fail on a class that is legitimately not built yet;
 * plan 04-02's Task 3 re-runs this same test once LocationController/
 * ResourceController exist, at which point the reflective assertions become
 * load-bearing.
 */
class Tier1Tier2ConsistencyTest {

    private static final String GATEWAY_CONFIG_PATH =
        "../api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java";

    private static final String LOCATION_CONTROLLER_FQCN =
        "com.bookinghub.locationsresources.controller.LocationController";

    private static final String RESOURCE_CONTROLLER_FQCN =
        "com.bookinghub.locationsresources.controller.ResourceController";

    /**
     * Main consistency test: reads Gateway's actual SecurityConfig.java source
     * and asserts its /locations/** and /resources/** rules require
     * "authenticated" for GET and "role_location_admin" for POST/PUT/DELETE.
     *
     * If plan 04-02's LocationController/ResourceController classes already
     * exist on the classpath (this test is being re-run as part of 04-02's
     * verification), also asserts every write-method @PreAuthorize contains
     * role_location_admin — never weaker than what Tier 1 requires for the
     * same route.
     */
    @Test
    void tier2NeverWeakerThanTier1ForLocationsAndResources() throws IOException {
        // Step 1: Read Tier 1's ACTUAL configuration from Gateway's SecurityConfig.java
        Map<String, String> tier1GetRules = new HashMap<>();
        Map<String, String> tier1WriteRules = new HashMap<>();
        parseGatewaySecurityConfig(tier1GetRules, tier1WriteRules);

        assertThat(tier1GetRules)
            .as("Gateway must have a GET rule for /locations/**, /resources/**")
            .containsKey("/locations/**,/resources/**");

        assertThat(tier1GetRules.get("/locations/**,/resources/**"))
            .as("Gateway GET /locations/**, /resources/** must require authenticated")
            .isEqualTo("authenticated");

        assertThat(tier1WriteRules)
            .as("Gateway must have POST/PUT/DELETE rules for /locations/**, /resources/**")
            .containsKeys("POST", "PUT", "DELETE");

        for (String method : new String[]{"POST", "PUT", "DELETE"}) {
            assertThat(tier1WriteRules.get(method))
                .as("Gateway %s /locations/**, /resources/** must require role_location_admin", method)
                .contains("role_location_admin");
        }

        // Step 2: If plan 04-02's controllers exist, reflectively prove Tier 2
        // write-method @PreAuthorize is never weaker than role_location_admin.
        if (controllerClassExists(LOCATION_CONTROLLER_FQCN)) {
            assertControllerWriteMethodsRequireRole(LOCATION_CONTROLLER_FQCN, "role_location_admin");
        }
        if (controllerClassExists(RESOURCE_CONTROLLER_FQCN)) {
            assertControllerWriteMethodsRequireRole(RESOURCE_CONTROLLER_FQCN, "role_location_admin");
        }
    }

    /**
     * Parse Gateway's SecurityConfig.java to extract GET vs write (POST/PUT/DELETE)
     * rules specifically for the /locations/**,/resources/** pathMatchers lines.
     */
    private void parseGatewaySecurityConfig(Map<String, String> tier1GetRules,
                                             Map<String, String> tier1WriteRules) throws IOException {
        Path configPath = Path.of(GATEWAY_CONFIG_PATH);

        if (!Files.exists(configPath)) {
            fail("Gateway SecurityConfig.java not found at expected path: %s. " +
                 "If repo structure changed, update GATEWAY_CONFIG_PATH in this test.",
                 configPath.toAbsolutePath());
        }

        String content = Files.readString(configPath);
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (!line.contains("/locations/**") && !line.contains("/resources/**")) {
                continue;
            }
            if (!line.contains(".pathMatchers(")) {
                continue;
            }

            boolean isGet = line.contains("HttpMethod.GET");
            boolean isPost = line.contains("HttpMethod.POST");
            boolean isPut = line.contains("HttpMethod.PUT");
            boolean isDelete = line.contains("HttpMethod.DELETE");

            if (isGet && line.contains(".authenticated()")) {
                tier1GetRules.put("/locations/**,/resources/**", "authenticated");
            }

            if ((isPost || isPut || isDelete) && line.contains(".hasAuthority(")) {
                Pattern authPattern = Pattern.compile("\\.hasAuthority\\(\"(SCOPE_[^\"]+)\"\\)");
                Matcher authMatcher = authPattern.matcher(line);
                if (authMatcher.find()) {
                    String role = authMatcher.group(1).replace("SCOPE_", "");
                    if (isPost) {
                        tier1WriteRules.put("POST", role);
                    }
                    if (isPut) {
                        tier1WriteRules.put("PUT", role);
                    }
                    if (isDelete) {
                        tier1WriteRules.put("DELETE", role);
                    }
                }
            }
        }
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
     * Reflectively asserts every POST/PUT/DELETE-mapped method on the given
     * controller class carries an @PreAuthorize expression containing the
     * required role.
     */
    private void assertControllerWriteMethodsRequireRole(String fqcn, String requiredRole) throws IOException {
        try {
            Class<?> controllerClass = Class.forName(fqcn);
            org.springframework.security.access.prepost.PreAuthorize preAuth;

            for (var method : controllerClass.getDeclaredMethods()) {
                boolean isWriteMethod =
                    method.isAnnotationPresent(org.springframework.web.bind.annotation.PostMapping.class)
                    || method.isAnnotationPresent(org.springframework.web.bind.annotation.PutMapping.class)
                    || method.isAnnotationPresent(org.springframework.web.bind.annotation.DeleteMapping.class);

                if (!isWriteMethod) {
                    continue;
                }

                preAuth = method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
                assertThat(preAuth)
                    .as("%s.%s must carry @PreAuthorize", fqcn, method.getName())
                    .isNotNull();
                assertThat(preAuth.value())
                    .as("%s.%s must require AT LEAST %s (found: %s)", fqcn, method.getName(), requiredRole, preAuth.value())
                    .contains(requiredRole);
            }
        } catch (ClassNotFoundException e) {
            fail("%s not found despite controllerClassExists check passing", fqcn);
        }
    }
}
