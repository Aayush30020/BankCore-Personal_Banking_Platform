package com.bankcore.service;

import com.bankcore.dto.TransferRequest;
import com.bankcore.dto.TransferResponse;
import com.bankcore.entity.Account;
import com.bankcore.entity.AccountStatus;
import com.bankcore.entity.LedgerEntry;
import com.bankcore.entity.LedgerEntryType;
import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionStatus;
import com.bankcore.entity.User;
import com.bankcore.repository.AccountRepository;
import com.bankcore.repository.LedgerEntryRepository;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public TransferResponse transfer(
            TransferRequest request,
            String idempotencyKey
    ) {
        validateRequest(request, idempotencyKey);

        var existingTransaction =
                transactionRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {
            return mapToResponse(existingTransaction.get());
        }

        User authenticatedUser = getAuthenticatedUser();

        if (request.fromAccountId()
                .equals(request.toAccountId())) {

            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be the same"
            );
        }

        Long firstAccountId =
                Math.min(
                        request.fromAccountId(),
                        request.toAccountId()
                );

        Long secondAccountId =
                Math.max(
                        request.fromAccountId(),
                        request.toAccountId()
                );

        Account firstLockedAccount =
                accountRepository
                        .findByIdForUpdate(firstAccountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found: "
                                                + firstAccountId
                                )
                        );

        Account secondLockedAccount =
                accountRepository
                        .findByIdForUpdate(secondAccountId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found: "
                                                + secondAccountId
                                )
                        );

        Account sourceAccount =
                request.fromAccountId()
                        .equals(firstAccountId)
                        ? firstLockedAccount
                        : secondLockedAccount;

        Account destinationAccount =
                request.toAccountId()
                        .equals(firstAccountId)
                        ? firstLockedAccount
                        : secondLockedAccount;

        if (!sourceAccount.getUser().getId()
                .equals(authenticatedUser.getId())) {

            throw new IllegalArgumentException(
                    "You are not authorized to transfer from this account"
            );
        }

        validateAccountStatus(sourceAccount);
        validateAccountStatus(destinationAccount);

        if (sourceAccount.getBalance()
                .compareTo(request.amount()) < 0) {

            throw new IllegalArgumentException(
                    "Insufficient account balance"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Transaction transaction =
                Transaction.builder()
                        .transactionReference(
                                generateTransactionReference()
                        )
                        .fromAccount(sourceAccount)
                        .toAccount(destinationAccount)
                        .amount(request.amount())
                        .currency(sourceAccount.getCurrency())
                        .status(TransactionStatus.PROCESSING)
                        .idempotencyKey(idempotencyKey)
                        .description(request.description())
                        .createdAt(now)
                        .build();

        transaction =
                transactionRepository.save(transaction);

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(request.amount())
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(request.amount())
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        LedgerEntry debitEntry =
                LedgerEntry.builder()
                        .transaction(transaction)
                        .account(sourceAccount)
                        .entryType(LedgerEntryType.DEBIT)
                        .amount(request.amount())
                        .createdAt(now)
                        .build();

        ledgerEntryRepository.save(debitEntry);

        LedgerEntry creditEntry =
                LedgerEntry.builder()
                        .transaction(transaction)
                        .account(destinationAccount)
                        .entryType(LedgerEntryType.CREDIT)
                        .amount(request.amount())
                        .createdAt(now)
                        .build();

        ledgerEntryRepository.save(creditEntry);

        transaction.setStatus(
                TransactionStatus.COMPLETED
        );

        transaction.setCompletedAt(
                LocalDateTime.now()
        );

        transaction =
                transactionRepository.save(transaction);

        /*
         * Publish the event only after the transaction
         * has been fully prepared.
         *
         * The listener uses AFTER_COMMIT, so Gemini
         * categorization will only happen after this
         * database transaction successfully commits.
         */
        eventPublisher.publishEvent(
                new TransactionCategorizationEvent(
                        transaction.getId()
                )
        );

        return mapToResponse(transaction);
    }

    private void validateRequest(
            TransferRequest request,
            String idempotencyKey
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Transfer request cannot be null"
            );
        }

        if (request.fromAccountId() == null ||
                request.toAccountId() == null) {

            throw new IllegalArgumentException(
                    "Both source and destination accounts are required"
            );
        }

        if (request.amount() == null ||
                request.amount()
                        .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Transfer amount must be greater than zero"
            );
        }

        if (idempotencyKey == null ||
                idempotencyKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        if (idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key cannot exceed 100 characters"
            );
        }
    }

    private void validateAccountStatus(
            Account account
    ) {
        if (account.getStatus() != AccountStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Account "
                            + account.getId()
                            + " is not active"
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

    private String generateTransactionReference() {

        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
    }

    private TransferResponse mapToResponse(
            Transaction transaction
    ) {

        return new TransferResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                transaction.getFromAccount().getId(),
                transaction.getToAccount().getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}