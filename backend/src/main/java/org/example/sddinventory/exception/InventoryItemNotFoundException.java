package org.example.sddinventory.exception;

public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(String message) {
        super(message);
    }

    public InventoryItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
