package com.arenafinder.booking.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class BookingDTOs {

    @Data
    public static class CreateBookingRequest {

        @NotNull(message = "Arena ID is required")
        private Long arenaId;

        @NotNull(message = "Date is required")
        private LocalDate date;

        @NotBlank(message = "Start time is required")
        private String startTime;

        @NotBlank(message = "End time is required")
        private String endTime;
    }

    @Data
    public static class BookingResponse {
        private Long id;
        private Long arenaId;
        private Long userId;
        private LocalDate date;
        private String startTime;
        private String endTime;
        private String status;
        private Double totalPrice;
    }
}