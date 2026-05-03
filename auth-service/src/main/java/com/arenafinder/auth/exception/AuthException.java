package com.arenafinder.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exceptions for the Auth Service.
 *
 * WHY CUSTOM EXCEPTIONS?
 * ───────────────────────
 * Without these, any error becomes a generic 500 Internal Server Error.
 * With them, the correct HTTP status is returned automatically.
 *
 * @ResponseStatus tells Spring: "when this exception is thrown,
 * return this HTTP status code". The GlobalExceptionHandler then
 * formats the response body consistently.
 *
 * Grouping related exceptions in one file (inner classes) keeps
 * the auth-exception logic in one place. Split them out if they grow.
 */
public class AuthException {

    /**
     * Thrown when email/password combination is wrong.
     * Maps to 401 Unauthorized.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException() {
            super("Invalid email or password");
        }
    }

    /**
     * Thrown when a JWT is expired, malformed, or tampered with.
     * Maps to 401 Unauthorized.
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException() {
            super("Token is invalid or expired");
        }
    }

    /**
     * Thrown when registering with an email that already exists.
     * Maps to 409 Conflict — the resource (user) already exists.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String email) {
            super("An account with email '" + email + "' already exists");
        }
    }
}
