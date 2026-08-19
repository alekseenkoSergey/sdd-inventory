package org.example.sddinventory.exception;

public class LocationHasItemsException extends RuntimeException {
    private final Integer itemCount;

    public LocationHasItemsException(String message, Integer itemCount) {
        super(message);
        this.itemCount = itemCount;
    }

    public Integer getItemCount() {
        return itemCount;
    }
}
