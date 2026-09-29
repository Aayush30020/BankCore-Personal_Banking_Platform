package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetResponse(

        Long id,

        TransactionCategory category,

        BigDecimal monthlyLimit,

        String currency,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}