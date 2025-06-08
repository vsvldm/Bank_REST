package com.example.bankcards.exception.handler;

import com.example.bankcards.exception.exception.*;
import com.example.bankcards.exception.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestExceptions(final BadRequestException e) {
        return new ApiError(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundExceptions(final NotFoundException e) {
        return new ApiError(HttpStatus.NOT_FOUND,
                "The required object was not found.",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleUnauthorizedExceptions(final UnauthorizedException e) {
        return new ApiError(HttpStatus.UNAUTHORIZED,
                "Authorization error.",
                e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationExceptions(final MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation error: ");

        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errorMessage
                        .append(fieldError.getDefaultMessage())
                        .append("; ");
            } else {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }
        });

        if (!errorMessage.isEmpty()) {
            errorMessage.setLength(errorMessage.length() - 2);
        }

        return new ApiError(HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                errorMessage.toString());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleEncryptionExceptions(final EncryptionException e) {
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Encryption error.",
                e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleCreationExceptions(final CreationException e) {
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Creation error.",
                e.getMessage());
    }
}
