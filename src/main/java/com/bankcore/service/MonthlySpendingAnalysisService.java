package com.bankcore.service;

import com.bankcore.dto.MonthlySpendingAnalysisResponse;
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
public class MonthlySpendingAnalysisService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MonthlySpendingAnalysisResponse getSpendingAnalysis(
            LocalDate startDate,
            LocalDate endDate
    ) {

        User user = getAuthenticatedUser();

        validateDates(startDate, endDate);

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.plusDays(1).atStartOfDay();

        Long userId = user.getId();

        BigDecimal totalSpent =
                transactionRepository.getTotalSpendingBetweenDates(
                        userId,
                        startDateTime,
                        endDateTime
                );

        long transactionCount =
                transactionRepository.countSpendingBetweenDates(
                        userId,
                        startDateTime,
                        endDateTime
                );

        BigDecimal largestTransactionAmount =
                transactionRepository.getLargestSpendingBetweenDates(
                        userId,
                        startDateTime,
                        endDateTime
                );

        BigDecimal smallestTransactionAmount =
                transactionRepository.getSmallestSpendingBetweenDates(
                        userId,
                        startDateTime,
                        endDateTime
                );

        if (largestTransactionAmount == null) {
            largestTransactionAmount = BigDecimal.ZERO;
        }

        if (smallestTransactionAmount == null) {
            smallestTransactionAmount = BigDecimal.ZERO;
        }

        BigDecimal averageTransactionAmount =
                calculateAverage(
                        totalSpent,
                        transactionCount
                );

        return new MonthlySpendingAnalysisResponse(
                startDate,
                endDate,
                totalSpent,
                transactionCount,
                averageTransactionAmount,
                largestTransactionAmount,
                smallestTransactionAmount
        );
    }

    private BigDecimal calculateAverage(
            BigDecimal totalSpent,
            long transactionCount
    ) {

        if (transactionCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalSpent.divide(
                BigDecimal.valueOf(transactionCount),
                2,
                RoundingMode.HALF_UP
        );
    }

    private void validateDates(
            LocalDate startDate,
            LocalDate endDate
    ) {

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

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
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