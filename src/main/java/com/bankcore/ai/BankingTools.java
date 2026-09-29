package com.bankcore.ai;

import com.bankcore.dto.AccountResponse;
import com.bankcore.dto.BudgetAnalysisResponse;
import com.bankcore.dto.CategorySpendingComparison;
import com.bankcore.dto.CategorySpendingPeriodResponse;
import com.bankcore.dto.CategorySpendingResponse;
import com.bankcore.dto.FinancialHealthSummaryResponse;
import com.bankcore.dto.FinancialInsightsResponse;
import com.bankcore.dto.MonthlySpendingAnalysisResponse;
import com.bankcore.dto.SpendingAnalysisResponse;
import com.bankcore.dto.SpendingComparisonResponse;
import com.bankcore.dto.SavingsGoalAnalysisResponse;
import com.bankcore.dto.SavingsGoalResponse;
import com.bankcore.dto.TransactionHistoryResponse;

import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.TransactionDirection;

import com.bankcore.service.AccountService;
import com.bankcore.service.BudgetAnalysisService;
import com.bankcore.service.CategorySpendingPeriodService;
import com.bankcore.service.CategorySpendingService;
import com.bankcore.service.FinancialInsightsService;
import com.bankcore.service.MonthlySpendingAnalysisService;
import com.bankcore.service.MonthlySpendingReportService;
import com.bankcore.service.SavingsGoalService;
import com.bankcore.service.SpendingAnalysisService;
import com.bankcore.service.SpendingComparisonService;
import com.bankcore.service.SpendingInsightsService;
import com.bankcore.service.TransactionQueryService;
import com.bankcore.service.TransactionSearchService;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BankingTools {

    private final AccountService accountService;

    private final TransactionQueryService transactionQueryService;

    private final FinancialInsightsService financialInsightsService;

    private final TransactionSearchService transactionSearchService;

    private final SpendingAnalysisService spendingAnalysisService;

    private final MonthlySpendingAnalysisService
            monthlySpendingAnalysisService;

    private final SpendingComparisonService spendingComparisonService;

    private final CategorySpendingService categorySpendingService;

    private final CategorySpendingPeriodService
            categorySpendingPeriodService;

    private final SpendingInsightsService spendingInsightsService;

    private final MonthlySpendingReportService
            monthlySpendingReportService;

    private final SavingsGoalService savingsGoalService;

    private final BudgetAnalysisService budgetAnalysisService;


    // ============================================================
    // TOOL 1: GET MY ACCOUNTS
    // ============================================================

    @Tool(
            name = "getMyAccounts",
            description = """
            Get the authenticated customer's bank accounts.

            Use this tool when the customer asks about:
            - bank accounts
            - account numbers
            - account balances
            - available accounts
            - how much money they have

            Only return information belonging to the
            authenticated customer.
            """
    )
    public List<AccountResponse> getMyAccounts() {

        return accountService.getMyAccounts();
    }


    // ============================================================
    // TOOL 2: GET MY RECENT TRANSACTIONS
    // ============================================================

    @Tool(
            name = "getMyRecentTransactions",
            description = """
            Get the authenticated customer's five most recent
            completed banking transactions.

            Use this tool for:
            - recent transactions
            - latest transaction
            - last transaction
            - recent transfers
            - transaction history

            Only return transactions belonging to the
            authenticated customer.
            """
    )
    public List<TransactionHistoryResponse>
    getMyRecentTransactions() {

        return transactionQueryService
                .getMyRecentTransactions(5);
    }


    // ============================================================
    // TOOL 3: FINANCIAL INSIGHTS
    // ============================================================

    @Tool(
            name = "getMyFinancialInsights",
            description = """
            Get financial transaction insights for the
            authenticated customer.

            Returns:
            - total money sent
            - total money received
            - net money flow
            - total transaction volume
            - sent transaction count
            - received transaction count

            All calculations are performed by the backend.
            """
    )
    public FinancialInsightsResponse
    getMyFinancialInsights() {

        return financialInsightsService
                .getMyFinancialInsights();
    }


    // ============================================================
    // TOOL 4: SEARCH MY TRANSACTIONS
    // ============================================================

    @Tool(
            name = "searchMyTransactions",
            description = """
            Search the authenticated customer's completed
            transactions using optional filters.

            Available filters:

            direction:
            SENT or RECEIVED

            minAmount:
            Minimum transaction amount in INR.

            maxAmount:
            Maximum transaction amount in INR.

            counterparty:
            Name of the other customer.

            Only the authenticated customer's transactions
            are searched.
            """
    )
    public List<TransactionHistoryResponse>
    searchMyTransactions(

            @ToolParam(
                    description = """
                    Transaction direction.

                    Must be SENT or RECEIVED.

                    Omit when no direction is specified.
                    """,
                    required = false
            )
            TransactionDirection direction,

            @ToolParam(
                    description = """
                    Minimum transaction amount in INR.

                    Omit when no minimum is specified.
                    """,
                    required = false
            )
            BigDecimal minAmount,

            @ToolParam(
                    description = """
                    Maximum transaction amount in INR.

                    Omit when no maximum is specified.
                    """,
                    required = false
            )
            BigDecimal maxAmount,

            @ToolParam(
                    description = """
                    Name of the other customer involved
                    in the transaction.

                    Omit when no counterparty is specified.
                    """,
                    required = false
            )
            String counterparty

    ) {

        return transactionSearchService
                .searchMyTransactions(
                        direction,
                        minAmount,
                        maxAmount,
                        counterparty
                );
    }


    // ============================================================
    // TOOL 5: OVERALL SPENDING ANALYSIS
    // ============================================================

    @Tool(
            name = "getMySpendingAnalysis",
            description = """
            Analyze all completed outgoing transactions
            belonging to the authenticated customer.

            Returns:
            - total amount spent
            - transaction count
            - average transaction
            - largest transaction
            - smallest transaction
            - net spending

            Use this for overall spending history.

            The backend performs all financial calculations.
            """
    )
    public SpendingAnalysisResponse
    getMySpendingAnalysis() {

        return spendingAnalysisService
                .getSpendingAnalysis();
    }


    // ============================================================
    // TOOL 6: MONTHLY / DATE-RANGE SPENDING ANALYSIS
    // ============================================================

    @Tool(
            name = "getMyMonthlySpendingAnalysis",
            description = """
            Analyze completed outgoing spending within
            a specific date range.

            Both dates are required.

            Dates must use YYYY-MM-DD format.

            The end date is inclusive.

            Use this for:
            - monthly spending
            - spending between two dates
            - spending in a specific period

            Backend performs all financial calculations.
            """
    )
    public MonthlySpendingAnalysisResponse
    getMyMonthlySpendingAnalysis(

            @ToolParam(
                    description = """
                    Inclusive start date.

                    Format: YYYY-MM-DD.
                    """
            )
            String startDate,

            @ToolParam(
                    description = """
                    Inclusive end date.

                    Format: YYYY-MM-DD.
                    """
            )
            String endDate

    ) {

        LocalDate parsedStartDate =
                parseDate(
                        startDate,
                        "startDate"
                );

        LocalDate parsedEndDate =
                parseDate(
                        endDate,
                        "endDate"
                );

        return monthlySpendingAnalysisService
                .getSpendingAnalysis(
                        parsedStartDate,
                        parsedEndDate
                );
    }


    // ============================================================
    // TOOL 7: SPENDING COMPARISON
    // ============================================================

    @Tool(
            name = "getMySpendingComparison",
            description = """
            Compare the authenticated customer's completed
            outgoing spending between two date ranges.

            Use this for questions such as:

            "Compare my spending this month with last month."

            Returns:
            - current period spending
            - current transaction count
            - previous period spending
            - previous transaction count
            - amount difference
            - percentage change when possible

            Backend performs all calculations.
            """
    )
    public SpendingComparisonResponse
    getMySpendingComparison(

            @ToolParam(
                    description = """
                    Inclusive start date of current period.

                    Format: YYYY-MM-DD.
                    """
            )
            String currentPeriodStart,

            @ToolParam(
                    description = """
                    Inclusive end date of current period.

                    Format: YYYY-MM-DD.
                    """
            )
            String currentPeriodEnd,

            @ToolParam(
                    description = """
                    Inclusive start date of previous period.

                    Format: YYYY-MM-DD.
                    """
            )
            String previousPeriodStart,

            @ToolParam(
                    description = """
                    Inclusive end date of previous period.

                    Format: YYYY-MM-DD.
                    """
            )
            String previousPeriodEnd

    ) {

        LocalDate parsedCurrentStart =
                parseDate(
                        currentPeriodStart,
                        "currentPeriodStart"
                );

        LocalDate parsedCurrentEnd =
                parseDate(
                        currentPeriodEnd,
                        "currentPeriodEnd"
                );

        LocalDate parsedPreviousStart =
                parseDate(
                        previousPeriodStart,
                        "previousPeriodStart"
                );

        LocalDate parsedPreviousEnd =
                parseDate(
                        previousPeriodEnd,
                        "previousPeriodEnd"
                );

        return spendingComparisonService
                .compareSpending(
                        parsedCurrentStart,
                        parsedCurrentEnd,
                        parsedPreviousStart,
                        parsedPreviousEnd
                );
    }


    // ============================================================
    // TOOL 8: FINANCIAL HEALTH SUMMARY
    // ============================================================

    @Tool(
            name = "getMyFinancialHealthSummary",
            description = """
            Generate a comprehensive financial snapshot
            for the authenticated customer.

            Includes:
            - account balances
            - money sent and received
            - net flow
            - overall spending
            - current month spending
            - previous month spending
            - monthly spending comparison

            Backend performs all financial calculations.

            This tool provides financial information and
            analysis, not regulated financial advice.
            """
    )
    public FinancialHealthSummaryResponse
    getMyFinancialHealthSummary() {

        LocalDate today =
                LocalDate.now();

        LocalDate currentMonthStart =
                today.withDayOfMonth(1);

        LocalDate currentMonthEnd =
                today;

        LocalDate previousMonthStart =
                currentMonthStart.minusMonths(1);

        LocalDate previousMonthEnd =
                currentMonthStart.minusDays(1);

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

        FinancialInsightsResponse financialInsights =
                financialInsightsService
                        .getMyFinancialInsights();

        SpendingAnalysisResponse overallSpending =
                spendingAnalysisService
                        .getSpendingAnalysis();

        MonthlySpendingAnalysisResponse
                currentMonthAnalysis =
                monthlySpendingAnalysisService
                        .getSpendingAnalysis(
                                currentMonthStart,
                                currentMonthEnd
                        );

        FinancialHealthSummaryResponse.PeriodSpending
                currentMonth =
                new FinancialHealthSummaryResponse.PeriodSpending(
                        currentMonthStart,
                        currentMonthEnd,
                        currentMonthAnalysis
                );

        MonthlySpendingAnalysisResponse
                previousMonthAnalysis =
                monthlySpendingAnalysisService
                        .getSpendingAnalysis(
                                previousMonthStart,
                                previousMonthEnd
                        );

        FinancialHealthSummaryResponse.PeriodSpending
                previousMonth =
                new FinancialHealthSummaryResponse.PeriodSpending(
                        previousMonthStart,
                        previousMonthEnd,
                        previousMonthAnalysis
                );

        SpendingComparisonResponse comparison =
                spendingComparisonService
                        .compareSpending(
                                currentMonthStart,
                                currentMonthEnd,
                                previousMonthStart,
                                previousMonthEnd
                        );

        return new FinancialHealthSummaryResponse(
                accountSummary,
                financialInsights,
                overallSpending,
                currentMonth,
                previousMonth,
                comparison
        );
    }


    // ============================================================
    // TOOL 9: CATEGORY-WISE SPENDING
    // ============================================================

    @Tool(
            name = "getMyCategorySpending",
            description = """
            Get a category-wise breakdown of the
            authenticated customer's completed outgoing spending.

            Categories:
            FOOD
            SHOPPING
            BILLS
            TRANSPORT
            ENTERTAINMENT
            HEALTH
            EDUCATION
            TRANSFER
            OTHER

            Backend performs all calculations.

            Uncategorized transactions are excluded.
            """
    )
    public CategorySpendingResponse
    getMyCategorySpending() {

        return categorySpendingService
                .getMyCategorySpending();
    }


    // ============================================================
    // TOOL 10: SPENDING BY CATEGORY
    // ============================================================

    @Tool(
            name = "getMySpendingByCategory",
            description = """
            Get the authenticated customer's total completed
            outgoing spending for one category.

            Available categories:
            FOOD
            SHOPPING
            BILLS
            TRANSPORT
            ENTERTAINMENT
            HEALTH
            EDUCATION
            TRANSFER
            OTHER

            Backend performs the calculation.
            """
    )
    public BigDecimal getMySpendingByCategory(

            @ToolParam(
                    description = """
                    Spending category.

                    Must be one of:
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
            TransactionCategory category

    ) {

        return categorySpendingService
                .getMySpendingByCategory(
                        category
                );
    }


    // ============================================================
    // TOOL 11: CATEGORY SPENDING FOR DATE RANGE
    // ============================================================

    @Tool(
            name = "getMyCategorySpendingForPeriod",
            description = """
            Get spending for one transaction category
            within a specific date range.

            Categories:
            FOOD
            SHOPPING
            BILLS
            TRANSPORT
            ENTERTAINMENT
            HEALTH
            EDUCATION
            TRANSFER
            OTHER

            Dates must use YYYY-MM-DD format.

            End date is inclusive.

            Uncategorized transactions are excluded.
            """
    )
    public CategorySpendingPeriodResponse
    getMyCategorySpendingForPeriod(

            @ToolParam(
                    description = """
                    Transaction category.

                    Must be one of the supported
                    TransactionCategory values.
                    """
            )
            TransactionCategory category,

            @ToolParam(
                    description = """
                    Inclusive start date.

                    Format: YYYY-MM-DD.
                    """
            )
            String startDate,

            @ToolParam(
                    description = """
                    Inclusive end date.

                    Format: YYYY-MM-DD.
                    """
            )
            String endDate

    ) {

        LocalDate parsedStartDate =
                parseDate(
                        startDate,
                        "startDate"
                );

        LocalDate parsedEndDate =
                parseDate(
                        endDate,
                        "endDate"
                );

        return categorySpendingPeriodService
                .getSpending(
                        category,
                        parsedStartDate,
                        parsedEndDate
                );
    }


    // ============================================================
    // TOOL 12: SPENDING INSIGHTS
    // ============================================================

    @Tool(
            name = "getMySpendingInsights",
            description = """
            Analyze the authenticated customer's current-month
            spending behavior.

            Provides:
            - current month spending
            - transaction count
            - category spending
            - highest spending category
            - spending concentration

            Use this when the customer asks:

            "What am I spending the most on?"
            "Where is most of my money going?"
            "Which category takes most of my money?"

            IMPORTANT:

            High spending is not automatically overspending.

            Budget status must be obtained from the
            budget analysis tool.

            Backend performs all calculations.
            """
    )
    public SpendingInsightsService.SpendingInsightsData
    getMySpendingInsights() {

        return spendingInsightsService
                .getCurrentMonthInsights();
    }


    // ============================================================
    // TOOL 13: MONTHLY AI SPENDING REPORT
    // ============================================================

    @Tool(
            name = "getMyMonthlySpendingReport",
            description = """
            Compare authenticated customer spending by category
            between the current calendar month and previous
            calendar month.

            Returns:
            - current month amount
            - current transaction count
            - previous month amount
            - previous transaction count
            - amount difference
            - percentage change when possible

            Use for category-level month-over-month analysis.

            Backend performs all calculations.
            """
    )
    public List<CategorySpendingComparison>
    getMyMonthlySpendingReport() {

        return monthlySpendingReportService
                .getMonthlyReport();
    }


    // ============================================================
    // TOOL 14: BUDGET ANALYSIS
    // ============================================================

    @Tool(
            name = "getMyBudgetAnalysis",
            description = """
            Analyze the authenticated customer's configured
            monthly budgets against their actual current-month
            spending.

            Returns for each configured budget:

            - category
            - monthly budget limit
            - current-month spending
            - remaining budget
            - usage percentage
            - transaction count
            - budget status
            - currency

            Budget statuses include:

            ON_TRACK
            NEAR_LIMIT
            AT_LIMIT
            EXCEEDED

            Use this tool when the customer asks:

            "How are my budgets doing?"
            "Show my budget status."
            "How much of my food budget have I used?"
            "How much budget do I have left?"
            "Am I over my food budget?"
            "Have I exceeded my budget?"
            "Which budgets are near their limits?"
            "Am I overspending on food?"
            "How much can I still spend?"

            IMPORTANT:

            This tool is the source of truth for budget status.

            Do not infer budget status from spending insights.

            A high spending amount is not automatically
            budget overspending.

            Only a configured budget can determine whether
            spending exceeds that budget.

            Only access budgets belonging to the
            authenticated customer.

            Backend performs all financial calculations.
            """
    )
    public List<BudgetAnalysisResponse>
    getMyBudgetAnalysis() {

        return budgetAnalysisService
                .getMyBudgetAnalysis();
    }


    // ============================================================
    // TOOL 15: GET MY SAVINGS GOALS
    // ============================================================

    @Tool(
            name = "getMySavingsGoals",
            description = """
            Get the authenticated customer's savings goals.

            Returns:
            - goal name
            - target amount
            - current saved amount
            - remaining amount
            - progress percentage
            - target date
            - currency

            Only return savings goals belonging to the
            authenticated customer.
            """
    )
    public List<SavingsGoalResponse>
    getMySavingsGoals() {

        return savingsGoalService
                .getMyGoals();
    }


    // ============================================================
    // TOOL 16: ANALYZE SAVINGS GOAL
    // ============================================================

    @Tool(
            name = "getMySavingsGoalAnalysis",
            description = """
            Analyze one specific savings goal belonging to
            the authenticated customer.

            Returns:
            - target amount
            - current saved amount
            - remaining amount
            - progress percentage
            - target date
            - days remaining
            - months remaining
            - required daily saving
            - required monthly saving
            - goal status

            Backend performs all calculations.

            The goal name must be provided.
            """
    )
    public SavingsGoalAnalysisResponse
    getMySavingsGoalAnalysis(

            @ToolParam(
                    description = """
                    Exact name of the savings goal.

                    Example:
                    Emergency Fund
                    """
            )
            String name

    ) {

        if (name == null || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Savings goal name is required"
            );
        }

        return savingsGoalService
                .analyzeGoal(
                        name.trim()
                );
    }


    // ============================================================
    // DATE PARSER
    // ============================================================

    private LocalDate parseDate(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        try {

            return LocalDate.parse(
                    value.trim()
            );

        } catch (DateTimeParseException exception) {

            throw new IllegalArgumentException(
                    fieldName
                            + " must use YYYY-MM-DD format"
            );
        }
    }
}