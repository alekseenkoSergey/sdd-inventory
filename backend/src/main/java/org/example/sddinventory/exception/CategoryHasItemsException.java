package org.example.sddinventory.exception;

public class CategoryHasItemsException extends RuntimeException {
    private final int itemCount;

    public CategoryHasItemsException(String message, int itemCount) {
        super(message);
        this.itemCount = itemCount;
    }

    public CategoryHasItemsException(String message, int itemCount, Throwable cause) {
        super(message, cause);
        this.itemCount = itemCount;
    }

    public int getItemCount() {
        return itemCount;
    }
}
