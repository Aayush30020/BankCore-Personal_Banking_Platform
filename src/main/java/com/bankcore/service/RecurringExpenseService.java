package com.bankcore.service;

import com.bankcore.dto.RecurringExpenseResponse;
import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.TransactionStatus;
import com.bankcore.entity.User;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    // ============================================================
    // GET MY RECURRING EXPENSES
    // ============================================================

    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse>
    getMyRecurringExpenses() {

        User user = getAuthenticatedUser();

        List<Transaction> transactions =
                transactionRepository
                        .findRecentTransactionsByUserId(
                                user.getId()
                        );

        /*
         * Only completed outgoing transactions are relevant.
         *
         * Incoming transactions are ignored because this feature
         * detects recurring expenses.
         */
        List<Transaction> outgoingTransactions =
                transactions
                        .stream()
                        .filter(transaction ->
                                transaction.getStatus()
                                        == TransactionStatus.COMPLETED
                        )
                        .filter(transaction ->
                                transaction.getFromAccount()
                                        .getUser()
                                        .getId()
                                        .equals(user.getId())
                        )
                        .filter(transaction ->
                                transaction.getDescription() != null
                                        && !transaction
                                        .getDescription()
                                        .isBlank()
                        )
                        .toList();


        /*
         * Group transactions by:
         *
         * category + normalized description
         *
         * Amount is intentionally NOT part of the key because
         * recurring bills can vary slightly from month to month.
         */
        Map<String, List<Transaction>> groupedTransactions =
                new LinkedHashMap<>();


        for (Transaction transaction :
                outgoingTransactions) {

            String key =
                    buildGroupingKey(transaction);

            groupedTransactions
                    .computeIfAbsent(
                            key,
                            ignored -> new ArrayList<>()
                    )
                    .add(transaction);
        }


        List<RecurringExpenseResponse> recurringExpenses =
                new ArrayList<>();


        for (List<Transaction> group :
                groupedTransactions.values()) {

            if (group.size() < 2) {
                continue;
            }

            group.sort(
                    Comparator.comparing(
                            Transaction::getCreatedAt
                    )
            );


            RecurringExpenseResponse response =
                    analyzeGroup(group);

            if (response != null) {

                recurringExpenses.add(response);
            }
        }


        /*
         * Highest estimated monthly recurring expenses first.
         */
        recurringExpenses.sort(
                Comparator.comparing(
                                RecurringExpenseResponse
                                        ::estimatedMonthlyAmount
                        )
                        .reversed()
        );


        return recurringExpenses;
    }


    // ============================================================
    // ANALYZE ONE GROUP
    // ============================================================

    private RecurringExpenseResponse analyzeGroup(
            List<Transaction> transactions
    ) {

        if (transactions.size() < 2) {
            return null;
        }


        List<Long> intervals =
                new ArrayList<>();


        for (int i = 1;
             i < transactions.size();
             i++) {

            LocalDateTime previous =
                    transactions
                            .get(i - 1)
                            .getCreatedAt();

            LocalDateTime current =
                    transactions
                            .get(i)
                            .getCreatedAt();

            long days =
                    Duration.between(
                            previous,
                            current
                    ).toDays();

            if (days > 0) {
                intervals.add(days);
            }
        }


        if (intervals.isEmpty()) {
            return null;
        }


        double averageIntervalDays =
                intervals
                        .stream()
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0);


        /*
         * Determine whether the timing actually looks recurring.
         *
         * Weekly:
         * approximately 7 days apart.
         *
         * Monthly:
         * approximately 30 days apart.
         */
        RecurringExpenseResponse.RecurrenceFrequency frequency =
                determineFrequency(
                        intervals,
                        averageIntervalDays
                );


        if (frequency
                == RecurringExpenseResponse
                .RecurrenceFrequency.UNKNOWN) {

            return null;
        }


        BigDecimal averageAmount =
                transactions
                        .stream()
                        .map(Transaction::getAmount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .divide(
                                BigDecimal.valueOf(
                                        transactions.size()
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );


        BigDecimal estimatedMonthlyAmount;


        if (frequency
                == RecurringExpenseResponse
                .RecurrenceFrequency.WEEKLY) {

            /*
             * Approximately 52 weeks per year / 12 months.
             */
            estimatedMonthlyAmount =
                    averageAmount
                            .multiply(
                                    BigDecimal.valueOf(
                                            52
                                    )
                            )
                            .divide(
                                    BigDecimal.valueOf(
                                            12
                                    ),
                                    2,
                                    RoundingMode.HALF_UP
                            );

        } else {

            /*
             * Monthly recurring expense.
             */
            estimatedMonthlyAmount =
                    averageAmount
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }


        Transaction firstTransaction =
                transactions.get(0);

        Transaction lastTransaction =
                transactions.get(
                        transactions.size() - 1
                );


        return new RecurringExpenseResponse(

                cleanDescription(
                        firstTransaction
                                .getDescription()
                ),

                firstTransaction.getCategory(),

                averageAmount,

                estimatedMonthlyAmount,

                transactions.size(),

                Math.round(
                        averageIntervalDays * 100.0
                ) / 100.0,

                frequency,

                firstTransaction
                        .getCreatedAt()
                        .toLocalDate(),

                lastTransaction
                        .getCreatedAt()
                        .toLocalDate()
        );
    }


    // ============================================================
    // DETERMINE FREQUENCY
    // ============================================================

    private RecurringExpenseResponse.RecurrenceFrequency
    determineFrequency(
            List<Long> intervals,
            double averageIntervalDays
    ) {

        if (intervals.isEmpty()) {

            return RecurringExpenseResponse
                    .RecurrenceFrequency.UNKNOWN;
        }


        /*
         * Weekly pattern:
         *
         * approximately 6–8 days between payments.
         */
        if (averageIntervalDays >= 6
                && averageIntervalDays <= 8
                && intervalsMatchRange(
                intervals,
                5,
                10
        )) {

            return RecurringExpenseResponse
                    .RecurrenceFrequency.WEEKLY;
        }


        /*
         * Monthly pattern:
         *
         * approximately 25–35 days between payments.
         */
        if (averageIntervalDays >= 25
                && averageIntervalDays <= 35
                && intervalsMatchRange(
                intervals,
                20,
                40
        )) {

            return RecurringExpenseResponse
                    .RecurrenceFrequency.MONTHLY;
        }


        return RecurringExpenseResponse
                .RecurrenceFrequency.UNKNOWN;
    }


    // ============================================================
    // INTERVAL CONSISTENCY
    // ============================================================

    private boolean intervalsMatchRange(
            List<Long> intervals,
            long minimum,
            long maximum
    ) {

        for (Long interval : intervals) {

            if (interval < minimum
                    || interval > maximum) {

                return false;
            }
        }

        return true;
    }


    // ============================================================
    // GROUPING KEY
    // ============================================================

    private String buildGroupingKey(
            Transaction transaction
    ) {

        String category =
                transaction.getCategory() == null
                        ? TransactionCategory.OTHER.name()
                        : transaction
                          .getCategory()
                          .name();

        String description =
                normalizeDescription(
                        transaction.getDescription()
                );

        return category + "|" + description;
    }


    // ============================================================
    // NORMALIZE DESCRIPTION
    // ============================================================

    private String normalizeDescription(
            String description
    ) {

        if (description == null) {
            return "";
        }

        return description
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9]+",
                        " "
                )
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }


    // ============================================================
    // CLEAN DESCRIPTION
    // ============================================================

    private String cleanDescription(
            String description
    ) {

        if (description == null
                || description.isBlank()) {

            return "Unknown";
        }

        return description.trim();
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