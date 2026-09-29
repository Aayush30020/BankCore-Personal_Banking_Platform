package com.bankcore.ai;

import com.bankcore.dto.RecurringExpenseResponse;
import com.bankcore.service.RecurringExpenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RecurringExpenseTools {

    private final RecurringExpenseService
            recurringExpenseService;


    // ============================================================
    // RECURRING EXPENSE DETECTION
    // ============================================================

    @Tool(
            name = "getMyRecurringExpenses",
            description = """
                    Detect recurring expenses in the authenticated
                    customer's completed outgoing transactions.

                    The backend looks for repeated transactions with
                    similar descriptions and consistent timing.

                    It currently detects:

                    WEEKLY:
                    Transactions occurring approximately every 7 days.

                    MONTHLY:
                    Transactions occurring approximately every 25 to 35 days.

                    The backend returns:

                    - description
                    - transaction category
                    - average transaction amount
                    - estimated monthly recurring amount
                    - number of occurrences
                    - average interval between transactions
                    - recurrence frequency
                    - first occurrence date
                    - most recent occurrence date

                    Use this tool when the customer asks:

                    "What recurring expenses do I have?"
                    "Which expenses repeat regularly?"
                    "What subscriptions do I have?"
                    "Show me my recurring payments."
                    "How much do my recurring expenses cost?"
                    "Which payments happen every month?"
                    "Which expenses are recurring?"
                    "What regular expenses do I have?"

                    Only backend-detected recurring transactions should
                    be described as recurring.

                    Do not invent subscriptions.

                    Do not claim that a recurring transaction is a
                    subscription unless the transaction description
                    supports that interpretation.

                    If no recurring expenses are detected, say so.
                    """
    )
    public List<RecurringExpenseResponse>
    getMyRecurringExpenses() {

        return recurringExpenseService
                .getMyRecurringExpenses();
    }
}