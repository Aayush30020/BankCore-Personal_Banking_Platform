package com.bankcore.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransferRequest(

        @NotNull(message = "Source account is required")
        Long fromAccountId,

        @NotNull(message = "Destination account is required")
        Long toAccountId,

        @NotNull(message = "Transfer amount is required")
        @DecimalMin(
                value = "0.01",
                message = "Transfer amount must be greater than zero"
        )
        BigDecimal amount,

        @Size(
                max = 255,
                message = "Description cannot exceed 255 characters"
        )
        String description

) {
}