package com.bankcore.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddMoneyRequest(

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "1.00",
                message = "Amount must be at least ₹1"
        )
        @DecimalMax(
                value = "1000000.00",
                message = "A single demo deposit cannot exceed ₹10,00,000"
        )
        @Digits(
                integer = 15,
                fraction = 2,
                message = "Amount can have at most 2 decimal places"
        )
        BigDecimal amount,

        @NotNull(message = "Account id is required")
        Long accountId,

        @Size(
                max = 255,
                message = "Description cannot exceed 255 characters"
        )
        String description
) {
}