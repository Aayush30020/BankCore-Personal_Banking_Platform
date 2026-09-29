package com.bankcore.dto;

import java.time.LocalDate;

public record FinancialHealthSummaryResponse(

        AccountSummary accountSummary,

        FinancialInsightsResponse financialInsights,

        SpendingAnalysisResponse overallSpending,

        PeriodSpending currentMonthSpending,

        PeriodSpending previousMonthSpending,

        SpendingComparisonResponse spendingComparison
) {

    public record AccountSummary(

            int accountCount,

            java.math.BigDecimal totalBalance

    ) {
    }

    public record PeriodSpending(

            LocalDate startDate,

            LocalDate endDate,

            MonthlySpendingAnalysisResponse analysis

    ) {
    }
}