package com.bankcore.service;

import com.bankcore.dto.AccountResponse;
import com.bankcore.dto.CreateAccountRequest;
import com.bankcore.entity.Account;
import com.bankcore.entity.AccountStatus;
import com.bankcore.entity.User;
import com.bankcore.repository.AccountRepository;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;


    // ============================================================
    // CREATE ACCOUNT
    // ============================================================

    /**
     * Creates a new bank account for the currently
     * authenticated user.
     */
    @Transactional
    public AccountResponse createAccount(
            CreateAccountRequest request
    ) {

        User user = getAuthenticatedUser();

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .user(user)
                .type(request.type())
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .build();

        Account savedAccount =
                accountRepository.save(account);

        return mapToResponse(savedAccount);
    }


    // ============================================================
    // GET MY ACCOUNTS
    // ============================================================

    /**
     * Returns all accounts belonging to the
     * currently authenticated user.
     */
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts() {

        User user = getAuthenticatedUser();

        return accountRepository
                .findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // ============================================================
    // GET SPECIFIC ACCOUNT
    // ============================================================

    /**
     * Returns a specific account only if it belongs
     * to the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(
            Long accountId
    ) {

        User user = getAuthenticatedUser();

        Account account =
                accountRepository
                        .findByIdAndUserId(
                                accountId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Account not found"
                                )
                        );

        return mapToResponse(account);
    }


    // ============================================================
    // RECIPIENT LOOKUP
    // ============================================================

    /**
     * Finds an active recipient account using the
     * account number entered by the authenticated user.
     *
     * IMPORTANT:
     *
     * We intentionally return only:
     *
     * - account ID
     * - account number
     * - account holder name
     *
     * We do NOT expose:
     *
     * - balance
     * - account status
     * - other sensitive account information
     */
    @Transactional(readOnly = true)
    public RecipientAccount lookupRecipient(
            String accountNumber
    ) {

        if (accountNumber == null ||
                accountNumber.isBlank()) {

            throw new IllegalArgumentException(
                    "Recipient account number is required"
            );
        }

        String normalizedAccountNumber =
                accountNumber
                        .trim()
                        .toUpperCase();

        Account account =
                accountRepository
                        .findByAccountNumberAndStatus(
                                normalizedAccountNumber,
                                AccountStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Active recipient account not found"
                                )
                        );

        return new RecipientAccount(
                account.getId(),
                account.getAccountNumber(),
                account.getUser().getName()
        );
    }


    // ============================================================
    // AUTHENTICATED USER
    // ============================================================

    /**
     * Gets the currently authenticated user
     * from the JWT/Spring Security context.
     */
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
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user does not exist"
                        )
                );
    }


    // ============================================================
    // ACCOUNT NUMBER GENERATION
    // ============================================================

    /**
     * Generates a unique bank account number.
     */
    private String generateAccountNumber() {

        String accountNumber;

        do {

            accountNumber =
                    "BK" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 16)
                                    .toUpperCase();

        } while (
                accountRepository
                        .existsByAccountNumber(
                                accountNumber
                        )
        );

        return accountNumber;
    }


    // ============================================================
    // ENTITY → DTO
    // ============================================================

    /**
     * Converts Entity → API response DTO.
     */
    private AccountResponse mapToResponse(
            Account account
    ) {

        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getType(),
                account.getStatus(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt()
        );
    }


    // ============================================================
    // RECIPIENT RESPONSE
    // ============================================================

    /**
     * Safe recipient information returned during
     * account-number verification.
     */
    public record RecipientAccount(

            Long accountId,

            String accountNumber,

            String accountHolderName

    ) {
    }
}