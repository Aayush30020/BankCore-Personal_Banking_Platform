package com.bankcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SavingsGoalResponse(

        Long id,

        String name,

        BigDecimal targetAmount,

        BigDecimal currentAmount,

        BigDecimal remainingAmount,

        BigDecimal progressPercentage,

        LocalDate targetDate,

        String currency,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}