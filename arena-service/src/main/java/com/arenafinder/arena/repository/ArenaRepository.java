package com.arenafinder.arena.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arenafinder.arena.model.Arena;

@Repository
public interface ArenaRepository extends JpaRepository<Arena, Long> {

    List<Arena> findByCity(String city);
    boolean existsByCity(String city);
    List<Arena> findBySport(Arena.Sport sport);
    List<Arena> findByCityAndSport(String city, Arena.Sport sport);

    boolean existsBySport(Arena.Sport sport);
}