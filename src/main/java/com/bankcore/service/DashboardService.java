package com.bankcore.service;

import com.bankcore.dto.AccountResponse;
import com.bankcore.dto.CategorySpendingResponse;
import com.bankcore.dto.DashboardResponse;
import com.bankcore.dto.FinancialInsightsResponse;
import com.bankcore.dto.MonthlySpendingAnalysisResponse;
import com.bankcore.dto.TransactionHistoryResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountService accountService;

    private final FinancialInsightsService financialInsightsService;

    private final MonthlySpendingAnalysisService
            monthlySpendingAnalysisService;

    private final CategorySpendingService categorySpendingService;

    private final TransactionQueryService transactionQueryService;


    // ============================================================
    // GET DASHBOARD DATA
    // ============================================================

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        // --------------------------------------------------------
        // ACCOUNTS
        // --------------------------------------------------------

        List<AccountResponse> accounts =
                accountService.getMyAccounts();


        BigDecimal totalBalance =
                accounts.stream()
                        .map(AccountResponse::balance)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        // --------------------------------------------------------
        // FINANCIAL INSIGHTS
        // --------------------------------------------------------

        FinancialInsightsResponse financialInsights =
                financialInsightsService
                        .getMyFinancialInsights();


        // --------------------------------------------------------
        // CURRENT MONTH SPENDING
        // --------------------------------------------------------

        LocalDate today =
                LocalDate.now();

        LocalDate currentMonthStart =
                today.withDayOfMonth(1);

        MonthlySpendingAnalysisResponse
                currentMonthSpending =
                monthlySpendingAnalysisService
                        .getSpendingAnalysis(
                                currentMonthStart,
                                today
                        );


        // --------------------------------------------------------
        // CATEGORY SPENDING
        // --------------------------------------------------------

        CategorySpendingResponse categorySpending =
                categorySpendingService
                        .getMyCategorySpending();


        // --------------------------------------------------------
        // RECENT TRANSACTIONS
        // --------------------------------------------------------

        List<TransactionHistoryResponse>
                recentTransactions =
                transactionQueryService
                        .getMyRecentTransactions(5);


        // --------------------------------------------------------
        // RETURN DASHBOARD
        // --------------------------------------------------------

        return new DashboardResponse(

                accounts,

                totalBalance,

                financialInsights,

                currentMonthSpending,

                categorySpending,

                recentTransactions
        );
    }
}