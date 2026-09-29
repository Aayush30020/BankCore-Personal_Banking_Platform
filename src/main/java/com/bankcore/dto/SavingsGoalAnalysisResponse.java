package com.bankcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoalAnalysisResponse(

        Long id,

        String name,

        BigDecimal targetAmount,

        BigDecimal currentAmount,

        BigDecimal remainingAmount,

        BigDecimal progressPercentage,

        LocalDate targetDate,

        long daysRemaining,

        long monthsRemaining,

        BigDecimal requiredDailySaving,

        BigDecimal requiredMonthlySaving,

        String status,

        String currency

) {
}