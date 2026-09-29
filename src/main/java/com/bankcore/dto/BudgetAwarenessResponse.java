package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetAwarenessResponse(

        TransactionCategory category,

        BigDecimal monthlyLimit,

        BigDecimal spentAmount,

        BigDecimal remainingAmount,

        BigDecimal usagePercentage,

        long transactionCount,

        BudgetStatus status,

        LocalDate periodStart,

        LocalDate periodEnd

) {

    public enum BudgetStatus {

        WITHIN_BUDGET,

        NEAR_LIMIT,

        OVER_BUDGET
    }
}