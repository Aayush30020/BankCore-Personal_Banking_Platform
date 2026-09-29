package com.bankcore.dto;

import java.math.BigDecimal;

public record SpendingAnalysisResponse(

        BigDecimal totalSpent,

        long transactionCount,

        BigDecimal averageTransactionAmount,

        BigDecimal largestTransactionAmount,

        BigDecimal smallestTransactionAmount,

        BigDecimal netSpending

) {
}