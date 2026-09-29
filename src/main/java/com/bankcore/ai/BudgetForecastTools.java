package com.bankcore.ai;

import com.bankcore.dto.BudgetForecastResponse;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.service.BudgetForecastService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BudgetForecastTools {

    private final BudgetForecastService budgetForecastService;


    // ============================================================
    // FORECAST ALL BUDGETS
    // ============================================================

    @Tool(
            name = "getMyBudgetForecasts",
            description = """
                    Forecast the authenticated customer's current-month
                    spending against every configured category budget.

                    The backend calculates:

                    - monthly budget
                    - amount spent so far
                    - remaining budget
                    - average daily spending
                    - projected month-end spending
                    - projected budget usage percentage
                    - days elapsed
                    - days remaining
                    - transaction count
                    - projected budget status

                    Projected statuses:

                    PROJECTED_WITHIN_BUDGET:
                    Projected month-end spending is below 80% of the budget.

                    PROJECTED_NEAR_LIMIT:
                    Projected month-end spending is between 80% and 100%
                    of the budget.

                    PROJECTED_OVER_BUDGET:
                    Projected month-end spending is above 100% of the budget.

                    Use this tool when the customer asks:

                    "How much am I likely to spend this month?"
                    "Will I exceed my budgets?"
                    "Which budget am I likely to exceed?"
                    "What will my spending look like by month end?"
                    "Am I on track with my budgets?"
                    "Forecast my spending."
                    "What will my expenses be by the end of the month?"
                    "Which budgets are at risk?"

                    The backend is the source of truth.

                    Do not perform financial calculations yourself.

                    Do not estimate values that are not returned by the tool.
                    """
    )
    public List<BudgetForecastResponse> getMyBudgetForecasts() {

        return budgetForecastService
                .getMyBudgetForecasts();
    }


    // ============================================================
    // FORECAST ONE CATEGORY
    // ============================================================

    @Tool(
            name = "getMyCategoryBudgetForecast",
            description = """
                    Forecast the authenticated customer's current-month
                    spending for one specific budget category.

                    The backend returns:

                    - monthly budget
                    - amount spent so far
                    - remaining budget
                    - average daily spending
                    - projected month-end spending
                    - projected budget usage percentage
                    - days elapsed
                    - days remaining
                    - transaction count
                    - projected budget status

                    Use this tool when the customer asks about the
                    spending forecast for a specific category.

                    Examples:

                    "Will I exceed my food budget?"
                    "Will I go over my shopping budget?"
                    "How much am I likely to spend on food this month?"
                    "Am I on track with my food budget?"
                    "How much will I probably spend on transport this month?"
                    "Will I exceed my bills budget?"

                    The backend performs all calculations.

                    Do not estimate financial values yourself.

                    The category must be one of:

                    FOOD
                    SHOPPING
                    BILLS
                    TRANSPORT
                    ENTERTAINMENT
                    HEALTH
                    EDUCATION
                    TRANSFER
                    OTHER
                    """
    )
    public BudgetForecastResponse getMyCategoryBudgetForecast(
            TransactionCategory category
    ) {

        return budgetForecastService
                .getMyBudgetForecast(category);
    }
}