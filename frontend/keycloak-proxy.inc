# Resolve "keycloak" per request (Docker's embedded DNS), not once at startup:
# with a fixed upstream, nginx refuses to start if keycloak's name is not
# resolvable yet, and a compose boot can start the frontend first.
resolver 127.0.0.11 valid=10s ipv6=off;
set $keycloak_upstream http://keycloak:8080;
proxy_pass $keycloak_upstream;
proxy_set_header Host $public_host;
proxy_set_header X-Forwarded-Host $public_host;
proxy_set_header X-Forwarded-Proto $public_proto;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
# Keycloak's auth cookies and redirect headers exceed nginx's 4k default.
proxy_buffer_size 16k;
proxy_buffers 8 16k;
