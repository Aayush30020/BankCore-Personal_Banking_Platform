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
                transactionRepository.getTotalSentByUserId(userId);

        long transactionCount =
                transactionRepository.countSentByUserId(userId);

        BigDecimal largestTransactionAmount =
                transactionRepository.getLargestSentTransactionByUserId(userId);

        BigDecimal smallestTransactionAmount =
                transactionRepository.getSmallestSentTransactionByUserId(userId);

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


    // ============================================================
    // CALCULATE AVERAGE TRANSACTION AMOUNT
    // ============================================================

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


    // ============================================================
    // GET AUTHENTICATED USER
    // ============================================================

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