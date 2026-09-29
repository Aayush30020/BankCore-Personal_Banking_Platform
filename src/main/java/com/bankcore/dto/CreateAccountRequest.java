package com.bankcore.dto;

import com.bankcore.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

        @NotNull(message = "Account type is required")
        AccountType type

) {
}