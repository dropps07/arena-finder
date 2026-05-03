package com.arenafinder.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity — maps directly to the "users" table in auth-db.
 *
 * IMPORTANT RULE: This class never leaves the auth-service.
 * The REST API always returns a DTO (LoginResponseDTO), never this object.
 * Why? Entities are tied to the database schema. If the schema changes,
 * you don't want it to silently break your API contract.
 *
 * Lombok annotations (saves ~60 lines of boilerplate):
 *   @Data           — generates getters, setters, equals, hashCode, toString
 *   @Builder        — enables: User.builder().email("x").build()
 *   @NoArgsConstructor — JPA requires a no-arg constructor
 *   @AllArgsConstructor — needed by @Builder
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // IDENTITY = postgres SERIAL / auto-increment. Each insert gets the next ID.
    private Long id;

    @Column(nullable = false, unique = true)
    // unique = true → database-level constraint, not just application-level.
    // If two threads race to register the same email, the DB catches it.
    private String email;

    @Column(nullable = false)
    // This stores the BCrypt hash, NOT the plain text password.
    // BCrypt hashes look like: $2a$10$... and are always 60 chars.
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    // EnumType.STRING stores "PLAYER" or "ARENA_OWNER" in the DB.
    // EnumType.ORDINAL stores 0, 1 — avoid it, breaks if you reorder the enum.
    @Column(nullable = false)
    private Role role;

    public enum Role {
        PLAYER,       // regular user who books slots
        ARENA_OWNER   // user who manages arenas
    }
}
