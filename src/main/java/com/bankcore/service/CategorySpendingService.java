package com.bankcore.service;

import com.bankcore.dto.CategorySpendingResponse;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorySpendingService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CategorySpendingResponse getMyCategorySpending() {

        User user = getAuthenticatedUser();

        Long userId = user.getId();

        List<Object[]> results =
                transactionRepository.findCategorySpending(
                        userId,
                        TransactionStatus.COMPLETED
                );

        List<CategorySpendingResponse.CategoryBreakdown>
                categories = new ArrayList<>();

        BigDecimal totalSpending = BigDecimal.ZERO;

        int totalTransactions = 0;

        for (Object[] result : results) {

            TransactionCategory category =
                    (TransactionCategory) result[0];

            BigDecimal amount =
                    (BigDecimal) result[1];

            long transactionCount =
                    ((Number) result[2]).longValue();

            categories.add(
                    new CategorySpendingResponse.CategoryBreakdown(
                            category,
                            amount,
                            (int) transactionCount
                    )
            );

            totalSpending =
                    totalSpending.add(amount);

            totalTransactions +=
                    (int) transactionCount;
        }

        return new CategorySpendingResponse(
                totalSpending,
                totalTransactions,
                categories
        );
    }

    @Transactional(readOnly = true)
    public BigDecimal getMySpendingByCategory(
            TransactionCategory category
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Transaction category is required"
            );
        }

        User user = getAuthenticatedUser();

        return transactionRepository.findSpendingByCategory(
                user.getId(),
                TransactionStatus.COMPLETED,
                category
        );
    }

    @Transactional(readOnly = true)
    public long getMyTransactionCountByCategory(
            TransactionCategory category
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Transaction category is required"
            );
        }

        User user = getAuthenticatedUser();

        return transactionRepository
                .countSpendingTransactionsByCategory(
                        user.getId(),
                        TransactionStatus.COMPLETED,
                        category
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