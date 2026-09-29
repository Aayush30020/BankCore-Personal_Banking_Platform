package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;

public record BudgetAnalysisResponse(

        TransactionCategory category,

        BigDecimal monthlyLimit,

        BigDecimal currentMonthSpending,

        BigDecimal remainingAmount,

        BigDecimal usagePercentage,

        long transactionCount,

        String status,

        String currency

) {

    /*
     * Backward-compatible accessor.
     *
     * Some existing code may refer to the spending value
     * as spentAmount().
     */
    public BigDecimal spentAmount() {

        return currentMonthSpending;
    }
}