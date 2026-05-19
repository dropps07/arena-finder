package com.arenafinder.arena.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.arenafinder.arena.dto.ArenaDTOs.ArenaResponse;
import com.arenafinder.arena.dto.ArenaDTOs.CreateArenaRequest;
import com.arenafinder.arena.dto.ArenaDTOs.UpdateArenaRequest;
import com.arenafinder.arena.exception.ArenaException.ArenaNotFoundException;
import com.arenafinder.arena.exception.ArenaException.UnauthorizedArenaAccessException;
import com.arenafinder.arena.model.Arena;
import com.arenafinder.arena.repository.ArenaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArenaService {
    private final ArenaRepository arenaRepository;

    @Transactional
    public ArenaResponse createArena(CreateArenaRequest request, Long ownerId) {
        log.info("Creating arena: {}", request.getName());
        Arena arena = Arena.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .pricePerHour(request.getPrice())
                .sport(request.getSport())
                .ownerId(ownerId)
                .build();
        arena = arenaRepository.save(arena);
        log.info("Arena created successfully with ID: {}", arena.getId());
        return mapToResponse(arena);
    }

    private ArenaResponse mapToResponse(Arena arena) {
        ArenaResponse response = new ArenaResponse();
        response.setId(arena.getId());
        response.setName(arena.getName());
        response.setAddress(arena.getAddress());
        response.setCity(arena.getCity());
        response.setLatitude(arena.getLatitude());
        response.setLongitude(arena.getLongitude());
        response.setOpenTime(arena.getOpenTime());
        response.setCloseTime(arena.getCloseTime());
        response.setPricePerHour(arena.getPricePerHour());
        response.setSport(arena.getSport());
        return response;
    }

    // get one arena by id
    public ArenaResponse getArenaById(Long id) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new ArenaNotFoundException(id));
        return mapToResponse(arena);
    }

    // get all arenas
    public List<ArenaResponse> getAllArenas() {
        return arenaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // get by city
    public List<ArenaResponse> getArenasByCity(String city) {
        return arenaRepository.findByCity(city).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // update — only owner can update
    @Transactional
    public ArenaResponse updateArena(Long id, UpdateArenaRequest request, Long ownerId) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new ArenaNotFoundException(id));

        if (!arena.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedArenaAccessException();
        }
        if (request.getName() != null)
            arena.setName(request.getName());
        if (request.getAddress() != null)
            arena.setAddress(request.getAddress());
        if (request.getCity() != null)
            arena.setCity(request.getCity());
        if (request.getLatitude() != null)
            arena.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            arena.setLongitude(request.getLongitude());
        if (request.getOpenTime() != null)
            arena.setOpenTime(request.getOpenTime());
        if (request.getCloseTime() != null)
            arena.setCloseTime(request.getCloseTime());
        if (request.getPricePerHour() != null)
            arena.setPricePerHour(request.getPricePerHour());
        if (request.getSport() != null)
            arena.setSport(request.getSport());
        arena = arenaRepository.save(arena); // ← was missing
        log.info("Arena updated successfully with ID: {}", arena.getId());
        return mapToResponse(arena);
    }

    // delete — only owner can delete
    @Transactional
    public void deleteArena(Long id, Long ownerId) {
        Arena arena = arenaRepository.findById(id)
                .orElseThrow(() -> new ArenaNotFoundException(id));
        if (!arena.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedArenaAccessException();
        }
        arenaRepository.delete(arena);
        log.info("Arena deleted successfully with ID: {}", arena.getId());
    }
}
