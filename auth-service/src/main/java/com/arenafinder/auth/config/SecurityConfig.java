package com.arenafinder.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the Auth Service.
 *
 * WAIT — why does the Auth Service need security config?
 * ────────────────────────────────────────────────────────
 * Spring Security is on the classpath (from pom.xml), so by default
 * it locks down EVERY endpoint with Basic Auth. Without this config,
 * even POST /auth/login would be blocked — you'd need a password to login,
 * which defeats the point.
 *
 * This config says: "The Auth Service's own endpoints are public.
 * JWT validation is handled by the API Gateway, not here."
 *
 * @Configuration — this class defines Spring beans
 * @EnableWebSecurity — activates Spring Security with our custom rules
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the security filter chain — the rules applied to every request.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless REST APIs.
            // CSRF protection is for browser sessions with cookies.
            // We use JWT in Authorization header, so CSRF doesn't apply.
            .csrf(AbstractHttpConfigurer::disable)

            // STATELESS — never create or use HTTP sessions.
            // Every request must carry its own JWT. No server-side state.
            // This is what makes the service horizontally scalable.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Permit all requests to auth endpoints — they ARE the auth endpoints.
            // Everything else is blocked (though the Gateway handles auth for other services).
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * BCryptPasswordEncoder bean — used in AuthService to hash and verify passwords.
     *
     * BCrypt automatically:
     * - Generates a random salt (so same password → different hashes)
     * - Includes the salt in the hash (no separate salt storage needed)
     * - Is slow by design (cost factor = 10 = ~100ms) — brute force is expensive
     *
     * Declaring it as @Bean means Spring manages one instance,
     * shared wherever PasswordEncoder is @Autowired or constructor-injected.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
