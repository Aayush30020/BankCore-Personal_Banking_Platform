package com.bankcore.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialRecommendationResponse(

        BigDecimal currentMonthSpending,

        int financialAlertCount,

        int criticalAlertCount,

        int warningAlertCount,

        BigDecimal recurringMonthlyCost,

        int savingsGoalCount,

        List<Recommendation> recommendations

) {

    public record Recommendation(

            Priority priority,

            String title,

            String message

    ) {
    }

    public enum Priority {

        HIGH,

        MEDIUM,

        LOW,

        INFO
    }
}