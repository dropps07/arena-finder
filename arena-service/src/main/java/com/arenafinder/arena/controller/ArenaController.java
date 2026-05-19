package com.arenafinder.arena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenafinder.arena.dto.ArenaDTOs.ArenaResponse;
import com.arenafinder.arena.dto.ArenaDTOs.CreateArenaRequest;
import com.arenafinder.arena.dto.ArenaDTOs.UpdateArenaRequest;
import com.arenafinder.arena.service.ArenaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/arenas")
@RequiredArgsConstructor
@Slf4j
public class ArenaController {

    private final ArenaService arenaService;

    @GetMapping
    public ResponseEntity<List<ArenaResponse>> getAllArenas() {
        return ResponseEntity.ok(arenaService.getAllArenas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArenaResponse> getArenaById(@PathVariable Long id) {
        return ResponseEntity.ok(arenaService.getArenaById(id));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<ArenaResponse>> getArenasByCity(@PathVariable String city) {
        return ResponseEntity.ok(arenaService.getArenasByCity(city));
    }

    @PostMapping
    public ResponseEntity<ArenaResponse> createArena(
            @Valid @RequestBody CreateArenaRequest request,
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(arenaService.createArena(request, ownerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArenaResponse> updateArena(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArenaRequest request,
            @RequestHeader("X-User-Id") Long ownerId) {
        return ResponseEntity.ok(arenaService.updateArena(id, request, ownerId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArena(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId) {
        arenaService.deleteArena(id, ownerId);
        return ResponseEntity.noContent().build();
    }
}