package com.arenafinder.arena.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arenafinder.arena.dto.ArenaDTOs.ArenaResponse;
import com.arenafinder.arena.service.ArenaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/arenas")
@RequiredArgsConstructor
@Slf4j
public class ArenaController {
    private final ArenaService arenaService;

    @GetMapping("/arenas")
    public List<ArenaResponse> getAllArenas() {
        return arenaService.getAllArenas();
    }

}
