package com.nortal.mid.mock.controller;

import com.nortal.mid.mock.error.ChaosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class MidExceptionHandler {

    @ExceptionHandler({ChaosException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse chaosException(Exception exception) {
        log.error("ChaosException - {}", exception.getMessage());
        return new ErrorResponse(exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse statusException(ResponseStatusException exception) {
        return new ErrorResponse(exception.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse genericException(Exception exception) {
        log.error("Internal server error - {}", exception.getMessage(), exception);
        return new ErrorResponse(exception.getMessage(), LocalDateTime.now());
    }

    private record ErrorResponse(String error, LocalDateTime time) {
    }
}
