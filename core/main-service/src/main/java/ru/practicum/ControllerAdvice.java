package ru.practicum;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.exception.ApiError;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@RestControllerAdvice
@Slf4j
public class ControllerAdvice {
    private static List<String> fillErrors(List<String> errors, Throwable cause) {
        while (cause != null) {
            errors.add(cause.getMessage());
            cause = cause.getCause();
        }
        return errors;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiError handleConstraintViolationException(final ConstraintViolationException e) {
        String message = e.getMessage();
        log.warn("ConstraintViolation: {}", message);
        List<String> errors = new ArrayList<>();
        errors.add(message);
        errors = fillErrors(errors, e.getCause());
        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();
        StringJoiner reason = new StringJoiner(", ");
        if (constraintViolations != null) {
            constraintViolations.forEach(x -> reason.add(x.getMessage()));
        }
        return ApiError.builder()
                .errors(errors)
                .message(message)
                .reason(reason.toString())
                .httpStatus(HttpStatus.BAD_REQUEST)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ApiError handleConflictException(final ConflictException e) {
        String message = e.getMessage();
        log.warn("Conflict: {}", message);
        List<String> errors = new ArrayList<>();
        errors.add(message);
        errors = fillErrors(errors, e.getCause());
        return ApiError.builder()
                .errors(errors)
                .httpStatus(HttpStatus.CONFLICT)
                .reason(errors.getLast())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiError handleDataIntegrityException(final DataIntegrityViolationException e) {
        String message = e.getMessage();
        log.warn("DataIntegrityViolation: {}", message);
        List<String> errors = new ArrayList<>();
        errors.add(message);
        errors = fillErrors(errors, e.getCause());
        return ApiError.builder()
                .errors(errors)
                .httpStatus(HttpStatus.CONFLICT)
                .reason(errors.getLast())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiError handleConflictException(final NotFoundException e) {
        String message = e.getMessage();
        log.warn("Not Found: {}", message);
        List<String> errors = new ArrayList<>();
        errors.add(message);
        errors = fillErrors(errors, e.getCause());
        return ApiError.builder()
                .errors(errors)
                .httpStatus(HttpStatus.NOT_FOUND)
                .reason(errors.getLast())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
