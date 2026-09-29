package com.bankcore.dto;

import java.math.BigDecimal;

public record FinancialInsightsResponse(
        BigDecimal totalSent,
        BigDecimal totalReceived,
        BigDecimal netFlow,
        BigDecimal totalTransactionVolume,
        long sentTransactionCount,
        long receivedTransactionCount
) {
}