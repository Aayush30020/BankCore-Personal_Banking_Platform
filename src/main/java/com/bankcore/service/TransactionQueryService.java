package com.bankcore.service;

import com.bankcore.dto.TransactionHistoryResponse;
import com.bankcore.entity.Transaction;
import com.bankcore.entity.User;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getMyRecentTransactions() {

        User user = getAuthenticatedUser();

        return transactionRepository
                .findRecentTransactionsByUserId(user.getId())
                .stream()
                .map(transaction -> mapToResponse(transaction, user.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getMyRecentTransactions(int limit) {

        if (limit < 1 || limit > 20) {
            throw new IllegalArgumentException(
                    "Transaction limit must be between 1 and 20"
            );
        }

        return getMyRecentTransactions()
                .stream()
                .limit(limit)
                .toList();
    }

    private TransactionHistoryResponse mapToResponse(
            Transaction transaction,
            Long userId
    ) {

        boolean sent = transaction
                .getFromAccount()
                .getUser()
                .getId()
                .equals(userId);

        String direction = sent ? "SENT" : "RECEIVED";

        String counterpartyAccountNumber = sent
                ? transaction.getToAccount().getAccountNumber()
                : transaction.getFromAccount().getAccountNumber();

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

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

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