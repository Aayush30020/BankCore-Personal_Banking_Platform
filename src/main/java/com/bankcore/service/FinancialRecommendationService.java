package com.bankcore.service;

import com.bankcore.dto.CategorySpendingResponse;
import com.bankcore.dto.FinancialAlertResponse;
import com.bankcore.dto.FinancialRecommendationResponse;
import com.bankcore.dto.FinancialRecommendationResponse.Priority;
import com.bankcore.dto.SavingsGoalResponse;
import com.bankcore.entity.TransactionCategory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialRecommendationService {

    private final BudgetAnalysisService budgetAnalysisService;

    private final FinancialAlertService financialAlertService;

    private final CategorySpendingService categorySpendingService;

    private final SavingsGoalService savingsGoalService;

    private final RecurringExpenseService recurringExpenseService;


    // ============================================================
    // GENERATE FINANCIAL RECOMMENDATIONS
    // ============================================================

    @Transactional(readOnly = true)
    public FinancialRecommendationResponse
    getMyFinancialRecommendations() {

        List<FinancialRecommendationResponse.Recommendation>
                recommendations =
                new ArrayList<>();


        // ========================================================
        // 1. CATEGORY SPENDING
        // ========================================================

        CategorySpendingResponse spending =
                categorySpendingService
                        .getMyCategorySpending();


        BigDecimal currentMonthSpending =
                spending.totalSpending();


        // ========================================================
        // 2. FINANCIAL ALERTS
        // ========================================================

        List<FinancialAlertResponse> alerts =
                financialAlertService
                        .getMyFinancialAlerts();


        int financialAlertCount =
                alerts.size();


        int criticalAlertCount =
                (int) alerts.stream()
                        .filter(alert ->
                                alert.severity()
                                        == FinancialAlertResponse
                                        .AlertSeverity
                                        .CRITICAL
                        )
                        .count();


        int warningAlertCount =
                (int) alerts.stream()
                        .filter(alert ->
                                alert.severity()
                                        == FinancialAlertResponse
                                        .AlertSeverity
                                        .WARNING
                        )
                        .count();


        // ========================================================
        // 3. BUDGET ANALYSIS
        // ========================================================

        var budgetAnalysis =
                budgetAnalysisService
                        .getMyBudgetAnalysis();


        // --------------------------------------------------------
        // EXCEEDED BUDGETS
        // --------------------------------------------------------

        budgetAnalysis.stream()
                .filter(budget ->
                        "EXCEEDED".equals(
                                budget.status()
                        )
                )
                .forEach(budget -> {

                    recommendations.add(
                            new FinancialRecommendationResponse
                                    .Recommendation(

                                    Priority.HIGH,

                                    budget.category()
                                            + " budget exceeded",

                                    "Your "
                                            + budget.category()
                                            + " spending is above "
                                            + "your monthly budget by "
                                            + formatAmount(
                                            budget.currentMonthSpending()
                                                    .subtract(
                                                            budget.monthlyLimit()
                                                    )
                                    )
                                            + ". Review spending in "
                                            + "this category and consider "
                                            + "reducing non-essential "
                                            + "expenses."
                            )
                    );
                });


        // --------------------------------------------------------
        // HIGH USAGE BUDGETS
        // --------------------------------------------------------

        budgetAnalysis.stream()
                .filter(budget ->
                        "NEAR_LIMIT".equals(
                                budget.status()
                        )
                                ||
                                "HIGH_USAGE".equals(
                                        budget.status()
                                )
                )
                .forEach(budget -> {

                    recommendations.add(
                            new FinancialRecommendationResponse
                                    .Recommendation(

                                    Priority.MEDIUM,

                                    budget.category()
                                            + " budget needs attention",

                                    "You have used "
                                            + budget.usagePercentage()
                                            + "% of your "
                                            + budget.category()
                                            + " budget. "
                                            + formatAmount(
                                            budget.remainingAmount()
                                    )
                                            + " remains for the rest "
                                            + "of the month."
                            )
                    );
                });


        // ========================================================
        // 4. TOP SPENDING CATEGORY
        // ========================================================

        CategorySpendingResponse.CategoryBreakdown
                topCategory =
                spending.categories()
                        .stream()
                        .findFirst()
                        .orElse(null);


        if (topCategory != null
                && currentMonthSpending
                .compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal topCategoryPercentage =
                    topCategory.amount()
                            .multiply(
                                    BigDecimal.valueOf(100)
                            )
                            .divide(
                                    currentMonthSpending,
                                    2,
                                    java.math.RoundingMode.HALF_UP
                            );


            recommendations.add(
                    new FinancialRecommendationResponse
                            .Recommendation(

                            Priority.INFO,

                            "Monitor your highest spending category",

                            topCategory.category()
                                    + " is currently your largest "
                                    + "spending category at "
                                    + formatAmount(
                                    topCategory.amount()
                            )
                                    + ", representing "
                                    + topCategoryPercentage
                                    + "% of your categorized "
                                    + "spending this month."
                    )
            );
        }


        // ========================================================
        // 5. RECURRING EXPENSES
        // ========================================================

        var recurringExpenses =
                recurringExpenseService
                        .getMyRecurringExpenses();


        BigDecimal recurringMonthlyCost =
                recurringExpenses
                        .stream()
                        .map(
                                com.bankcore.dto
                                        .RecurringExpenseResponse
                                        ::estimatedMonthlyAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        if (!recurringExpenses.isEmpty()) {

            recommendations.add(
                    new FinancialRecommendationResponse
                            .Recommendation(

                            Priority.MEDIUM,

                            "Review recurring expenses",

                            "You have "
                                    + recurringExpenses.size()
                                    + " recurring expense"
                                    + (
                                    recurringExpenses.size() == 1
                                            ? ""
                                            : "s"
                            )
                                    + " with an estimated monthly "
                                    + "cost of "
                                    + formatAmount(
                                    recurringMonthlyCost
                            )
                                    + ". Review these expenses "
                                    + "regularly and consider "
                                    + "removing subscriptions or "
                                    + "services you no longer use."
                    )
            );
        }


        // ========================================================
        // 6. SAVINGS GOALS
        // ========================================================

        List<SavingsGoalResponse> savingsGoals =
                savingsGoalService
                        .getMyGoals();


        int savingsGoalCount =
                savingsGoals.size();


        if (savingsGoalCount > 0) {

            recommendations.add(
                    new FinancialRecommendationResponse
                            .Recommendation(

                            Priority.INFO,

                            "Keep your savings goals active",

                            "You currently have "
                                    + savingsGoalCount
                                    + " configured savings goal"
                                    + (
                                    savingsGoalCount == 1
                                            ? ""
                                            : "s"
                            )
                                    + ". Keep your planned savings "
                                    + "contributions consistent and "
                                    + "review progress against each "
                                    + "target date."
                    )
            );

        } else {

            recommendations.add(
                    new FinancialRecommendationResponse
                            .Recommendation(

                            Priority.LOW,

                            "Consider setting a savings goal",

                            "You currently do not have any "
                                    + "configured savings goals. "
                                    + "A specific target can help "
                                    + "you track progress toward "
                                    + "a financial objective."
                    )
            );
        }


        // ========================================================
        // 7. GENERAL POSITIVE STATUS
        // ========================================================

        if (recommendations.isEmpty()) {

            recommendations.add(
                    new FinancialRecommendationResponse
                            .Recommendation(

                            Priority.INFO,

                            "No immediate action required",

                            "Your current financial data does "
                                    + "not show any budget alerts "
                                    + "or other conditions requiring "
                                    + "immediate attention."
                    )
            );
        }


        // ========================================================
        // RETURN RESPONSE
        // ========================================================

        return new FinancialRecommendationResponse(

                currentMonthSpending,

                financialAlertCount,

                criticalAlertCount,

                warningAlertCount,

                recurringMonthlyCost,

                savingsGoalCount,

                recommendations
        );
    }


    // ============================================================
    // FORMAT AMOUNT
    // ============================================================

    private String formatAmount(
            BigDecimal amount
    ) {

        if (amount == null) {

            return "₹0";
        }

        return "₹" +
                amount.stripTrailingZeros()
                        .toPlainString();
    }
}