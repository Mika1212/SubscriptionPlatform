package org.mika1212.common.exception;

import org.mika1212.subscription.exception.SubscriptionActivateDateException;
import org.mika1212.subscription.exception.SubscriptionAlreadyExistsException;
import org.mika1212.subscription.exception.SubscriptionNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    ///  TODO выводить структурированное сообщение об ошибках
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message", ex.getMessage(),
                        "error", "BAD_REQUEST"
                )
        );
    }

    ///  TODO выводить структурированное сообщение об ошибках
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonParse(HttpMessageNotReadableException ex) {

        return ResponseEntity.badRequest().body(
                Map.of(
                        "message", ex.getMessage(),
                        "error", "BAD_REQUEST"
                )
        );
    }

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleAlreadyExists(SubscriptionAlreadyExistsException ex) {

        return ResponseEntity.status(409).body(
                new ApiErrorResponse(
                        ex.getMessage(),
                        "SUBSCRIPTION_ALREADY_EXISTS",
                        409,
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(SubscriptionNotFoundException ex) {

        return ResponseEntity.status(404).body(
                new ApiErrorResponse(
                        ex.getMessage(),
                        "SUBSCRIPTION_NOT_FOUND",
                        404,
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(SubscriptionActivateDateException.class)
    public ResponseEntity<ApiErrorResponse> handlePastDate(SubscriptionActivateDateException ex) {

        return ResponseEntity.status(409).body(
                new ApiErrorResponse(
                        ex.getMessage(),
                        "SUBSCRIPTION_DATE_INVALID",
                        409,
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex) {

        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(
                        ex.getMessage(),
                        "BAD_REQUEST",
                        400,
                        Instant.now()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {

        return ResponseEntity.status(500).body(
                new ApiErrorResponse(
                        "Internal server error",
                        "INTERNAL_ERROR",
                        500,
                        Instant.now()
                )
        );
    }
}
