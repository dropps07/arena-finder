package com.arenafinder.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.arenafinder.auth.dto.AuthDTOs.LoginRequest;
import com.arenafinder.auth.dto.AuthDTOs.LoginResponse;
import com.arenafinder.auth.dto.AuthDTOs.RegisterRequest;
import com.arenafinder.auth.dto.AuthDTOs.ValidateResponse;
import com.arenafinder.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for authentication endpoints.
 *
 * CONTROLLER RULES (enforced in every service):
 * ───────────────────────────────────────────────
 * 1. No business logic here — only receive, delegate, respond
 * 2. No direct repository calls — always go through Service
 * 3. Methods should be 5-10 lines max
 *
 * If a controller method is getting long, that logic belongs in the service.
 *
 * @RestController = @Controller + @ResponseBody
 *                 Every method return value is serialised to JSON
 *                 automatically.
 *
 *                 @RequestMapping("/auth") — all endpoints in this controller
 *                 are prefixed with /auth
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login, register, and token validation")
// @Tag is Swagger/OpenAPI — groups these endpoints in the docs UI
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     *
     * @Valid triggers the validation annotations on LoginRequest.
     *        If validation fails, Spring throws MethodArgumentNotValidException
     *        before this method is even called — the GlobalExceptionHandler catches
     *        it.
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password", description = "Returns a JWT on success")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/register
     * Returns 201 Created (not 200) — a new resource was created.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user account")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /auth/validate
     *
     * Called by the API Gateway to verify a JWT before forwarding a request.
     * The token comes in the Authorization header as "Bearer <token>".
     *
     * We extract just the token part (strip "Bearer ").
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate a JWT token", description = "Used by the API Gateway")
    public ResponseEntity<ValidateResponse> validate(
            @RequestHeader("Authorization") String authHeader) {

        // "Bearer eyJhbGci..." → "eyJhbGci..."
        String token = authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : authHeader;

        ValidateResponse response = authService.validate(token);
        return ResponseEntity.ok(response);
    }

    // @GetMapping("/generate-hash")
    // public String generateHash() {
    // return new BCryptPasswordEncoder().encode("password123");
    // }
}
