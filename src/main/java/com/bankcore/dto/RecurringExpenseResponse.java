package com.bankcore.dto;

import com.bankcore.entity.TransactionCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringExpenseResponse(

        String description,

        TransactionCategory category,

        BigDecimal averageAmount,

        BigDecimal estimatedMonthlyAmount,

        int occurrenceCount,

        double averageIntervalDays,

        RecurrenceFrequency frequency,

        LocalDate firstSeen,

        LocalDate lastSeen

) {

    public enum RecurrenceFrequency {

        WEEKLY,

        MONTHLY,

        UNKNOWN
    }
}