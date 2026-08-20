package org.example.sddinventory.exception;

public class NegativeQuantityException extends RuntimeException {
    public NegativeQuantityException(String message) {
        super(message);
    }

    public NegativeQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
