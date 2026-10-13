proxy_pass http://keycloak:8080;
proxy_set_header Host $public_host;
proxy_set_header X-Forwarded-Host $public_host;
proxy_set_header X-Forwarded-Proto $public_proto;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
# Keycloak's auth cookies and redirect headers exceed nginx's 4k default.
proxy_buffer_size 16k;
proxy_buffers 8 16k;
