package org.example.sddinventory.config;

import org.example.sddinventory.exception.CategoryHasItemsException;
import org.example.sddinventory.exception.CategoryNameNotUniqueException;
import org.example.sddinventory.exception.CategoryNotFoundException;
import org.example.sddinventory.exception.InventoryItemNotFoundException;
import org.example.sddinventory.exception.LocationHasItemsException;
import org.example.sddinventory.exception.LocationNameNotUniqueException;
import org.example.sddinventory.exception.LocationNotFoundException;
import org.example.sddinventory.exception.SkuDuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

    @ExceptionHandler(CategoryNameNotUniqueException.class)
    public ResponseEntity<?> handleCategoryNameNotUniqueException(CategoryNameNotUniqueException ex, WebRequest request) {
        logger.warn("Category name not unique: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "CATEGORY_NAME_NOT_UNIQUE");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CategoryHasItemsException.class)
    public ResponseEntity<?> handleCategoryHasItemsException(CategoryHasItemsException ex, WebRequest request) {
        logger.warn("Cannot delete category with items: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "CATEGORY_HAS_ITEMS");
        body.put("message", ex.getMessage());
        body.put("itemCount", ex.getItemCount());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<?> handleCategoryNotFoundException(CategoryNotFoundException ex, WebRequest request) {
        logger.warn("Category not found: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "CATEGORY_NOT_FOUND");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LocationNameNotUniqueException.class)
    public ResponseEntity<?> handleLocationNameNotUniqueException(LocationNameNotUniqueException ex, WebRequest request) {
        logger.warn("Location name not unique: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "LOCATION_NAME_NOT_UNIQUE");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LocationHasItemsException.class)
    public ResponseEntity<?> handleLocationHasItemsException(LocationHasItemsException ex, WebRequest request) {
        logger.warn("Cannot delete location with items: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "LOCATION_HAS_ITEMS");
        body.put("message", ex.getMessage());
        body.put("itemCount", ex.getItemCount());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<?> handleLocationNotFoundException(LocationNotFoundException ex, WebRequest request) {
        logger.warn("Location not found: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "LOCATION_NOT_FOUND");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InventoryItemNotFoundException.class)
    public ResponseEntity<?> handleInventoryItemNotFoundException(InventoryItemNotFoundException ex, WebRequest request) {
        logger.warn("Inventory item not found: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "ITEM_NOT_FOUND");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SkuDuplicateException.class)
    public ResponseEntity<?> handleSkuDuplicateException(SkuDuplicateException ex, WebRequest request) {
        logger.warn("SKU duplicate: {}", ex.getMessage());

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "SKU_DUPLICATE");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        logger.warn("Validation error: {}", message);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", ZonedDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "VALIDATION_ERROR");
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
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
