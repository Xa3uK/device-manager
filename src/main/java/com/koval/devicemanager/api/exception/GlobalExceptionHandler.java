package com.koval.devicemanager.api.exception;

import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.InvalidNullException;
import com.koval.devicemanager.api.dto.response.ErrorResponse;
import com.koval.devicemanager.domain.exception.DeviceNotFoundException;
import com.koval.devicemanager.domain.exception.PageSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeviceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleDeviceNotFound(DeviceNotFoundException ex, HttpServletRequest request) {
        return new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(PageSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePageSizeExceeded(PageSizeExceededException ex, HttpServletRequest request) {
        return new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        if (ex.getCause() instanceof InvalidNullException cause) {
            return nullFieldError(cause, request);
        }
        if (ex.getCause() instanceof InvalidFormatException cause && cause.getTargetType().isEnum()) {
            return invalidEnumValueError(cause, request);
        }
        return badRequest("Malformed request body", request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidSortProperty(PropertyReferenceException ex, HttpServletRequest request) {
        return badRequest("Invalid sort property '" + ex.getPropertyName() + "'. " +
                "Valid properties: id, name, brand, state, createdAt, updatedAt", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ErrorResponse handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return new ErrorResponse(
            Instant.now(),
            HttpStatus.UNPROCESSABLE_CONTENT.value(),
            HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
    }

    private ErrorResponse nullFieldError(InvalidNullException cause, HttpServletRequest request) {
        String fieldName = cause.getPath().get(0).getPropertyName();
        return badRequest(String.format("Field '%s' must not be null", fieldName), request);
    }

    private ErrorResponse invalidEnumValueError(InvalidFormatException cause, HttpServletRequest request) {
        String fieldName = cause.getPath().get(0).getPropertyName();
        String validValues = Arrays.stream(cause.getTargetType().getEnumConstants())
            .map(Object::toString)
            .collect(Collectors.joining(", "));
        return badRequest(String.format("Invalid value '%s' for field '%s'. Valid values are: %s",
            cause.getValue(), fieldName, validValues), request);
    }

    private ErrorResponse badRequest(String message, HttpServletRequest request) {
        return new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(), message, request.getRequestURI());
    }
}
