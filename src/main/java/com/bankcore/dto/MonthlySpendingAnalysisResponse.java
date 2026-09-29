package com.bankcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlySpendingAnalysisResponse(

        LocalDate startDate,

        LocalDate endDate,

        BigDecimal totalSpent,

        long transactionCount,

        BigDecimal averageTransactionAmount,

        BigDecimal largestTransactionAmount,

        BigDecimal smallestTransactionAmount

) {
}