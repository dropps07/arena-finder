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
                .pricePerHour(request.getPricePerHour())
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

    // get arenas by sport
    public List<ArenaResponse> getArenasBySport(Arena.Sport sport) {
        return arenaRepository.findBySport(sport).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ArenaResponse> getArenasNearby(Double lat, Double lng,
            Double radiusKm, String sport) {
        // Haversine formula — finds arenas within radiusKm
        return arenaRepository.findAll().stream()
                .filter(arena -> {
                    double distance = calculateDistance(lat, lng,
                            arena.getLatitude(), arena.getLongitude());
                    return distance <= radiusKm;
                })
                .filter(arena -> sport == null ||
                        arena.getSport().name().equals(sport.toUpperCase()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private double calculateDistance(double lat1, double lng1,
            double lat2, double lng2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // get arenas by city and sport
    public List<ArenaResponse> getArenasByCityAndSport(String city, Arena.Sport sport) {
        return arenaRepository.findByCityAndSport(city, sport).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
