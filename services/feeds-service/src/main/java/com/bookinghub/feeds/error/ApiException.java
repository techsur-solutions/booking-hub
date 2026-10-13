package com.bookinghub.feeds.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for all API exceptions in feeds-service.
 * Each subclass hard-codes its FRD-specified HTTP status and error_code.
 *
 * GlobalExceptionHandler catches ApiException polymorphically and constructs
 * ApiError responses with the exception's httpStatus/errorCode/message.
 *
 * This phase defines exactly ONE subclass:
 * - FeedLocationFilterInvalidException (400, FEED_LOCATION_FILTER_INVALID)
 *   thrown by plan 07-03 when a present location_id query param is not a
 *   parseable UUID.
 *
 * Deliberately NOT defined (per plan 07-01 named decision):
 * - FEED_FORBIDDEN — superseded by F9.6's public decision (no auth gate).
 * - FEED_FORMAT_UNSUPPORTED — cannot occur; each format is its own distinct
 *   route, so an unsupported format is simply a 404 ROUTE_NOT_FOUND handled by
 *   Spring's default dispatcher.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    protected ApiException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 400 FEED_LOCATION_FILTER_INVALID — the location_id query param is present
     * but is not a parseable UUID. Thrown by plan 07-03's public feed endpoints
     * during parameter validation.
     *
     * Note: an UNKNOWN-but-well-formed location_id is NOT an error — it returns
     * an empty feed per Phase 7 Success Criterion 2, handled in plan 07-03's
     * query (not here). Only a MALFORMED (unparseable) UUID is a clean 400.
     */
    public static class FeedLocationFilterInvalidException extends ApiException {
        public FeedLocationFilterInvalidException(String message) {
            super(HttpStatus.BAD_REQUEST, "FEED_LOCATION_FILTER_INVALID", message);
        }
    }
}
