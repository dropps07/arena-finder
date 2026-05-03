package com.arenafinder.auth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Catches exceptions thrown anywhere in the service and returns
 * a consistent, structured JSON error response.
 *
 * WITHOUT THIS:
 * Spring returns its default error format which is verbose,
 * inconsistent, and sometimes leaks internal details.
 *
 * WITH THIS:
 * Every error looks the same:
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Invalid email or password",
 *   "timestamp": "2024-01-15T10:30:00Z"
 * }
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * It intercepts exceptions from ALL controllers in this service.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ── Auth-specific exceptions ───────────────────────────────────

    @ExceptionHandler(AuthException.InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            AuthException.InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AuthException.InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            AuthException.InvalidTokenException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AuthException.EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(
            AuthException.EmailAlreadyExistsException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── Validation exceptions (@Valid failed) ─────────────────────

    /**
     * Triggered when @Valid on a @RequestBody fails.
     * Collects ALL field errors and returns them together.
     * So "email missing AND password too short" = one response, not two.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                fieldErrors.toString()
        );
        return ResponseEntity.badRequest().body(response);
    }

    // ── Catch-all (unexpected exceptions) ─────────────────────────

    /**
     * Safety net — catches anything we didn't explicitly handle.
     * Logs the full stack trace (for debugging) but returns a
     * generic message to the client (don't leak internal details).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
    }

    // ── Helper ────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }

    /**
     * The standard error response shape returned for all errors.
     * Using a record (Java 16+) — immutable, no boilerplate needed.
     */
    public record ErrorResponse(int status, String error, String message) {
        // timestamp is always "now" — added automatically
        public Instant timestamp() {
            return Instant.now();
        }
    }
}
