package com.arenafinder.gateway.filter;

import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j

/**
 * WHY A GLOBAL FILTER?
 * Runs on EVERY request before it reaches any service.
 * One place to enforce auth — not scattered across services.
 *
 * WHY REACTIVE (Mono)?
 * Gateway uses WebFlux — non-blocking reactive framework.
 * Everything returns Mono<> instead of plain objects.
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;

    // Public routes — no token needed
    private static final List<String> PUBLIC_ROUTES = List.of(
            "/auth/login",
            "/auth/register",
            "/actuator/health");

    public JwtAuthFilter(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        log.debug("=== GATEWAY FILTER ===");
        log.debug("Path: {}", path);

        if (PUBLIC_ROUTES.stream().anyMatch(path::startsWith)) {
            log.debug("Public route — passing through");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders().getFirst("Authorization");
        log.debug("Auth header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid auth header found");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        log.debug("Token length: {}", token.length());

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.debug("Token valid — userId: {}", claims.getSubject());

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", claims.getSubject())
                            .header("X-User-Role", claims.get("role", String.class)))
                    .build();

            return chain.filter(modifiedExchange);

        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Run before all other filters
    }
}