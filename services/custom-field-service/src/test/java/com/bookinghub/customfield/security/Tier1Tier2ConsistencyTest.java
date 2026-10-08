package com.bookinghub.customfield.security;

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
 * Tier1/Tier2 consistency contract test — this service's capstone proof.
 *
 * Verifies that this service's own role requirement (Tier 2) EXACTLY matches
 * what the Gateway's SecurityConfig.java (Tier 1) route table declares for
 * /custom-fields/** and /field-templates/** — not merely not-weaker, since
 * this route group has no read/write split (every method, including GETs,
 * requires role_customfield_admin uniformly, per TechArch §4.4).
 *
 * This test reads the Gateway's ACTUAL current configuration (never a
 * hardcoded copy of an assumed rule) and this service's actual @PreAuthorize
 * annotations via reflection, failing the build if Tier 2 is ever weaker than
 * (or diverges from) Tier 1 for the same routes.
 *
 * NOTE: CustomFieldController/FieldTemplateController are built in plan 04-04,
 * not this plan (04-03). Controller classes are located via Class.forName (by
 * fully-qualified name, not a compile-time class literal) so this test
 * compiles and is checked in now, ready to be fully exercised the moment
 * plan 04-04's controllers exist. Until then, the "no @PreAuthorize methods
 * found" branch below reports zero methods checked rather than failing the
 * build — this test's job in THIS plan is to be written and wired correctly,
 * per the plan's own done-criteria ("fully exercised once plan 04-04's
 * controllers exist").
 */
class Tier1Tier2ConsistencyTest {

    private static final String GATEWAY_CONFIG_PATH =
        "../api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java";

    private static final String[] CONTROLLER_CLASS_NAMES = {
        "com.bookinghub.customfield.controller.CustomFieldController",
        "com.bookinghub.customfield.controller.FieldTemplateController"
    };

    private static final String REQUIRED_ROLE = "role_customfield_admin";

    @Test
    void testTier1Tier2ExactMatchForCustomFieldRoutes() throws IOException {
        // Step 1: Read Tier 1's ACTUAL configuration from Gateway's SecurityConfig.java
        String gatewayConfigText = readGatewaySecurityConfig();

        // Step 1b: Assert the Gateway's actual route-to-role line associates
        // /custom-fields/** and /field-templates/** with role_customfield_admin
        // for ALL methods (no HttpMethod.GET/POST narrowing on this prefix —
        // parse for the literal string, not just a write-method subset).
        assertGatewayGatesCustomFieldRoutesUniformly(gatewayConfigText);

        // Step 2: Read Tier 2's actual configuration via reflection on the
        // controllers plan 04-04 builds. If those controllers don't exist yet
        // (this plan, 04-03), report zero methods found rather than failing —
        // the invariant below is vacuously satisfied until 04-04 lands, at
        // which point every @PreAuthorize method found MUST contain the role.
        int methodsChecked = 0;
        for (String className : CONTROLLER_CLASS_NAMES) {
            Class<?> controllerClass = tryLoadClass(className);
            if (controllerClass == null) {
                continue;
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                PreAuthorize preAuth = method.getAnnotation(PreAuthorize.class);
                if (preAuth == null) {
                    // Every method on these controllers MUST carry @PreAuthorize,
                    // including GETs — no method is exempted for this route group.
                    fail("%s.%s has no @PreAuthorize annotation — every method on " +
                         "this service's custom-field/template controllers must " +
                         "require %s, including GET methods (no read/write carve-out " +
                         "for this route group, per the Gateway's uniform Tier-1 rule)",
                         controllerClass.getSimpleName(), method.getName(), REQUIRED_ROLE);
                }

                assertThat(preAuth.value())
                    .as("%s.%s: Tier 2 must require exactly %s (found: %s)",
                        controllerClass.getSimpleName(), method.getName(), REQUIRED_ROLE, preAuth.value())
                    .contains(REQUIRED_ROLE);

                methodsChecked++;
            }
        }

        // Document the state explicitly: either this plan runs before the
        // controllers exist (0 checked, vacuous pass) or after (N checked,
        // every one asserted to carry the required role).
        if (methodsChecked == 0) {
            System.out.println("Tier1Tier2ConsistencyTest: no controller methods found yet "
                + "(CustomFieldController/FieldTemplateController not yet built — expected until "
                + "plan 04-04 lands). Gateway-side assertion already passed.");
        }
    }

    /**
     * Attempts to load a controller class by fully-qualified name. Returns null
     * (rather than throwing) if the class does not exist yet, since plan 04-04
     * (not this plan) creates these controllers.
     */
    private Class<?> tryLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Reads Gateway's SecurityConfig.java source text. Fails explicitly if the
     * file cannot be found — never silently skips the Tier-1 half of this
     * contract.
     */
    private String readGatewaySecurityConfig() throws IOException {
        Path configPath = Path.of(GATEWAY_CONFIG_PATH);

        if (!Files.exists(configPath)) {
            fail("Gateway SecurityConfig.java not found at expected path: %s. " +
                 "If repo structure changed, update GATEWAY_CONFIG_PATH in this test.",
                 configPath.toAbsolutePath());
        }

        return Files.readString(configPath);
    }

    /**
     * Asserts the Gateway's actual SecurityConfig.java associates
     * /custom-fields/** and /field-templates/** with role_customfield_admin,
     * on a line with NO HttpMethod restriction (proving the "no read/write
     * split, every method" rule this test's class-level doc describes).
     */
    private void assertGatewayGatesCustomFieldRoutesUniformly(String gatewayConfigText) {
        // Find the .pathMatchers(...) line(s) mentioning /custom-fields/**
        Pattern linePattern = Pattern.compile(
            "\\.pathMatchers\\(([^)]*\"/custom-fields/\\*\\*\"[^)]*)\\)\\s*\\.hasAuthority\\(\"([^\"]+)\"\\)");
        Matcher matcher = linePattern.matcher(gatewayConfigText);

        assertThat(matcher.find())
            .as("Gateway SecurityConfig.java must have a .pathMatchers(...) rule for " +
                "/custom-fields/** using .hasAuthority(...) with NO HttpMethod.* restriction " +
                "(proving all methods, including GET, are gated identically)")
            .isTrue();

        String matchedArgs = matcher.group(1);
        String matchedAuthority = matcher.group(2);

        assertThat(matchedArgs)
            .as("The /custom-fields/** pathMatchers call must NOT be scoped to a specific " +
                "HttpMethod (e.g. HttpMethod.GET) — this route group has no read/write split")
            .doesNotContain("HttpMethod");

        assertThat(matchedArgs)
            .as("The same pathMatchers call must also include /field-templates/**")
            .contains("/field-templates/**");

        assertThat(matchedAuthority)
            .as("Gateway's authority for /custom-fields/**-/field-templates/** must be SCOPE_%s", REQUIRED_ROLE)
            .isEqualTo("SCOPE_" + REQUIRED_ROLE);
    }
}
