package com.example.challenge.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This class centralizes exception handling for the application. It provides custom responses for
 * validation errors and business-specific exceptions, ensuring error messages are localized
 * based on the current locale.
 */
@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * Extracts validation errors for individual fields and provides localized error messages.
     * Falls back to a default validation error message if a specific message is unavailable.
     *
     * @param ex the {@link MethodArgumentNotValidException} thrown during validation
     * @return a {@link ResponseEntity} containing a map of field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String localizedErrorMessage;
            try {
                localizedErrorMessage = messageSource.getMessage(fieldError, locale);
            } catch (Exception e) {
                localizedErrorMessage = messageSource.getMessage("validation.exception.default.message", null, locale);
            }
            errors.put(fieldError.getField(), localizedErrorMessage);
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Provides a localized error message based on the exception's message key.
     * Falls back to a default business error message if the key is not found.
     *
     * @param ex the {@link BusinessException} containing the error details
     * @return a {@link ResponseEntity} containing the localized error message
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        Locale locale = LocaleContextHolder.getLocale();
        String localizedMessage;
        try {
            localizedMessage = messageSource.getMessage(ex.getMessageKey(), null, locale);
        } catch (Exception e) {
            localizedMessage = messageSource.getMessage("business.error.default", null, locale);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", ex.getStatus());
        response.put("error", localizedMessage);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Handles all unhandled exceptions and provides a generic error response.
     *
     * @param ex the exception that was not handled by other handlers
     * @return a ResponseEntity with a standardized error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnhandledExceptions(Exception ex) {
        // Log the exception for debugging
        log.error("Unhandled exception occurred: ", ex);

        // Build a generic error response
        Locale locale = LocaleContextHolder.getLocale();
        String localizedMessage = messageSource.getMessage("business.error.default", null, locale);
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", localizedMessage);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}