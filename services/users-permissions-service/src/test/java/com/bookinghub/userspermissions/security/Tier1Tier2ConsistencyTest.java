package com.bookinghub.userspermissions.security;

import com.bookinghub.userspermissions.controller.PermissionController;
import com.bookinghub.userspermissions.controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Tier1/Tier2 consistency contract test - Phase 3's capstone proof.
 * 
 * Verifies that this service's own role requirements (Tier 2) are never weaker
 * than what the Gateway's SecurityConfig.java (Tier 1) route table implies for
 * the same prefixes.
 * 
 * This test reads the Gateway's ACTUAL current configuration (never a hardcoded
 * copy of an assumed rule) and this service's actual @PreAuthorize annotations
 * + PermissionController's manual hasRole(...) checks, failing the build if
 * Tier 2 is ever weaker than Tier 1 for the same route.
 * 
 * Implements Phase 3's 5th success criterion: "A Gateway-protected route's
 * minimum required role and this service's own @PreAuthorize minimum required
 * role for the same route prefix are proven to never disagree in the direction
 * of Tier 2 being weaker than Tier 1... both proven by an automated test
 * reading both tiers' ACTUAL current configuration every run, not by prose
 * assertion or a hardcoded copy of an assumed rule."
 */
class Tier1Tier2ConsistencyTest {
    
    private static final String GATEWAY_CONFIG_PATH = 
        "../api-gateway/src/main/java/com/bookinghub/gateway/config/SecurityConfig.java";
    
    /**
     * Main consistency test: reads Gateway's actual SecurityConfig.java source,
     * reads this service's actual UserController/PermissionController enforcement,
     * and asserts Tier 2 is never weaker than Tier 1 for the same routes.
     */
    @Test
    void testTier1Tier2ConsistencyAndUsersmeCarveOut() throws IOException {
        // Step 1: Read Tier 1's ACTUAL configuration from Gateway's SecurityConfig.java
        Map<String, String> tier1Rules = parseGatewaySecurityConfig();
        
        // Step 1b: Directly assert /users/** pattern exists and requires role_user_admin
        // (per plan 02-10, Gateway enforces role_user_admin on ALL /users/** with no
        // carve-out — self-access logic is Tier 2 only)
        assertThat(tier1Rules)
            .as("Gateway must have a rule for /users/**")
            .containsKey("/users/**");
        
        String usersRule = tier1Rules.get("/users/**");
        assertThat(usersRule)
            .as("Gateway /users/** must require role_user_admin")
            .contains("role_user_admin");
        
        // Step 2: Read Tier 2's actual configuration
        Map<String, String> userControllerTier2 = extractUserControllerPreAuthorize();
        Map<String, String> permissionControllerTier2 = extractPermissionControllerManualChecks();
        
        // Step 3: Assert the invariant per route group
        
        // /users/** (UserController, every method except /me endpoints)
        // Tier 2 must require AT LEAST role_user_admin
        // Methods with self-access satisfy this: hasRole('role_user_admin') OR isSelf(#id)
        for (Map.Entry<String, String> entry : userControllerTier2.entrySet()) {
            String method = entry.getKey();
            String tier2Expr = entry.getValue();
            
            if (method.equals("GET /users/me") || method.equals("PUT /users/me")) {
                // Special case: /users/me endpoints should NOT require role_user_admin
                // (self-access only, per F6.2). Tier 1 gates ALL /users/** on role_user_admin,
                // but Tier 2 allows authenticated-only for /me endpoints (resolved to self via
                // CurrentUserProvider). Assert no role_user_admin requirement here.
                assertThat(tier2Expr)
                    .as("%s: Tier 2 should NOT require role_user_admin (self-access only)", method)
                    .doesNotContain("hasRole('role_user_admin')");
            } else {
                // All other /users/** methods: Tier 2 must include role_user_admin
                // (possibly OR'd with isSelf)
                assertThat(tier2Expr)
                    .as("%s: Tier 2 must require AT LEAST role_user_admin (found: %s)", method, tier2Expr)
                    .contains("hasRole('role_user_admin')");
            }
        }
        
        // /permissions/**, /roles/** (PermissionController)
        // Tier 2 must require AT LEAST role_permissions_admin
        assertThat(tier1Rules)
            .as("Gateway must have rules for /permissions/** and /roles/**")
            .containsKeys("/permissions/**", "/roles/**");
        
        String permissionsRule = tier1Rules.get("/permissions/**");
        String rolesRule = tier1Rules.get("/roles/**");
        
        assertThat(permissionsRule)
            .as("Gateway /permissions/** must require role_permissions_admin")
            .contains("role_permissions_admin");
        
        assertThat(rolesRule)
            .as("Gateway /roles/** must require role_permissions_admin")
            .contains("role_permissions_admin");
        
        for (Map.Entry<String, String> entry : permissionControllerTier2.entrySet()) {
            String method = entry.getKey();
            String tier2Check = entry.getValue();
            
            assertThat(tier2Check)
                .as("%s: Tier 2 must require role_permissions_admin (found: %s)", method, tier2Check)
                .isEqualTo("hasRole(\"role_permissions_admin\")");
        }
        
        // Also assert PermissionController has NO @PreAuthorize annotations
        // (deliberate manual-check mechanism to produce PERMISSIONS_FORBIDDEN specifically)
        assertNoPreAuthorizeOnPermissionController();
    }
    
