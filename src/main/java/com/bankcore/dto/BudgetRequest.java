package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;

public record BudgetRequest(

        TransactionCategory category,

        BigDecimal monthlyLimit

) {
}