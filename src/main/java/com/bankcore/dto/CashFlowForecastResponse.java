package com.bankcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CashFlowForecastResponse(

        BigDecimal currentBalance,

        BigDecimal currentMonthSpending,

        BigDecimal averageDailySpending,

        BigDecimal projectedMonthEndSpending,

        BigDecimal projectedRemainingSpending,

        BigDecimal projectedEndOfMonthBalance,

        int daysElapsed,

        int daysRemaining,

        int totalDaysInMonth,

        LocalDate periodStart,

        LocalDate periodEnd,

        ForecastStatus status

) {

    public enum ForecastStatus {

        PROJECTED_POSITIVE_BALANCE,

        PROJECTED_LOW_BALANCE,

        PROJECTED_NEGATIVE_BALANCE,

        NO_SPENDING_DATA
    }
}