package com.DevArena.backend.common.exception;

import com.DevArena.backend.battle.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles ResponseStatusException (like BAD_REQUEST, FORBIDDEN etc.)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatus status = (HttpStatus) ex.getStatusCode();

        ErrorResponse error = new ErrorResponse(
                false,
                ex.getReason(),
                status.value()
        );

        return new ResponseEntity<>(error, status);
    }

    // Handles all other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorResponse error = new ErrorResponse(
                false,
                "Something went wrong",
                500
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
