package com.wowinfobiz.authenticationservice.controller;

import com.wowinfobiz.authenticationservice.dto.MessageResponseDTO;
import java.util.Collections;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new MessageResponseDTO(ex.getMessage(), "FAILED", Collections.emptyMap()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MessageResponseDTO> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponseDTO(ex.getMessage(), "FAILED", Collections.emptyMap()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null
                ? ex.getMostSpecificCause().getMessage()
                : "Data integrity violation";
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponseDTO(message, "FAILED", Collections.emptyMap()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponseDTO("Forbidden", "FAILED", Collections.emptyMap()));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleAuthMissing(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponseDTO("Unauthorized", "FAILED", Collections.emptyMap()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponseDTO("Unexpected server error: " + ex.getMessage(), "FAILED", Collections.emptyMap()));
    }
}
