package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;

public record FinancialAlertResponse(

        AlertType type,

        String title,

        String message,

        TransactionCategory category,

        BigDecimal amount,

        BigDecimal referenceAmount,

        BigDecimal percentage,

        AlertSeverity severity

) {

    public enum AlertType {

        BUDGET_EXCEEDED,

        BUDGET_NEAR_LIMIT,

        BUDGET_PROJECTED_OVER,

        SPENDING_INCREASE,

        RECURRING_EXPENSE
    }


    public enum AlertSeverity {

        INFO,

        WARNING,

        CRITICAL
    }
}