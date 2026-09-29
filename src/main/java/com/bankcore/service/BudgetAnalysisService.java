package com.bankcore.service;

import com.bankcore.dto.BudgetAnalysisResponse;
import com.bankcore.entity.Budget;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.TransactionStatus;
import com.bankcore.entity.User;
import com.bankcore.repository.BudgetRepository;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetAnalysisService {

    private static final BigDecimal NEAR_LIMIT_PERCENT =
            BigDecimal.valueOf(80);

    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    // ============================================================
    // GET ALL MY BUDGET ANALYSIS
    // ============================================================

    @Transactional(readOnly = true)
    public List<BudgetAnalysisResponse>
    getMyBudgetAnalysis() {

        User user =
                getAuthenticatedUser();

        LocalDate today =
                LocalDate.now();

        LocalDate monthStart =
                today.withDayOfMonth(1);

        LocalDateTime startDateTime =
                monthStart.atStartOfDay();

        LocalDateTime endDateTime =
                today
                        .plusDays(1)
                        .atStartOfDay();

        List<Budget> budgets =
                budgetRepository
                        .findByUserIdOrderByCategoryAsc(
                                user.getId()
                        );

        return buildAnalysis(
                user.getId(),
                budgets,
                startDateTime,
                endDateTime
        );
    }


    // ============================================================
    // GET MY BUDGET ANALYSIS BY CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    public BudgetAnalysisResponse
    getMyBudgetAnalysis(
            TransactionCategory category
    ) {

        if (category == null) {

            throw new IllegalArgumentException(
                    "Budget category is required"
            );
        }

        User user =
                getAuthenticatedUser();

        Budget budget =
                budgetRepository
                        .findByUserIdAndCategory(
                                user.getId(),
                                category
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No budget found for category "
                                                + category
                                )
                        );

        LocalDate today =
                LocalDate.now();

        LocalDate monthStart =
                today.withDayOfMonth(1);

        LocalDateTime startDateTime =
                monthStart.atStartOfDay();

        LocalDateTime endDateTime =
                today
                        .plusDays(1)
                        .atStartOfDay();

        return buildAnalysisForBudget(
                user.getId(),
                budget,
                startDateTime,
                endDateTime
        );
    }


    // ============================================================
    // BUILD ALL ANALYSIS
    // ============================================================

    private List<BudgetAnalysisResponse>
    buildAnalysis(
            Long userId,
            List<Budget> budgets,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        List<BudgetAnalysisResponse> analysis =
                new ArrayList<>();

        for (Budget budget : budgets) {

            analysis.add(
                    buildAnalysisForBudget(
                            userId,
                            budget,
                            startDateTime,
                            endDateTime
                    )
            );
        }

        return analysis;
    }


    // ============================================================
    // BUILD SINGLE BUDGET ANALYSIS
    // ============================================================

    private BudgetAnalysisResponse
    buildAnalysisForBudget(
            Long userId,
            Budget budget,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {

        TransactionCategory category =
                budget.getCategory();

        BigDecimal currentMonthSpending =
                transactionRepository
                        .findCategorySpendingAmountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                category,
                                startDateTime,
                                endDateTime
                        );

        if (currentMonthSpending == null) {

            currentMonthSpending =
                    BigDecimal.ZERO;
        }

        long transactionCount =
                transactionRepository
                        .findCategorySpendingCountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                category,
                                startDateTime,
                                endDateTime
                        );

        BigDecimal monthlyLimit =
                budget.getMonthlyLimit();

        BigDecimal remainingAmount =
                monthlyLimit.subtract(
                        currentMonthSpending
                );

        BigDecimal usagePercentage =
                currentMonthSpending
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                monthlyLimit,
                                2,
                                RoundingMode.HALF_UP
                        );

        String status =
                determineStatus(
                        currentMonthSpending,
                        monthlyLimit,
                        usagePercentage
                );

        return new BudgetAnalysisResponse(
                category,
                monthlyLimit,
                currentMonthSpending,
                remainingAmount,
                usagePercentage,
                transactionCount,
                status,
                budget.getCurrency()
        );
    }


    // ============================================================
    // DETERMINE STATUS
    // ============================================================

    private String determineStatus(
            BigDecimal spending,
            BigDecimal limit,
            BigDecimal usagePercentage
    ) {

        if (spending.compareTo(limit) > 0) {

            return "EXCEEDED";
        }

        if (spending.compareTo(limit) == 0) {

            return "AT_LIMIT";
        }

        if (usagePercentage.compareTo(
                NEAR_LIMIT_PERCENT
        ) >= 0) {

            return "NEAR_LIMIT";
        }

        return "ON_TRACK";
    }


    // ============================================================
    // AUTHENTICATED USER
    // ============================================================

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user not found"
            );
        }

        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user does not exist"
                        )
                );
    }
}