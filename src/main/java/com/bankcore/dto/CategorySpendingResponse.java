package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;
import java.util.List;

public record CategorySpendingResponse(

        BigDecimal totalSpending,

        int totalTransactions,

        List<CategoryBreakdown> categories
) {

    public record CategoryBreakdown(

            TransactionCategory category,

            BigDecimal amount,

            int transactionCount
    ) {
    }
}