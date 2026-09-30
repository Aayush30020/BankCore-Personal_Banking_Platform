package com.bankcore.service;

import com.bankcore.dto.SpendingComparisonResponse;
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

@Service
@RequiredArgsConstructor
public class SpendingComparisonService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SpendingComparisonResponse compareSpending(
            LocalDate currentPeriodStart,
            LocalDate currentPeriodEnd,
            LocalDate previousPeriodStart,
            LocalDate previousPeriodEnd
    ) {

        User user = getAuthenticatedUser();

        validateDates(
                currentPeriodStart,
                currentPeriodEnd,
                "Current period"
        );

        validateDates(
                previousPeriodStart,
                previousPeriodEnd,
                "Previous period"
        );

        Long userId = user.getId();

        LocalDateTime currentStartDateTime =
                currentPeriodStart.atStartOfDay();

        LocalDateTime currentEndDateTime =
                currentPeriodEnd
                        .plusDays(1)
                        .atStartOfDay();

        BigDecimal currentPeriodSpent =
                transactionRepository.getTotalSpendingBetweenDates(
                        userId,
                        currentStartDateTime,
                        currentEndDateTime
                );

        long currentPeriodTransactionCount =
                transactionRepository.countSpendingBetweenDates(
                        userId,
                        currentStartDateTime,
                        currentEndDateTime
                );

        LocalDateTime previousStartDateTime =
                previousPeriodStart.atStartOfDay();

        LocalDateTime previousEndDateTime =
                previousPeriodEnd
                        .plusDays(1)
                        .atStartOfDay();

        BigDecimal previousPeriodSpent =
                transactionRepository.getTotalSpendingBetweenDates(
                        userId,
                        previousStartDateTime,
                        previousEndDateTime
                );

        long previousPeriodTransactionCount =
                transactionRepository.countSpendingBetweenDates(
                        userId,
                        previousStartDateTime,
                        previousEndDateTime
                );

        BigDecimal difference =
                currentPeriodSpent
                        .subtract(previousPeriodSpent);

        BigDecimal percentageChange =
                calculatePercentageChange(
                        currentPeriodSpent,
                        previousPeriodSpent
                );

        return new SpendingComparisonResponse(
                currentPeriodStart,
                currentPeriodEnd,
                currentPeriodSpent,
                currentPeriodTransactionCount,
                previousPeriodStart,
                previousPeriodEnd,
                previousPeriodSpent,
                previousPeriodTransactionCount,
                difference,
                percentageChange
        );
    }

    private BigDecimal calculatePercentageChange(
            BigDecimal currentValue,
            BigDecimal previousValue
    ) {

        if (previousValue == null ||
                previousValue.compareTo(BigDecimal.ZERO) == 0) {

            return null;
        }

        return currentValue
                .subtract(previousValue)
                .divide(
                        previousValue,
                        4,
                        RoundingMode.HALF_UP
                )
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate,
            String periodName
    ) {

        if (startDate == null) {
            throw new IllegalArgumentException(
                    periodName + " start date is required"
            );
        }

        if (endDate == null) {
            throw new IllegalArgumentException(
                    periodName + " end date is required"
            );
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    periodName + " start date cannot be after end date"
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