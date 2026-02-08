package com.omatheusmesmo.shoppmate.utils.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED,
                "Authentication Failed", "The username or password is incorrect");
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ApiError> handleAccountStatus(AccountStatusException ex) {
        log.warn("Account status issue: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN,
                "Account Status Issue", "The account is locked or disabled");
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Access Denied", "You are not authorized to access this resource");
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<ApiError> handleJwtSignature(SignatureException ex) {
        log.warn("Invalid JWT signature: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Invalid Token Signature", "The JWT signature is invalid");
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleExpiredJwt(ExpiredJwtException ex) {
        log.warn("Expired JWT token: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Expired Token", "The JWT token has expired");
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNoSuchElement(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();

        String errorDetails = String.join(", ", errors);
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation Failed", errorDetails);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid Argument", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(UsernameNotFoundException ex) {
        log.warn("User not found: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, "Invalid Argument", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid Argument Type: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid Argument Type", ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    //TODO -> InvalidDataAccessApiUsageException

    // TODO: Tratar DataIntegrityViolationException

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAllUncaughtException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);

        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred", "Internal Server Error - Please contact support.");
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Getter
    public static class ApiError {
        private final int status;
        private final String error;
        private final String message;
        private final String details;

        public ApiError(HttpStatus status, String message, String details) {
            this.status = status.value();
            this.error = status.getReasonPhrase();
            this.message = message;
            this.details = details;
        }

    }
}