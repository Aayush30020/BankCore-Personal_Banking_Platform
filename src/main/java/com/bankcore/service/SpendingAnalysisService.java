package com.bankcore.service;

import com.bankcore.dto.SpendingAnalysisResponse;
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

@Service
@RequiredArgsConstructor
public class SpendingAnalysisService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SpendingAnalysisResponse getSpendingAnalysis() {

        User user = getAuthenticatedUser();

        Long userId = user.getId();

        BigDecimal totalSpent =
                transactionRepository.getTotalSpendingByUserId(userId);

        long transactionCount =
                transactionRepository.countSpendingByUserId(userId);

        BigDecimal largestTransactionAmount =
                transactionRepository.getLargestSpendingTransactionByUserId(userId);

        BigDecimal smallestTransactionAmount =
                transactionRepository.getSmallestSpendingTransactionByUserId(userId);

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

        BigDecimal netSpending =
                totalSpent.negate();

        return new SpendingAnalysisResponse(
                totalSpent,
                transactionCount,
                averageTransactionAmount,
                largestTransactionAmount,
                smallestTransactionAmount,
                netSpending
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