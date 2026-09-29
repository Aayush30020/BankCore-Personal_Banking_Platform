package com.bankcore.dto;

import com.bankcore.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(

        Long transactionId,

        String transactionReference,

        Long fromAccountId,

        Long toAccountId,

        BigDecimal amount,

        String currency,

        TransactionStatus status,

        String description,

        LocalDateTime createdAt,

        LocalDateTime completedAt

) {
}