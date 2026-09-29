package com.bankcore.service;

import com.bankcore.dto.CategorySpendingPeriodResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CategorySpendingPeriodService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CategorySpendingPeriodResponse getSpending(
            TransactionCategory category,
            LocalDate startDate,
            LocalDate endDate
    ) {

        validateInput(
                category,
                startDate,
                endDate
        );

        User user = getAuthenticatedUser();

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate
                        .plusDays(1)
                        .atStartOfDay();

        BigDecimal totalSpending =
                transactionRepository
                        .findCategorySpendingAmountBetweenDates(
                                user.getId(),
                                TransactionStatus.COMPLETED,
                                category,
                                startDateTime,
                                endDateTime
                        );

        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }

        long transactionCount =
                transactionRepository
                        .findCategorySpendingCountBetweenDates(
                                user.getId(),
                                TransactionStatus.COMPLETED,
                                category,
                                startDateTime,
                                endDateTime
                        );

        return new CategorySpendingPeriodResponse(
                startDate,
                endDate,
                category,
                totalSpending,
                transactionCount
        );
    }

    private void validateInput(
            TransactionCategory category,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Transaction category is required"
            );
        }

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "Start date is required"
            );
        }

        if (endDate == null) {
            throw new IllegalArgumentException(
                    "End date is required"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

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