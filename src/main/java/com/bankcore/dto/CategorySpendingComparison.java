package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;

public record CategorySpendingComparison(

        TransactionCategory category,

        BigDecimal currentPeriodAmount,

        long currentPeriodTransactions,

        BigDecimal previousPeriodAmount,

        long previousPeriodTransactions,

        BigDecimal amountDifference,

        BigDecimal percentageChange

) {
}