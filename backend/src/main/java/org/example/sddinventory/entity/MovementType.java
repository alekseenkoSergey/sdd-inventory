package org.example.sddinventory.entity;

public enum MovementType {
    OPENING_BALANCE("Opening Balance"),
    STOCK_IN("Stock In"),
    STOCK_OUT("Stock Out"),
    ADJUSTMENT("Adjustment");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
