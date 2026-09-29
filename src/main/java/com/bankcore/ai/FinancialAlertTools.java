package com.bankcore.ai;

import com.bankcore.dto.FinancialAlertResponse;
import com.bankcore.service.FinancialAlertService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FinancialAlertTools {

    private final FinancialAlertService financialAlertService;


    // ============================================================
    // FINANCIAL ALERTS
    // ============================================================

    @Tool(
            name = "getMyFinancialAlerts",
            description = """
                    Analyze the authenticated customer's current financial
                    situation and return important financial alerts detected
                    by the BankCore backend.

                    The backend can detect:

                    1. BUDGET_EXCEEDED
                       Actual spending has exceeded the configured
                       category budget.

                    2. BUDGET_NEAR_LIMIT
                       Actual spending has reached at least 80 percent
                       of the configured category budget.

                    3. BUDGET_PROJECTED_OVER
                       Current spending behavior projects that the
                       category budget will be exceeded by month end.

                    4. SPENDING_INCREASE
                       Current month-to-date category spending is at least
                       25 percent higher than the same period of the
                       previous month.

                    5. RECURRING_EXPENSE
                       The backend has detected one or more recurring
                       expenses from the customer's transaction history.

                    Each alert can contain:

                    - alert type
                    - title
                    - message
                    - category
                    - amount
                    - reference amount
                    - percentage
                    - severity

                    Use this tool when the customer asks:

                    "Do I have any financial alerts?"
                    "Is there anything I should know about my spending?"
                    "Give me important financial warnings."
                    "Are there any problems with my spending?"
                    "What should I be aware of financially?"
                    "Do I have any budget warnings?"
                    "Is anything unusual about my spending?"
                    "Give me a financial alert summary."

                    IMPORTANT:

                    The backend is the source of truth.

                    Do not invent alerts.

                    Do not describe normal spending as a financial
                    problem unless the backend returns an alert.

                    Do not provide regulated financial advice.

                    If no alerts are returned, tell the customer that
                    no financial alerts were detected.
                    """
    )
    public List<FinancialAlertResponse>
    getMyFinancialAlerts() {

        return financialAlertService
                .getMyFinancialAlerts();
    }
}