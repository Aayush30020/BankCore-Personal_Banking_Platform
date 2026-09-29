package com.bankcore.service;

import com.bankcore.dto.CategorySpendingAggregate;
import com.bankcore.dto.CategorySpendingComparison;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonthlySpendingReportService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CategorySpendingComparison> getMonthlyReport() {

        User user = getAuthenticatedUser();

        LocalDate today = LocalDate.now();

        // ========================================================
        // CURRENT MONTH
        // ========================================================

        LocalDate currentMonthStart =
                today.withDayOfMonth(1);

        LocalDate currentMonthEnd =
                today;

        LocalDateTime currentStartDateTime =
                currentMonthStart.atStartOfDay();

        LocalDateTime currentEndDateTime =
                currentMonthEnd
                        .plusDays(1)
                        .atStartOfDay();

        // ========================================================
        // PREVIOUS MONTH
        // ========================================================

        LocalDate previousMonthStart =
                currentMonthStart.minusMonths(1);

        LocalDate previousMonthEnd =
                currentMonthStart.minusDays(1);

        LocalDateTime previousStartDateTime =
                previousMonthStart.atStartOfDay();

        LocalDateTime previousEndDateTime =
                previousMonthEnd
                        .plusDays(1)
                        .atStartOfDay();

        // ========================================================
        // GET CURRENT MONTH CATEGORY DATA
        // ========================================================

        List<CategorySpendingAggregate> currentMonthCategories =
                transactionRepository
                        .findCategorySpendingBetweenDates(
                                user.getId(),
                                TransactionStatus.COMPLETED,
                                currentStartDateTime,
                                currentEndDateTime
                        );

        // ========================================================
        // GET PREVIOUS MONTH CATEGORY DATA
        // ========================================================

        List<CategorySpendingAggregate> previousMonthCategories =
                transactionRepository
                        .findCategorySpendingBetweenDates(
                                user.getId(),
                                TransactionStatus.COMPLETED,
                                previousStartDateTime,
                                previousEndDateTime
                        );

        // ========================================================
        // CONVERT RESULTS INTO MAPS
        // ========================================================

        Map<TransactionCategory, CategorySpendingAggregate>
                currentMonthMap =
                toCategoryMap(currentMonthCategories);

        Map<TransactionCategory, CategorySpendingAggregate>
                previousMonthMap =
                toCategoryMap(previousMonthCategories);

        // ========================================================
        // COMPARE ALL CATEGORIES
        // ========================================================

        List<CategorySpendingComparison> comparisons =
                new ArrayList<>();

        for (TransactionCategory category :
                TransactionCategory.values()) {

            CategorySpendingAggregate current =
                    currentMonthMap.get(category);

            CategorySpendingAggregate previous =
                    previousMonthMap.get(category);

            BigDecimal currentAmount =
                    current != null
                            ? current.amount()
                            : BigDecimal.ZERO;

            long currentTransactions =
                    current != null
                            ? current.transactionCount()
                            : 0L;

            BigDecimal previousAmount =
                    previous != null
                            ? previous.amount()
                            : BigDecimal.ZERO;

            long previousTransactions =
                    previous != null
                            ? previous.transactionCount()
                            : 0L;

            BigDecimal amountDifference =
                    currentAmount.subtract(
                            previousAmount
                    );

            BigDecimal percentageChange =
                    calculatePercentageChange(
                            currentAmount,
                            previousAmount
                    );

            comparisons.add(
                    new CategorySpendingComparison(
                            category,
                            currentAmount,
                            currentTransactions,
                            previousAmount,
                            previousTransactions,
                            amountDifference,
                            percentageChange
                    )
            );
        }

        return comparisons;
    }

    private Map<TransactionCategory, CategorySpendingAggregate>
    toCategoryMap(
            List<CategorySpendingAggregate> categories
    ) {

        Map<TransactionCategory, CategorySpendingAggregate>
                result =
                new EnumMap<>(
                        TransactionCategory.class
                );

        for (CategorySpendingAggregate category :
                categories) {

            result.put(
                    category.category(),
                    category
            );
        }

        return result;
    }

    private BigDecimal calculatePercentageChange(
            BigDecimal currentAmount,
            BigDecimal previousAmount
    ) {

        if (previousAmount == null
                || previousAmount.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }

        return currentAmount
                .subtract(previousAmount)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        previousAmount,
                        2,
                        RoundingMode.HALF_UP
                );
    }

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