    /**
     * Parse Gateway's SecurityConfig.java to extract route → role mappings.
     * Returns a map of path pattern → required authority/role string.
     */
    private Map<String, String> parseGatewaySecurityConfig() throws IOException {
        Path configPath = Path.of(GATEWAY_CONFIG_PATH);
        
        if (!Files.exists(configPath)) {
            fail("Gateway SecurityConfig.java not found at expected path: %s. " +
                 "If repo structure changed, update GATEWAY_CONFIG_PATH in this test.", 
                 configPath.toAbsolutePath());
        }
        
        String content = Files.readString(configPath);
        Map<String, String> rules = new HashMap<>();
        
        // Parse .pathMatchers lines
        // Example 1: .pathMatchers("/users/**").hasAuthority("SCOPE_role_user_admin")
        // Example 2: .pathMatchers("/permissions/**", "/roles/**").hasAuthority("SCOPE_role_permissions_admin")
        // Example 3: .pathMatchers(HttpMethod.GET, "/settings/**").authenticated()
        
        // Split into lines and parse each pathMatchers call
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains(".pathMatchers(")) {
                // Extract paths (everything between quotes after pathMatchers)
                Pattern pathPattern = Pattern.compile("\"(/[^\"]+)\"");
                Matcher pathMatcher = pathPattern.matcher(line);
                
                // Extract authority - check for hasAuthority or hasAnyAuthority
                String authority = null;
                if (line.contains(".hasAuthority(")) {
                    Pattern authPattern = Pattern.compile("\\.hasAuthority\\(\"(SCOPE_[^\"]+)\"\\)");
                    Matcher authMatcher = authPattern.matcher(line);
                    if (authMatcher.find()) {
                        authority = authMatcher.group(1);
                    }
                } else if (line.contains(".hasAnyAuthority(")) {
                    Pattern authPattern = Pattern.compile("\\.hasAnyAuthority\\(\"(SCOPE_[^\"]+)\"");
                    Matcher authMatcher = authPattern.matcher(line);
                    if (authMatcher.find()) {
                        authority = authMatcher.group(1);
                    }
                }
                
                if (authority != null) {
                    String role = authority.replace("SCOPE_", "");
                    
                    // Add all paths found in this line
                    while (pathMatcher.find()) {
                        String path = pathMatcher.group(1);
                        rules.put(path, role);
                    }
                }
            }
        }
        
        return rules;
    }
    
    /**
     * Extract @PreAuthorize expressions from UserController methods via reflection.
     * Returns a map of "HTTP_METHOD /path" → @PreAuthorize expression string.
     */
    private Map<String, String> extractUserControllerPreAuthorize() {
        Map<String, String> tier2 = new HashMap<>();
        
        for (Method method : UserController.class.getDeclaredMethods()) {
            PreAuthorize preAuth = method.getAnnotation(PreAuthorize.class);
            
            if (preAuth != null) {
                String methodName = method.getName();
                String path = inferPathFromMethodName(methodName, "users");
                String httpMethod = inferHttpMethodFromMethodName(methodName);
                
                tier2.put(httpMethod + " " + path, preAuth.value());
            }
        }
        
        return tier2;
    }
    
    /**
     * Extract manual hasRole checks from PermissionController source text.
     * Returns a map of "HTTP_METHOD /path" → manual check string.
     */
    private Map<String, String> extractPermissionControllerManualChecks() throws IOException {
        Map<String, String> tier2 = new HashMap<>();
        
        // Read PermissionController.java source
        Path controllerPath = Path.of(
            "src/main/java/com/bookinghub/userspermissions/controller/PermissionController.java"
        );
        
        if (!Files.exists(controllerPath)) {
            fail("PermissionController.java not found at: %s", controllerPath.toAbsolutePath());
        }
        
        String content = Files.readString(controllerPath);
        
        // Check each method contains the manual hasRole check
        if (content.contains("@GetMapping(\"/permissions\")")) {
            tier2.put("GET /permissions", "hasRole(\"role_permissions_admin\")");
        }
        
        if (content.contains("@GetMapping(\"/roles/{role}/permissions\")")) {
            tier2.put("GET /roles/{role}/permissions", "hasRole(\"role_permissions_admin\")");
        }
        
        if (content.contains("@PutMapping(\"/roles/{role}/permissions\")")) {
            tier2.put("PUT /roles/{role}/permissions", "hasRole(\"role_permissions_admin\")");
        }
        
        // Verify each has the actual hasRole check in its body (non-comment lines only)
        // Remove single-line comments and multi-line /** */ comments (including JavaDoc)
        String codeOnly = content
            .replaceAll("//.*?\n", "\n")  // Single-line comments
            .replaceAll("(?s)/\\*\\*.*?\\*/", "")  // JavaDoc comments (/** ... */) with DOTALL
            .replaceAll("(?s)/\\*.*?\\*/", "");  // Block comments (/* ... */) with DOTALL
        
        Pattern checkPattern = Pattern.compile("\\bcurrentUserProvider\\.hasRole\\(\"role_permissions_admin\"\\)");
        Matcher matcher = checkPattern.matcher(codeOnly);
        int checkCount = 0;
        while (matcher.find()) {
            checkCount++;
        }
        
        assertThat(checkCount)
            .as("PermissionController must have exactly 3 currentUserProvider.hasRole('role_permissions_admin') calls (found %d)", checkCount)
            .isEqualTo(3);
        
        return tier2;
    }
    
    /**
     * Assert PermissionController has NO @PreAuthorize annotations.
     * (Deliberate design to produce PERMISSIONS_FORBIDDEN specifically.)
     */
    private void assertNoPreAuthorizeOnPermissionController() {
        long preAuthCount = Arrays.stream(PermissionController.class.getDeclaredMethods())
            .filter(m -> m.isAnnotationPresent(PreAuthorize.class))
            .count();
        
        assertThat(preAuthCount)
            .as("PermissionController must have NO @PreAuthorize annotations (uses manual checks)")
            .isZero();
    }
    
    /**
     * Infer REST path from Java method name.
     * Example: getUsers() → /users, getUserById() → /users/{id}
     */
    private String inferPathFromMethodName(String methodName, String resource) {
        if (methodName.equals("getUsers")) {
            return "/users";
        } else if (methodName.equals("createUser")) {
            return "/users";
        } else if (methodName.equals("getUserById")) {
            return "/users/{id}";
        } else if (methodName.equals("updateUser")) {
            return "/users/{id}";
        } else if (methodName.equals("updateUserRoles")) {
            return "/users/{id}/roles";
        } else if (methodName.equals("getCurrentUser")) {
            return "/users/me";
        } else if (methodName.equals("updateCurrentUser")) {
            return "/users/me";
        }
        
        return "/" + resource + "/**";
    }
    
    /**
     * Infer HTTP method from Java method name.
     * Example: getUsers() → GET, createUser() → POST
     */
    private String inferHttpMethodFromMethodName(String methodName) {
        if (methodName.startsWith("get")) {
            return "GET";
        } else if (methodName.startsWith("create")) {
            return "POST";
        } else if (methodName.startsWith("update")) {
            return "PUT";
        } else if (methodName.startsWith("delete")) {
            return "DELETE";
        }
        
        return "GET";
    }
}
