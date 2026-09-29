package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;

public record CategorySpendingAggregate(

        TransactionCategory category,

        BigDecimal amount,

        long transactionCount

) {
}