# Resolve "api-gateway" per request (Docker's embedded DNS), not once at startup,
# for the same reason as keycloak-proxy.inc: a compose boot can start the
# frontend before the gateway, and a fixed upstream would keep nginx from starting.
resolver 127.0.0.11 valid=10s ipv6=off;
set $gateway_upstream http://api-gateway:8080;
# /api/bookings?x=1 -> /bookings?x=1 (the gateway's routes live at the root).
rewrite ^/api/(.*)$ /$1 break;
proxy_pass $gateway_upstream;
proxy_set_header Host $public_host;
proxy_set_header X-Forwarded-Host $public_host;
proxy_set_header X-Forwarded-Proto $public_proto;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
# Bookings queries over a wide date range can take a while on a cold stack.
proxy_read_timeout 60s;
