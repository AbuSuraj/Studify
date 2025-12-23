package com.edutech.studify.exception;

import com.edutech.studify.dto.response.GradeResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ============= Custom Exceptions =============

    /**
     * Handle ResourceNotFoundException
     * Status: 404 NOT FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle DuplicateResourceException
     * Status: 409 CONFLICT
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handle BadRequestException
     * Status: 400 BAD REQUEST
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle BusinessException
     * Status: 400 BAD REQUEST
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Business Rule Violation")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UnauthorizedException
     * Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle ForbiddenException
     * Status: 403 FORBIDDEN
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle InvalidCredentialsException
     * Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // ============= Spring Security Exceptions =============

    /**
     * Handle Spring Security BadCredentialsException
     * Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Invalid username or password")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle Spring Security AccessDeniedException
     * Status: 403 FORBIDDEN
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("You don't have permission to access this resource")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // ============= Validation Exceptions =============

    /**
     * Handle MethodArgumentNotValidException (validation errors)
     * Status: 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<GradeResponse.ErrorResponse.ValidationError> validationErrors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();

            validationErrors.add(GradeResponse.ErrorResponse.ValidationError.builder()
                    .field(fieldName)
                    .message(errorMessage)
                    .rejectedValue(rejectedValue)
                    .build());
        });

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed. Check 'errors' field for details.")
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle MethodArgumentTypeMismatchException
     * Status: 400 BAD REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(),
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ============= Generic Exception Handler =============

    /**
     * Handle all other exceptions
     * Status: 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GradeResponse.ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        GradeResponse.ErrorResponse errorResponse = GradeResponse.ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();

        // Log the exception for debugging
        ex.printStackTrace();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
