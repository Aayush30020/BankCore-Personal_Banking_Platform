package com.bankcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SpendingComparisonResponse(

        LocalDate currentPeriodStart,

        LocalDate currentPeriodEnd,

        BigDecimal currentPeriodSpent,

        long currentPeriodTransactionCount,

        LocalDate previousPeriodStart,

        LocalDate previousPeriodEnd,

        BigDecimal previousPeriodSpent,

        long previousPeriodTransactionCount,

        BigDecimal difference,

        BigDecimal percentageChange

) {
}