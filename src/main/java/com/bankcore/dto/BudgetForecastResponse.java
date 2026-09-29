package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BudgetForecastResponse(

        TransactionCategory category,

        BigDecimal monthlyBudget,

        BigDecimal spentSoFar,

        BigDecimal remainingBudget,

        BigDecimal averageDailySpend,

        BigDecimal projectedMonthEndSpend,

        BigDecimal projectedUsagePercentage,

        long transactionCount,

        int daysElapsed,

        int daysRemaining,

        int totalDaysInMonth,

        ForecastStatus status,

        LocalDate periodStart,

        LocalDate periodEnd

) {

    public enum ForecastStatus {

        PROJECTED_WITHIN_BUDGET,

        PROJECTED_NEAR_LIMIT,

        PROJECTED_OVER_BUDGET
    }
}