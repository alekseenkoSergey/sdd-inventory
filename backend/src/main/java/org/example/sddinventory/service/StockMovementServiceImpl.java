package org.example.sddinventory.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class StockMovementServiceImpl implements StockMovementService {
    @Override
    public void createOpeningBalance(Long itemId, BigDecimal quantity) {
        // TODO: Implement stock movement creation for opening balance
    }
}
