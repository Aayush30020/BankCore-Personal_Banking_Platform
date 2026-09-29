package com.bankcore.dto;

import com.bankcore.entity.AccountStatus;
import com.bankcore.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(

        Long id,

        String accountNumber,

        AccountType type,

        AccountStatus status,

        BigDecimal balance,

        String currency,

        LocalDateTime createdAt

) {
}