package com.bookinghub.gateway.error;

import com.bookinghub.gateway.config.SecurityConfig;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global error attributes customizer to shape all error responses
 * as the shared ApiError format (error_code, message, timestamp, path).
 * Applies to 401/403/429/503/500 and every other error response.
 */
@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Throwable error = getError(request);
        Map<String, Object> errorAttributes = new HashMap<>();
        
        // Determine error_code and message based on the exception type
        if (error instanceof SecurityConfig.JwksUnreachableException) {
            // Fail-closed JWKS unreachability → 503
            errorAttributes.put("error_code", "SERVICE_UNAVAILABLE");
            errorAttributes.put("message", "Identity provider unavailable - authentication service temporarily unreachable");
            errorAttributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        } else {
            // Extract status from default error attributes
            Map<String, Object> defaultAttrs = super.getErrorAttributes(request, options);
            int status = (Integer) defaultAttrs.getOrDefault("status", 500);
            
            errorAttributes.put("status", status);
            errorAttributes.put("error_code", mapStatusToErrorCode(status));
            errorAttributes.put("message", determineMessage(error, status, defaultAttrs));
        }
        
        errorAttributes.put("timestamp", Instant.now().toString());
        errorAttributes.put("path", request.path());
        
        return errorAttributes;
    }

    private String mapStatusToErrorCode(int status) {
        return switch (status) {
            case 400 -> "REQUEST_MALFORMED";
            case 401 -> "AUTH_UNAUTHENTICATED";
            case 403 -> "GATEWAY_FORBIDDEN";
            case 404 -> "ROUTE_NOT_FOUND";
            case 429 -> "RATE_LIMIT_EXCEEDED";
            case 503 -> "SERVICE_UNAVAILABLE";
            case 500 -> "INTERNAL_ERROR";
            default -> "ERROR_" + status;
        };
    }

    private String determineMessage(Throwable error, int status, Map<String, Object> defaultAttrs) {
        if (error != null && error.getMessage() != null && !error.getMessage().isEmpty()) {
            return error.getMessage();
        }
        
        String defaultMessage = (String) defaultAttrs.get("message");
        if (defaultMessage != null && !defaultMessage.isEmpty()) {
            return defaultMessage;
        }
        
        return switch (status) {
            case 400 -> "Request is malformed or contains invalid parameters";
            case 401 -> "Authentication required";
            case 403 -> "Access denied - insufficient permissions";
            case 404 -> "Route not found";
            case 429 -> "Too many requests - rate limit exceeded";
            case 503 -> "Service temporarily unavailable";
            case 500 -> "Internal server error";
            default -> "An error occurred";
        };
    }
}
