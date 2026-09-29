package com.bankcore.service;

import com.bankcore.dto.FinancialHealthSummaryResponse;
import com.bankcore.dto.FinancialInsightsResponse;
import com.bankcore.dto.MonthlySpendingAnalysisResponse;
import com.bankcore.dto.SpendingAnalysisResponse;
import com.bankcore.dto.SpendingComparisonResponse;
import com.bankcore.dto.AccountResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialHealthService {

    private final AccountService accountService;

    private final FinancialInsightsService financialInsightsService;

    private final SpendingAnalysisService spendingAnalysisService;

    private final MonthlySpendingAnalysisService
            monthlySpendingAnalysisService;

    private final SpendingComparisonService
            spendingComparisonService;


    // ============================================================
    // GET FINANCIAL HEALTH SUMMARY
    // ============================================================

    @Transactional(readOnly = true)
    public FinancialHealthSummaryResponse
    getFinancialHealthSummary() {

        // --------------------------------------------------------
        // CURRENT DATE
        // --------------------------------------------------------

        LocalDate today =
                LocalDate.now();


        // --------------------------------------------------------
        // CURRENT MONTH
        // --------------------------------------------------------

        LocalDate currentMonthStart =
                today.withDayOfMonth(1);

        LocalDate currentMonthEnd =
                today;


        // --------------------------------------------------------
        // PREVIOUS MONTH
        // --------------------------------------------------------

        LocalDate previousMonthStart =
                currentMonthStart.minusMonths(1);

        LocalDate previousMonthEnd =
                currentMonthStart.minusDays(1);


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


        FinancialHealthSummaryResponse.AccountSummary
                accountSummary =
                new FinancialHealthSummaryResponse.AccountSummary(
                        accounts.size(),
                        totalBalance
                );


        // --------------------------------------------------------
        // FINANCIAL INSIGHTS
        // --------------------------------------------------------

        FinancialInsightsResponse financialInsights =
                financialInsightsService
                        .getMyFinancialInsights();


        // --------------------------------------------------------
        // OVERALL SPENDING
        // --------------------------------------------------------

        SpendingAnalysisResponse overallSpending =
                spendingAnalysisService
                        .getSpendingAnalysis();


        // --------------------------------------------------------
        // CURRENT MONTH SPENDING
        // --------------------------------------------------------

        MonthlySpendingAnalysisResponse
                currentMonthAnalysis =
                monthlySpendingAnalysisService
                        .getSpendingAnalysis(
                                currentMonthStart,
                                currentMonthEnd
                        );


        FinancialHealthSummaryResponse.PeriodSpending
                currentMonthSpending =
                new FinancialHealthSummaryResponse.PeriodSpending(
                        currentMonthStart,
                        currentMonthEnd,
                        currentMonthAnalysis
                );


        // --------------------------------------------------------
        // PREVIOUS MONTH SPENDING
        // --------------------------------------------------------

        MonthlySpendingAnalysisResponse
                previousMonthAnalysis =
                monthlySpendingAnalysisService
                        .getSpendingAnalysis(
                                previousMonthStart,
                                previousMonthEnd
                        );


        FinancialHealthSummaryResponse.PeriodSpending
                previousMonthSpending =
                new FinancialHealthSummaryResponse.PeriodSpending(
                        previousMonthStart,
                        previousMonthEnd,
                        previousMonthAnalysis
                );


        // --------------------------------------------------------
        // MONTH-OVER-MONTH COMPARISON
        // --------------------------------------------------------

        SpendingComparisonResponse
                spendingComparison =
                spendingComparisonService
                        .compareSpending(
                                currentMonthStart,
                                currentMonthEnd,
                                previousMonthStart,
                                previousMonthEnd
                        );


        // --------------------------------------------------------
        // FINAL RESPONSE
        // --------------------------------------------------------

        return new FinancialHealthSummaryResponse(

                accountSummary,

                financialInsights,

                overallSpending,

                currentMonthSpending,

                previousMonthSpending,

                spendingComparison
        );
    }
}