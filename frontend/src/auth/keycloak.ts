import Keycloak from "keycloak-js";

// Keycloak is served from the app's own origin: nginx forwards /realms/, /resources/ and /js/
// to the keycloak container (frontend/nginx.conf). The browser never needs a second address,
// which matters behind Pivota's preview — each preview URL is signed for ONE port, so a
// Keycloak URL on another port (8180-...) is refused ("preview auth: invalid token").
// `npm run dev` has no nginx in front, so it talks to Keycloak directly.
function getKeycloakUrl(): string {
  if (import.meta.env.VITE_KEYCLOAK_URL) return import.meta.env.VITE_KEYCLOAK_URL;
  return import.meta.env.DEV ? "http://localhost:8180" : window.location.origin;
}

export const keycloak = new Keycloak({
  url: getKeycloakUrl(),
  realm: "bookinghub",
  clientId: "bookinghub-frontend",
});
