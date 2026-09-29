package com.bankcore.dto;

import com.bankcore.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionHistoryResponse(
        String transactionReference,
        String direction,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String counterpartyAccountNumber,
        String description,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}