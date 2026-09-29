package com.bankcore.service;

import com.bankcore.dto.TransactionHistoryResponse;
import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionDirection;
import com.bankcore.entity.User;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TransactionSearchService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> searchMyTransactions(
            TransactionDirection direction,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String counterparty
    ) {

        User user = getAuthenticatedUser();

        validateAmounts(minAmount, maxAmount);

        String directionValue =
                direction == null
                        ? null
                        : direction.name();

        String normalizedCounterparty =
                normalizeCounterparty(counterparty);

        List<Transaction> transactions =
                transactionRepository.searchMyTransactions(
                        user.getId(),
                        directionValue,
                        minAmount,
                        maxAmount,
                        normalizedCounterparty
                );

        return transactions
                .stream()
                .limit(20)
                .map(transaction ->
                        mapToResponse(transaction, user.getId()))
                .toList();
    }


    // ============================================================
    // NORMALIZE COUNTERPARTY
    // ============================================================

    private String normalizeCounterparty(String counterparty) {

        if (counterparty == null || counterparty.isBlank()) {
            return null;
        }

        return counterparty
                .trim()
                .toLowerCase(Locale.ROOT);
    }


    // ============================================================
    // VALIDATE AMOUNTS
    // ============================================================

    private void validateAmounts(
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {

        if (minAmount != null &&
                minAmount.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Minimum amount cannot be negative"
            );
        }

        if (maxAmount != null &&
                maxAmount.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Maximum amount cannot be negative"
            );
        }

        if (minAmount != null &&
                maxAmount != null &&
                minAmount.compareTo(maxAmount) > 0) {

            throw new IllegalArgumentException(
                    "Minimum amount cannot exceed maximum amount"
            );
        }
    }


    // ============================================================
    // MAP TRANSACTION TO RESPONSE
    // ============================================================

    private TransactionHistoryResponse mapToResponse(
            Transaction transaction,
            Long userId
    ) {

        boolean sent =
                transaction
                        .getFromAccount()
                        .getUser()
                        .getId()
                        .equals(userId);

        String direction =
                sent
                        ? "SENT"
                        : "RECEIVED";

        String counterpartyAccountNumber =
                sent
                        ? transaction
                          .getToAccount()
                          .getAccountNumber()
                        : transaction
                          .getFromAccount()
                          .getAccountNumber();

        return new TransactionHistoryResponse(
                transaction.getTransactionReference(),
                direction,
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                counterpartyAccountNumber,
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
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