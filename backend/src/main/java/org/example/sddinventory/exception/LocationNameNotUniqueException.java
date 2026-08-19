package org.example.sddinventory.exception;

public class LocationNameNotUniqueException extends RuntimeException {
    public LocationNameNotUniqueException(String message) {
        super(message);
    }
}
