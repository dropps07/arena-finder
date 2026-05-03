package com.arenafinder.auth.repository;

import com.arenafinder.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for the User entity.
 *
 * WHY EXTEND JpaRepository?
 * ─────────────────────────
 * JpaRepository<User, Long> gives you these for free (zero code):
 *   save(user)          → INSERT or UPDATE
 *   findById(id)        → SELECT WHERE id = ?
 *   findAll()           → SELECT *
 *   delete(user)        → DELETE
 *   count()             → SELECT COUNT(*)
 *   existsById(id)      → SELECT EXISTS(...)
 *
 * The <User, Long> means: entity type = User, primary key type = Long.
 *
 * CUSTOM QUERY METHODS:
 * Spring Data reads the method name and generates the SQL automatically.
 * "findByEmail" → SELECT * FROM users WHERE email = ?
 * No @Query annotation needed for simple lookups.
 *
 * Optional<User> is better than returning null —
 * it forces the caller to handle the "not found" case explicitly
 * instead of getting a NullPointerException at runtime.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
