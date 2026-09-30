package com.bankcore.dto;

import java.math.BigDecimal;

public record DemoDataResponse(
        boolean created,
        String message,
        Long currentAccountId,
        Long savingsAccountId,
        BigDecimal currentBalance,
        BigDecimal savingsBalance
) {
}