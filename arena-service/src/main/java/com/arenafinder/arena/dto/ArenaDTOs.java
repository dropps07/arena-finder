package com.arenafinder.arena.dto;

import com.arenafinder.arena.model.Arena;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class ArenaDTOs {
    @Data
    public static class CreateArenaRequest {
        @NotBlank(message = "arena name is req")
        @Size(max = 100, message = "user must be over 100 words ")
        private String name;

        @NotBlank(message = "address is req")
        private String address;

        @NotNull(message = "opening time is req")
        private String openingTime;

        @NotNull(message = "closing time is req")
        private String closingTime;

        @NotBlank(message = "City is req")
        private String city;

        @NotNull(message = "longitude is req")
        private Double longitude;

        @NotNull(message = "latitude is req")
        private Double latitude;

        @NotNull(message = "Price cant be -ve ")
        @Min(value = 1, message = "price cant be zero")
        private Double price;
    }

    @Data
    public static class UpdateArenaRequest {
        private String name;

        private String address;

        private String city;

        private Double latitude;

        private Double longitude;

        private Arena.Sport sport;

        private String openingTime;

        private String closingTime;

        private Double price;
    }

    @Data
    public static class ArenaResponse {
        private Long id;

        private String name;

        private String address;

        private String city;

        private Double longitude;

        private Double latitude;

        private Double price;

        private String openingTime;

        private String closingTime;

        private Arena.Sport sport;
    }
}