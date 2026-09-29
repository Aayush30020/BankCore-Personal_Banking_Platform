package com.bankcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SavingsGoalRequest(

        String name,

        BigDecimal targetAmount,

        BigDecimal currentAmount,

        LocalDate targetDate

) {
}