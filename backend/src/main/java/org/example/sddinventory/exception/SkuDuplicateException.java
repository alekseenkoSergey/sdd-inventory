package org.example.sddinventory.exception;

public class SkuDuplicateException extends RuntimeException {
    public SkuDuplicateException(String message) {
        super(message);
    }

    public SkuDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}
