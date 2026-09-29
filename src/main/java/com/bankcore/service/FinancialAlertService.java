package com.bankcore.service;

import com.bankcore.dto.FinancialAlertResponse;
import com.bankcore.dto.FinancialAlertResponse.AlertSeverity;
import com.bankcore.dto.FinancialAlertResponse.AlertType;
import com.bankcore.dto.RecurringExpenseResponse;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialAlertService {

    private static final BigDecimal NEAR_LIMIT_PERCENT =
            BigDecimal.valueOf(80);

    private static final BigDecimal SIGNIFICANT_INCREASE_PERCENT =
            BigDecimal.valueOf(25);

    private final BudgetRepository budgetRepository;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private final BudgetForecastService budgetForecastService;

    private final RecurringExpenseService recurringExpenseService;


    // ============================================================
    // GET ALL FINANCIAL ALERTS
    // ============================================================

    @Transactional(readOnly = true)
    public List<FinancialAlertResponse> getMyFinancialAlerts() {

        User user = getAuthenticatedUser();

        List<FinancialAlertResponse> alerts =
                new ArrayList<>();


        // --------------------------------------------------------
        // BUDGET ALERTS
        // --------------------------------------------------------

        alerts.addAll(
                getBudgetAlerts(user.getId())
        );


        // --------------------------------------------------------
        // SPENDING INCREASE ALERTS
        // --------------------------------------------------------

        alerts.addAll(
                getSpendingIncreaseAlerts(
                        user.getId()
                )
        );


        // --------------------------------------------------------
        // RECURRING EXPENSE ALERTS
        // --------------------------------------------------------

        alerts.addAll(
                getRecurringExpenseAlerts()
        );


        // --------------------------------------------------------
        // SORT BY SEVERITY
        // --------------------------------------------------------

        alerts.sort(
                Comparator.comparingInt(
                        alert ->
                                severityRank(
                                        alert.severity()
                                )
                )
        );


        return alerts;
    }


    // ============================================================
    // BUDGET ALERTS
    // ============================================================

    private List<FinancialAlertResponse> getBudgetAlerts(
            Long userId
    ) {

        List<FinancialAlertResponse> alerts =
                new ArrayList<>();


        List<Budget> budgets =
                budgetRepository
                        .findByUserIdOrderByCategoryAsc(
                                userId
                        );


        for (Budget budget : budgets) {

            TransactionCategory category =
                    budget.getCategory();


            BigDecimal spent =
                    getCurrentMonthSpending(
                            userId,
                            category
                    );


            BigDecimal limit =
                    budget.getMonthlyLimit();


            if (limit == null
                    || limit.compareTo(BigDecimal.ZERO) <= 0) {

                continue;
            }


            BigDecimal usagePercentage =
                    spent
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    limit,
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // ====================================================
            // BUDGET EXCEEDED
            // ====================================================

            if (spent.compareTo(limit) > 0) {

                alerts.add(
                        new FinancialAlertResponse(

                                AlertType.BUDGET_EXCEEDED,

                                category
                                        + " budget exceeded",

                                "You have exceeded your "
                                        + category
                                        + " budget this month.",

                                category,

                                spent,

                                limit,

                                usagePercentage,

                                AlertSeverity.CRITICAL
                        )
                );
            }


            // ====================================================
            // BUDGET NEAR LIMIT
            // ====================================================

            else if (usagePercentage.compareTo(
                    NEAR_LIMIT_PERCENT
            ) >= 0) {

                alerts.add(
                        new FinancialAlertResponse(

                                AlertType.BUDGET_NEAR_LIMIT,

                                category
                                        + " budget is near its limit",

                                "You have used "
                                        + usagePercentage
                                        + "% of your "
                                        + category
                                        + " budget this month.",

                                category,

                                spent,

                                limit,

                                usagePercentage,

                                AlertSeverity.WARNING
                        )
                );
            }


            // ====================================================
            // PROJECTED BUDGET ALERT
            // ====================================================

            try {

                var forecast =
                        budgetForecastService
                                .getMyBudgetForecast(
                                        category
                                );


                if (forecast != null
                        && forecast.projectedUsagePercentage()
                        != null
                        && forecast.projectedUsagePercentage()
                        .compareTo(
                                BigDecimal.valueOf(100)
                        ) > 0) {

                    /*
                     * Do not create a projected alert when the
                     * budget has already been exceeded.
                     */
                    if (spent.compareTo(limit) <= 0) {

                        alerts.add(
                                new FinancialAlertResponse(

                                        AlertType.BUDGET_PROJECTED_OVER,

                                        category
                                                + " budget is projected to be exceeded",

                                        "Your current spending pattern "
                                                + "projects that you may exceed "
                                                + "your "
                                                + category
                                                + " budget by month end.",

                                        category,

                                        forecast
                                                .projectedMonthEndSpend(),

                                        limit,

                                        forecast
                                                .projectedUsagePercentage(),

                                        AlertSeverity.WARNING
                                )
                        );
                    }
                }

            } catch (Exception ignored) {

                /*
                 * Forecast failure must never prevent the other
                 * financial alerts from being returned.
                 */
            }
        }


        return alerts;
    }


    // ============================================================
    // SPENDING INCREASE ALERTS
    // ============================================================

    private List<FinancialAlertResponse>
    getSpendingIncreaseAlerts(Long userId) {

        List<FinancialAlertResponse> alerts =
                new ArrayList<>();


        LocalDate today =
                LocalDate.now();


        YearMonth currentMonth =
                YearMonth.from(today);


        YearMonth previousMonth =
                currentMonth.minusMonths(1);


        LocalDate currentStart =
                currentMonth.atDay(1);


        int comparisonDay =
                Math.min(
                        today.getDayOfMonth(),
                        previousMonth.lengthOfMonth()
                );


        LocalDate previousEnd =
                previousMonth.atDay(
                        comparisonDay
                );


        LocalDateTime currentStartDateTime =
                currentStart.atStartOfDay();


        LocalDateTime currentEndDateTime =
                today
                        .plusDays(1)
                        .atStartOfDay();


        LocalDateTime previousStartDateTime =
                previousMonth
                        .atDay(1)
                        .atStartOfDay();


        LocalDateTime previousEndDateTime =
                previousEnd
                        .plusDays(1)
                        .atStartOfDay();


        for (TransactionCategory category :
                TransactionCategory.values()) {

            BigDecimal currentAmount =
                    transactionRepository
                            .findCategorySpendingAmountBetweenDates(
                                    userId,
                                    TransactionStatus.COMPLETED,
                                    category,
                                    currentStartDateTime,
                                    currentEndDateTime
                            );


            if (currentAmount == null) {
                currentAmount = BigDecimal.ZERO;
            }


            BigDecimal previousAmount =
                    transactionRepository
                            .findCategorySpendingAmountBetweenDates(
                                    userId,
                                    TransactionStatus.COMPLETED,
                                    category,
                                    previousStartDateTime,
                                    previousEndDateTime
                            );


            if (previousAmount == null) {
                previousAmount = BigDecimal.ZERO;
            }


            /*
             * A zero previous-month baseline cannot produce a
             * meaningful percentage increase.
             */
            if (previousAmount.compareTo(
                    BigDecimal.ZERO
            ) == 0) {

                continue;
            }


            if (currentAmount.compareTo(
                    previousAmount
            ) <= 0) {

                continue;
            }


            BigDecimal increasePercentage =
                    currentAmount
                            .subtract(previousAmount)
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    previousAmount,
                                    2,
                                    RoundingMode.HALF_UP
                            );


            if (increasePercentage.compareTo(
                    SIGNIFICANT_INCREASE_PERCENT
            ) >= 0) {

                alerts.add(
                        new FinancialAlertResponse(

                                AlertType.SPENDING_INCREASE,

                                category
                                        + " spending increased",

                                "Your "
                                        + category
                                        + " spending is "
                                        + increasePercentage
                                        + "% higher than the same period "
                                        + "last month.",

                                category,

                                currentAmount,

                                previousAmount,

                                increasePercentage,

                                AlertSeverity.WARNING
                        )
                );
            }
        }


        return alerts;
    }


    // ============================================================
    // RECURRING EXPENSE ALERT
    // ============================================================

    private List<FinancialAlertResponse>
    getRecurringExpenseAlerts() {

        List<RecurringExpenseResponse> recurringExpenses =
                recurringExpenseService
                        .getMyRecurringExpenses();


        if (recurringExpenses.isEmpty()) {

            return List.of();
        }


        BigDecimal totalEstimatedMonthly =
                recurringExpenses
                        .stream()
                        .map(
                                RecurringExpenseResponse
                                        ::estimatedMonthlyAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        String message =
                recurringExpenses.size()
                        + " recurring expense"
                        + (
                        recurringExpenses.size() == 1
                                ? " was"
                                : "s were"
                )
                        + " detected, with an estimated monthly "
                        + "cost of ₹"
                        + totalEstimatedMonthly
                        + ".";


        FinancialAlertResponse alert =
                new FinancialAlertResponse(

                        AlertType.RECURRING_EXPENSE,

                        "Recurring expenses detected",

                        message,

                        null,

                        totalEstimatedMonthly,

                        null,

                        null,

                        AlertSeverity.INFO
                );


        return List.of(alert);
    }


    // ============================================================
    // CURRENT MONTH CATEGORY SPENDING
    // ============================================================

    private BigDecimal getCurrentMonthSpending(
            Long userId,
            TransactionCategory category
    ) {

        LocalDate today =
                LocalDate.now();


        YearMonth currentMonth =
                YearMonth.from(today);


        LocalDateTime startDate =
                currentMonth
                        .atDay(1)
                        .atStartOfDay();


        LocalDateTime endDate =
                today
                        .plusDays(1)
                        .atStartOfDay();


        BigDecimal amount =
                transactionRepository
                        .findCategorySpendingAmountBetweenDates(
                                userId,
                                TransactionStatus.COMPLETED,
                                category,
                                startDate,
                                endDate
                        );


        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }


    // ============================================================
    // SEVERITY RANK
    // ============================================================

    private int severityRank(
            AlertSeverity severity
    ) {

        return switch (severity) {

            case CRITICAL -> 1;

            case WARNING -> 2;

            case INFO -> 3;
        };
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