package com.bankcore.ai;

import com.bankcore.dto.BudgetAwarenessResponse;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.service.BudgetAwarenessService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BudgetBankingTools {

    private final BudgetAwarenessService budgetAwarenessService;


    // ============================================================
    // GET ALL BUDGET AWARENESS
    // ============================================================

    @Tool(
            name = "getMyBudgetAwareness",
            description = """
                    Analyze the authenticated customer's current-month
                    spending against their configured category budgets.

                    Returns, for each configured budget:
                    - category
                    - monthly budget limit
                    - amount spent this month
                    - remaining amount
                    - percentage of budget used
                    - transaction count
                    - budget status

                    Budget statuses are:

                    WITHIN_BUDGET:
                    Less than 80% of the budget has been used.

                    NEAR_LIMIT:
                    80% to 100% of the budget has been used.

                    OVER_BUDGET:
                    More than 100% of the budget has been used.

                    IMPORTANT:
                    Use this tool when the customer asks whether they are
                    overspending, approaching a budget limit, or staying
                    within their budget.

                    Do not assume that high spending means overspending.
                    Actual overspending must be determined by comparing
                    spending against the customer's configured budget.

                    Examples:
                    "Am I overspending?"
                    "Am I overspending on food?"
                    "How much of my food budget have I used?"
                    "Which budgets am I close to exceeding?"
                    "How much budget do I have left?"
                    "Am I within my monthly budgets?"
                    """
    )
    public List<BudgetAwarenessResponse>
    getMyBudgetAwareness() {

        return budgetAwarenessService
                .getMyBudgetAwareness();
    }


    // ============================================================
    // GET BUDGET AWARENESS FOR ONE CATEGORY
    // ============================================================

    @Tool(
            name = "getMyCategoryBudgetAwareness",
            description = """
                    Analyze the authenticated customer's current-month
                    spending against their budget for one specific
                    transaction category.

                    Use this when the customer asks whether they are
                    overspending or approaching the budget for a specific
                    category such as FOOD, SHOPPING, TRANSPORT, BILLS,
                    ENTERTAINMENT, HEALTH or EDUCATION.

                    The result includes:
                    - monthly budget
                    - amount spent
                    - remaining amount
                    - percentage used
                    - transaction count
                    - budget status

                    Do not classify spending as overspending based only
                    on the amount spent. Compare it with the configured
                    budget.

                    Example:
                    "Am I overspending on food?"
                    "How much of my shopping budget is left?"
                    """
    )
    public BudgetAwarenessResponse
    getMyCategoryBudgetAwareness(
            TransactionCategory category
    ) {

        return budgetAwarenessService
                .getMyBudgetAwareness(category);
    }
}