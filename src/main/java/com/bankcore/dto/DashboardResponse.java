package com.bankcore.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        // ============================================================
        // ACCOUNTS
        // ============================================================

        List<AccountResponse> accounts,

        BigDecimal totalBalance,


        // ============================================================
        // FINANCIAL INSIGHTS
        // ============================================================

        FinancialInsightsResponse financialInsights,


        // ============================================================
        // CURRENT MONTH SPENDING
        // ============================================================

        MonthlySpendingAnalysisResponse currentMonthSpending,


        // ============================================================
        // CATEGORY SPENDING
        // ============================================================

        CategorySpendingResponse categorySpending,


        // ============================================================
        // RECENT TRANSACTIONS
        // ============================================================

        List<TransactionHistoryResponse> recentTransactions

) {
}