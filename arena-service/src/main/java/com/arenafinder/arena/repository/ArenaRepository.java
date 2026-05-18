package com.arenafinder.arena.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenafinder.arena.model.Arena;

@Repository
public interface ArenaRepository extends JpaRepository<Arena, Long> {

    List<Arena> findByCity(String city);

    boolean existsByCity(String city);

    List<Arena> findBySport(Arena.Sport sport); // optional means 0 or 1 result, List mean 0>=

    boolean existsBySport(Arena.Sport sport);
}