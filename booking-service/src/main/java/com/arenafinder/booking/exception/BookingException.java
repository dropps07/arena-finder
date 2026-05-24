package com.arenafinder.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BookingException {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(Long id) {
            super("Booking not found with id: " + id);
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class SlotAlreadyBookedException extends RuntimeException {
        public SlotAlreadyBookedException() {
            super("This slot is already booked");
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class UnauthorizedBookingAccessException extends RuntimeException {
        public UnauthorizedBookingAccessException() {
            super("You don't have permission to modify this booking");
        }
    }
}