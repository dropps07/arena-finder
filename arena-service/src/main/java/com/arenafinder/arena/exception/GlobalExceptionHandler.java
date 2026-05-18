package com.arenafinder.arena.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.arenafinder.arena.exception.ArenaException.ArenaNotFoundException;
import com.arenafinder.arena.exception.ArenaException.UnauthorizedArenaAccessException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ArenaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleArenaNotFoundException(
            ArenaNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedArenaAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedArenaAccessException(
            UnauthorizedArenaAccessException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }

    public record ErrorResponse(int status, String error, String message) {
        public Instant timestamp() {
            return Instant.now();
        }
    }
}
