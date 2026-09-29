package com.bankcore.service;

import com.bankcore.dto.BudgetAwarenessResponse;
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
public class BudgetAwarenessService {


    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    // ============================================================
    // GET BUDGET AWARENESS FOR ALL MY BUDGETS
    // ============================================================

    @Transactional(readOnly = true)
    public List<BudgetAwarenessResponse>
    getMyBudgetAwareness() {

        User user = getAuthenticatedUser();

        List<Budget> budgets =
                budgetRepository
                        .findByUserIdOrderByCategoryAsc(
                                user.getId()
                        );

        List<BudgetAwarenessResponse> results =
                new ArrayList<>();

        for (Budget budget : budgets) {

            results.add(
                    calculateBudgetAwareness(
                            user.getId(),
                            budget
                    )
            );
        }

        return results;
    }


    // ============================================================
    // GET BUDGET AWARENESS FOR ONE CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    public BudgetAwarenessResponse
    getMyBudgetAwareness(
            TransactionCategory category
    ) {

        if (category == null) {

            throw new IllegalArgumentException(
                    "Budget category is required"
            );
        }

        User user = getAuthenticatedUser();

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

        return calculateBudgetAwareness(
                user.getId(),
                budget
        );
    }


    // ============================================================
    // CALCULATE BUDGET AWARENESS
    // ============================================================

    private BudgetAwarenessResponse
    calculateBudgetAwareness(
            Long userId,
            Budget budget
    ) {

        LocalDate today =
                LocalDate.now();

        LocalDate periodStart =
                today.withDayOfMonth(1);

        LocalDate periodEnd =
                today;


        LocalDateTime startDateTime =
                periodStart.atStartOfDay();

        LocalDateTime endDateTime =
                periodEnd
                        .plusDays(1)
                        .atStartOfDay();


        // --------------------------------------------------------
        // GET ACTUAL CATEGORY SPENDING
        // --------------------------------------------------------

        BigDecimal spentAmount =
                transactionRepository
                        .findCategorySpendingAmountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                budget.getCategory(),
                                startDateTime,
                                endDateTime
                        );


        if (spentAmount == null) {

            spentAmount =
                    BigDecimal.ZERO;
        }


        // --------------------------------------------------------
        // GET TRANSACTION COUNT
        // --------------------------------------------------------

        long transactionCount =
                transactionRepository
                        .findCategorySpendingCountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                budget.getCategory(),
                                startDateTime,
                                endDateTime
                        );


        // --------------------------------------------------------
        // CALCULATE REMAINING AMOUNT
        // --------------------------------------------------------

        BigDecimal remainingAmount =
                budget.getMonthlyLimit()
                        .subtract(spentAmount);


        // --------------------------------------------------------
        // CALCULATE USAGE PERCENTAGE
        // --------------------------------------------------------

        BigDecimal usagePercentage =
                spentAmount
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                budget.getMonthlyLimit(),
                                2,
                                RoundingMode.HALF_UP
                        );


        // --------------------------------------------------------
        // DETERMINE STATUS
        // --------------------------------------------------------

        BudgetAwarenessResponse.BudgetStatus status;


        if (usagePercentage.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {

            status =
                    BudgetAwarenessResponse.BudgetStatus
                            .OVER_BUDGET;

        } else if (usagePercentage.compareTo(
                BigDecimal.valueOf(80)
        ) >= 0) {

            status =
                    BudgetAwarenessResponse.BudgetStatus
                            .NEAR_LIMIT;

        } else {

            status =
                    BudgetAwarenessResponse.BudgetStatus
                            .WITHIN_BUDGET;
        }


        // --------------------------------------------------------
        // RETURN RESULT
        // --------------------------------------------------------

        return new BudgetAwarenessResponse(

                budget.getCategory(),

                budget.getMonthlyLimit(),

                spentAmount,

                remainingAmount,

                usagePercentage,

                transactionCount,

                status,

                periodStart,

                periodEnd
        );
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
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user does not exist"
                        )
                );
    }
}