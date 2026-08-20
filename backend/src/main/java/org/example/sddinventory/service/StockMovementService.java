package org.example.sddinventory.service;

import java.math.BigDecimal;

public interface StockMovementService {
    void createOpeningBalance(Long itemId, BigDecimal quantity);
}
