package com.arenafinder.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO = Data Transfer Object.
 *
 * WHY NOT USE THE User ENTITY DIRECTLY?
 * ──────────────────────────────────────
 * 1. Security: the User entity has a 'password' field. If you accidentally
 * return the entity, you leak the hashed password in the response.
 * 2. Decoupling: your API contract is separate from your DB schema.
 * You can rename DB columns without breaking the API.
 * 3. Validation: @NotBlank etc. belong on the INPUT object (DTO),
 * not the storage object (Entity).
 *
 * This file has multiple DTOs — grouped here since they're small.
 * As they grow, split into separate files.
 */
public class AuthDTOs {

    // ── REQUEST DTOs (what the client sends) ──────────────────────

    /**
     * Body for POST /auth/login
     * Validation annotations are checked BEFORE the controller method runs.
     * If validation fails, Spring returns 400 automatically — no if/else needed.
     */
    @Data
    public static class LoginRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }

    /**
     * Body for POST /auth/register
     */
    @Data
    public static class RegisterRequest {

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        // Role defaults to PLAYER if not provided — most users are players
        private String role = "PLAYER";
    }

    // ── RESPONSE DTOs (what the server returns) ───────────────────

    /**
     * Returned after successful login or register.
     * Contains the JWT the client must include in future requests.
     */
    @Data
    public static class LoginResponse {
        private String token;
        private String email;
        private String name;
        private String role;
        private long expiresIn; // milliseconds

        // Static factory — cleaner than a big constructor call
        public static LoginResponse of(String token, String email,
                String name, String role, long expiresIn) {
            LoginResponse r = new LoginResponse();
            r.token = token;
            r.email = email;
            r.name = name;
            r.role = role;
            r.expiresIn = expiresIn;
            return r;
        }
    }

    /**
     * Returned by GET /auth/validate
     * The API Gateway calls this endpoint to verify a JWT before
     * forwarding a request to another service.
     */
    @Data
    public static class ValidateResponse {
        private Long userId;
        private String email;
        private String role;
        private boolean valid;

        public static ValidateResponse of(Long userId, String email,
                String role) {
            ValidateResponse r = new ValidateResponse();
            r.userId = userId;
            r.email = email;
            r.role = role;
            r.valid = true;
            return r;
        }
    }
}
