package org.example.sddinventory.exception;

public class CategoryNameNotUniqueException extends RuntimeException {
    public CategoryNameNotUniqueException(String message) {
        super(message);
    }

    public CategoryNameNotUniqueException(String message, Throwable cause) {
        super(message, cause);
    }
}
