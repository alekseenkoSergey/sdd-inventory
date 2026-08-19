package org.example.sddinventory.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
