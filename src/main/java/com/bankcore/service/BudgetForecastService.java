package com.bankcore.service;

import com.bankcore.dto.BudgetForecastResponse;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetForecastService {

    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    // ============================================================
    // GET FORECAST FOR ALL MY BUDGETS
    // ============================================================

    @Transactional(readOnly = true)
    public List<BudgetForecastResponse> getMyBudgetForecasts() {

        User user = getAuthenticatedUser();

        List<Budget> budgets =
                budgetRepository
                        .findByUserIdOrderByCategoryAsc(
                                user.getId()
                        );

        List<BudgetForecastResponse> results =
                new ArrayList<>();

        for (Budget budget : budgets) {

            results.add(
                    calculateForecast(
                            user.getId(),
                            budget
                    )
            );
        }

        return results;
    }


    // ============================================================
    // GET FORECAST FOR ONE CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    public BudgetForecastResponse getMyBudgetForecast(
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

        return calculateForecast(
                user.getId(),
                budget
        );
    }


    // ============================================================
    // CALCULATE FORECAST
    // ============================================================

    private BudgetForecastResponse calculateForecast(
            Long userId,
            Budget budget
    ) {

        LocalDate today =
                LocalDate.now();

        YearMonth currentMonth =
                YearMonth.from(today);

        LocalDate periodStart =
                currentMonth.atDay(1);

        LocalDate periodEnd =
                today;

        int totalDaysInMonth =
                currentMonth.lengthOfMonth();

        int daysElapsed =
                today.getDayOfMonth();

        int daysRemaining =
                totalDaysInMonth - daysElapsed;


        LocalDateTime startDateTime =
                periodStart.atStartOfDay();

        LocalDateTime endDateTime =
                periodEnd
                        .plusDays(1)
                        .atStartOfDay();


        // ========================================================
        // CURRENT-MONTH SPENDING
        // ========================================================

        BigDecimal spentSoFar =
                transactionRepository
                        .findCategorySpendingAmountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                budget.getCategory(),
                                startDateTime,
                                endDateTime
                        );

        if (spentSoFar == null) {

            spentSoFar =
                    BigDecimal.ZERO;
        }


        // ========================================================
        // TRANSACTION COUNT
        // ========================================================

        long transactionCount =
                transactionRepository
                        .findCategorySpendingCountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                budget.getCategory(),
                                startDateTime,
                                endDateTime
                        );


        // ========================================================
        // REMAINING BUDGET
        // ========================================================

        BigDecimal remainingBudget =
                budget.getMonthlyLimit()
                        .subtract(spentSoFar)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // AVERAGE DAILY SPENDING
        //
        // Keep extra precision here.
        // Only round the final values that are returned.
        // ========================================================

        BigDecimal averageDailySpend =
                spentSoFar
                        .divide(
                                BigDecimal.valueOf(
                                        daysElapsed
                                ),
                                10,
                                RoundingMode.HALF_UP
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // MORE ACCURATE MONTH-END PROJECTION
        //
        // Calculate using the unrounded average.
        // This prevents:
        //
        // ₹100 / 28 = ₹3.571428...
        //
        // from becoming:
        //
        // ₹3.57 × 30 = ₹107.10
        //
        // Instead:
        //
        // ₹3.571428... × 30 = ₹107.14
        // ========================================================

        BigDecimal preciseAverageDailySpend =
                spentSoFar
                        .divide(
                                BigDecimal.valueOf(
                                        daysElapsed
                                ),
                                10,
                                RoundingMode.HALF_UP
                        );

        BigDecimal projectedMonthEndSpend =
                preciseAverageDailySpend
                        .multiply(
                                BigDecimal.valueOf(
                                        totalDaysInMonth
                                )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // PROJECTED BUDGET USAGE
        // ========================================================

        BigDecimal projectedUsagePercentage =
                projectedMonthEndSpend
                        .multiply(
                                BigDecimal.valueOf(100)
                        )
                        .divide(
                                budget.getMonthlyLimit(),
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // DETERMINE FORECAST STATUS
        // ========================================================

        BudgetForecastResponse.ForecastStatus status;

        if (projectedUsagePercentage.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {

            status =
                    BudgetForecastResponse.ForecastStatus
                            .PROJECTED_OVER_BUDGET;

        } else if (projectedUsagePercentage.compareTo(
                BigDecimal.valueOf(80)
        ) >= 0) {

            status =
                    BudgetForecastResponse.ForecastStatus
                            .PROJECTED_NEAR_LIMIT;

        } else {

            status =
                    BudgetForecastResponse.ForecastStatus
                            .PROJECTED_WITHIN_BUDGET;
        }


        // ========================================================
        // RETURN FORECAST
        // ========================================================

        return new BudgetForecastResponse(

                budget.getCategory(),

                budget.getMonthlyLimit()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        ),

                spentSoFar
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        ),

                remainingBudget,

                averageDailySpend,

                projectedMonthEndSpend,

                projectedUsagePercentage,

                transactionCount,

                daysElapsed,

                daysRemaining,

                totalDaysInMonth,

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