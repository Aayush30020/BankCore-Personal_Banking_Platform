package com.bankcore.service;

import com.bankcore.dto.CategorySpendingAggregate;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpendingInsightsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SpendingInsightsData getCurrentMonthInsights() {

        User user = getAuthenticatedUser();

        LocalDate today = LocalDate.now();

        LocalDate currentMonthStart =
                today.withDayOfMonth(1);

        LocalDateTime startDate =
                currentMonthStart.atStartOfDay();

        LocalDateTime endDate =
                today.plusDays(1).atStartOfDay();

        List<CategorySpendingAggregate> categories =
                transactionRepository
                        .findCategorySpendingBetweenDates(
                                user.getId(),
                                TransactionStatus.COMPLETED,
                                startDate,
                                endDate
                        );

        BigDecimal totalSpending =
                categories.stream()
                        .map(CategorySpendingAggregate::amount)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        long totalTransactions =
                categories.stream()
                        .mapToLong(
                                CategorySpendingAggregate::transactionCount
                        )
                        .sum();

        CategorySpendingAggregate highestCategory =
                categories.isEmpty()
                        ? null
                        : categories.get(0);

        BigDecimal highestCategoryPercentage =
                BigDecimal.ZERO;

        if (highestCategory != null
                && totalSpending.compareTo(BigDecimal.ZERO) > 0) {

            highestCategoryPercentage =
                    highestCategory.amount()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    totalSpending,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        return new SpendingInsightsData(
                currentMonthStart,
                today,
                totalSpending,
                totalTransactions,
                categories,
                highestCategory,
                highestCategoryPercentage
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

    public record SpendingInsightsData(

            LocalDate startDate,

            LocalDate endDate,

            BigDecimal totalSpending,

            long totalTransactions,

            List<CategorySpendingAggregate> categories,

            CategorySpendingAggregate highestSpendingCategory,

            BigDecimal highestCategoryPercentage

    ) {
    }
}