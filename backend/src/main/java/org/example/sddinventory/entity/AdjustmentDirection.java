package org.example.sddinventory.entity;

public enum AdjustmentDirection {
    INCREASE("Increase"),
    DECREASE("Decrease");

    private final String label;

    AdjustmentDirection(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
