import Keycloak from "keycloak-js";

// Compute Keycloak URL at runtime to support dynamic preview URLs
function getKeycloakUrl(): string {
  const hostname = window.location.hostname;
  
  // If hostname starts with 3000-, replace with 8180- to reach Keycloak on same preview domain
  if (hostname.startsWith('3000-')) {
    const keycloakHostname = hostname.replace(/^3000-/, '8180-');
    return `${window.location.protocol}//${keycloakHostname}`;
  }
  
  // Otherwise use env var or localhost fallback
  return import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8180";
}

export const keycloak = new Keycloak({
  url: getKeycloakUrl(),
  realm: "bookinghub",
  clientId: "bookinghub-frontend",
});
