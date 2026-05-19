package com.arenafinder.arena.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ArenaException {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ArenaNotFoundException extends RuntimeException {
        public ArenaNotFoundException(Long id) {
            super("Arena not found with id: " + id); // 404: Resource doesnt exist, 409: conflict (duplicate)
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN) // 401: Unauthorized, 403: Forbidden
    public static class UnauthorizedArenaAccessException extends RuntimeException {
        public UnauthorizedArenaAccessException() {
            super("Not Authorized");
        }
    }
}
