package com.arenafinder.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.arenafinder.auth.dto.AuthDTOs.LoginRequest;
import com.arenafinder.auth.dto.AuthDTOs.LoginResponse;
import com.arenafinder.auth.dto.AuthDTOs.RegisterRequest;
import com.arenafinder.auth.dto.AuthDTOs.ValidateResponse;
import com.arenafinder.auth.exception.AuthException;
import com.arenafinder.auth.model.User;
import com.arenafinder.auth.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * All authentication business logic lives here.
 *
 * LAYERED ARCHITECTURE RULE:
 * Controller → Service → Repository
 * Each layer only talks to the layer directly below it.
 * The controller never touches the repository directly.
 * This makes it easy to test the service in isolation (mock the repository).
 *
 * @Slf4j — injects a Logger field named 'log'. Use log.info(), log.error() etc.
 * @RequiredArgsConstructor — generates a constructor for all 'final' fields.
 *                          This is how Spring injects dependencies (constructor
 *                          injection).
 *                          It's preferred over @Autowired because:
 *                          1. Dependencies are immutable (final)
 *                          2. Makes missing deps a compile error, not a runtime
 *                          crash
 *                          3. Easier to test (just call new
 *                          AuthService(mockRepo, mockEncoder, mockJwt))
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Authenticates a user and returns a JWT.
     *
     * We deliberately give the SAME error message for wrong email
     * AND wrong password. This is a security practice called
     * "username enumeration prevention" — if we said "email not found"
     * vs "wrong password", an attacker could enumerate valid emails.
     */
    public LoginResponse login(LoginRequest request) {
        log.info("=== LOGIN DEBUG ===");
        log.info("Email received: '{}'", request.getEmail());
        log.info("Email length: {}", request.getEmail().length());

        // Step 1: find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed — email not found: {}", request.getEmail());
                    // Same message as wrong password — intentional
                    return new AuthException.InvalidCredentialsException();
                });

        log.info("User FOUND: {}", user.getEmail());
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        log.info("Password matches: {}", matches);
        // Step 2: verify password against stored BCrypt hash
        // passwordEncoder.matches(rawPassword, hashedPassword)
        // BCrypt is slow by design — makes brute-force attacks expensive
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed — wrong password for: {}", request.getEmail());
            throw new AuthException.InvalidCredentialsException();
        }

        // Step 3: generate JWT
        String token = jwtUtil.generateToken(user);
        log.info("Login successful for: {}", request.getEmail());

        return LoginResponse.of(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                jwtUtil.getExpirationMs());
    }

    /**
     * Registers a new user and returns a JWT (auto-login after register).
     *
     * @Transactional — if anything fails after userRepository.save(),
     *                the entire operation rolls back. The user isn't half-created.
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check for duplicate email BEFORE trying to save
        // (the DB would also reject it, but this gives a cleaner error message)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException.EmailAlreadyExistsException(request.getEmail());
        }

        // Parse role — default to PLAYER if unrecognised
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = User.Role.PLAYER;
        }

        // Build and save the user
        // NEVER save the raw password — always hash it first
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(role)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        // Generate JWT and return (user is now logged in)
        String token = jwtUtil.generateToken(user);
        return LoginResponse.of(
                token,
                user.getEmail(),
                user.getName(),
                user.getRole().name(),
                jwtUtil.getExpirationMs());
    }

    /**
     * Validates a JWT and returns the user info inside it.
     * Called by the API Gateway on every protected request.
     */
    public ValidateResponse validate(String token) {
        try {
            Claims claims = jwtUtil.validateAndExtract(token);

            return ValidateResponse.of(
                    Long.parseLong(claims.getSubject()),
                    claims.get("email", String.class),
                    claims.get("role", String.class));
        } catch (JwtException e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new AuthException.InvalidTokenException();
        }
    }
}
