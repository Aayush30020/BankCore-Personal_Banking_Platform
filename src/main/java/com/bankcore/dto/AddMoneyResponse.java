package com.bankcore.dto;

import java.math.BigDecimal;

public record AddMoneyResponse(
        Long accountId,
        String accountNumber,
        BigDecimal amount,
        BigDecimal newBalance,
        String transactionReference,
        String message
) {
}