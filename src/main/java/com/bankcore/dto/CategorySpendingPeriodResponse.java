package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CategorySpendingPeriodResponse(

        LocalDate startDate,

        LocalDate endDate,

        TransactionCategory category,

        BigDecimal totalSpending,

        long transactionCount
) {
}