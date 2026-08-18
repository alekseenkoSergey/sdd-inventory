package org.example.sddinventory.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger("auth");

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<?> handleOAuth2AuthenticationException(OAuth2AuthenticationException ex, WebRequest request) {
        logger.warn("OAuth error: error={}, details={}, timestamp={}", ex.getError(), ex.getMessage(), System.currentTimeMillis());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "OAuth2AuthenticationException");
        body.put("message", "Authentication failed. Please try again.");
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        logger.warn("Authentication failed: reason={}, error={}, timestamp={}", ex.getClass().getSimpleName(), ex.getMessage(), System.currentTimeMillis());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication required. Please login.");
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Internal server error: error={}, details={}, timestamp={}", ex.getClass().getSimpleName(), ex.getMessage(), System.currentTimeMillis());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "InternalServerError");
        body.put("message", "An unexpected error occurred. Please try again later.");
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
