package com.bankcore.service;

import com.bankcore.dto.AddMoneyRequest;
import com.bankcore.dto.AddMoneyResponse;
import com.bankcore.dto.DemoDataResponse;
import com.bankcore.entity.Account;
import com.bankcore.entity.AccountStatus;
import com.bankcore.entity.AccountType;
import com.bankcore.entity.LedgerEntry;
import com.bankcore.entity.LedgerEntryType;
import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.TransactionStatus;
import com.bankcore.entity.User;
import com.bankcore.repository.AccountRepository;
import com.bankcore.repository.LedgerEntryRepository;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemoBankingService {

    private static final String FUNDING_EMAIL =
            "demo-funding@bankcore.internal";

    private static final String FUNDING_ACCOUNT_NUMBER =
            "BKDEMOFUNDING001";

    private static final BigDecimal FUNDING_POOL =
            new BigDecimal("1000000000.00");

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Adds simulated money to one of the authenticated
     * user's own accounts.
     *
     * The money comes from a dedicated internal demo
     * funding account so that the transaction maintains
     * proper double-entry accounting.
     */
    @Transactional
    public AddMoneyResponse addMoney(AddMoneyRequest request) {

        User user = getAuthenticatedUser();

        Account targetAccount = accountRepository
                .findByIdForUpdate(request.accountId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        )
                );

        if (!targetAccount.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You can only add money to your own account"
            );
        }

        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Account is not active"
            );
        }

        Account fundingAccount = getOrCreateFundingAccount();

        if (fundingAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException(
                    "Demo funding account does not have enough balance"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        String transactionReference =
                generateTransactionReference();

        String idempotencyKey =
                "DEMO-DEPOSIT-" + UUID.randomUUID();

        Transaction transaction = Transaction.builder()
                .transactionReference(transactionReference)
                .fromAccount(fundingAccount)
                .toAccount(targetAccount)
                .amount(request.amount())
                .currency(targetAccount.getCurrency())
                .status(TransactionStatus.COMPLETED)
                .category(TransactionCategory.OTHER)
                .idempotencyKey(idempotencyKey)
                .description(
                        request.description() == null ||
                                request.description().isBlank()
                                ? "Demo account funding"
                                : request.description().trim()
                )
                .createdAt(now)
                .completedAt(now)
                .build();

        fundingAccount.setBalance(
                fundingAccount.getBalance()
                        .subtract(request.amount())
        );

        targetAccount.setBalance(
                targetAccount.getBalance()
                        .add(request.amount())
        );

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        accountRepository.save(fundingAccount);
        accountRepository.save(targetAccount);

        createLedgerEntries(
                savedTransaction,
                fundingAccount,
                targetAccount,
                request.amount(),
                now
        );

        return new AddMoneyResponse(
                targetAccount.getId(),
                targetAccount.getAccountNumber(),
                request.amount(),
                targetAccount.getBalance(),
                savedTransaction.getTransactionReference(),
                "Money added successfully"
        );
    }

    /**
     * Generates a complete demo banking profile.
     *
     * Current account:
     * ₹33,000
     *
     * Savings account:
     * ₹50,000
     *
     * Also creates realistic spending transactions.
     */
    @Transactional
    public DemoDataResponse generateDemoData() {

        User user = getAuthenticatedUser();

        Account currentAccount =
                getOrCreateAccount(
                        user,
                        AccountType.CURRENT
                );

        Account savingsAccount =
                getOrCreateAccount(
                        user,
                        AccountType.SAVINGS
                );

        /*
         * Avoid generating duplicate demo data.
         */
        if (hasDemoData(user)) {
            return new DemoDataResponse(
                    false,
                    "Demo data already exists for this account",
                    currentAccount.getId(),
                    savingsAccount.getId(),
                    currentAccount.getBalance(),
                    savingsAccount.getBalance()
            );
        }

        /*
         * Initial funding.
         */
        addMoneyInternal(
                user,
                currentAccount,
                new BigDecimal("100000.00"),
                "Demo initial funding"
        );

        /*
         * Move ₹50,000 from Current to Savings.
         */
        createTransfer(
                user,
                currentAccount,
                savingsAccount,
                new BigDecimal("50000.00"),
                TransactionCategory.TRANSFER,
                "Demo transfer to savings"
        );

        /*
         * Demo spending.
         */
        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("4000.00"),
                TransactionCategory.FOOD,
                "Food & dining"
        );

        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("6000.00"),
                TransactionCategory.SHOPPING,
                "Shopping"
        );

        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("3200.00"),
                TransactionCategory.BILLS,
                "Monthly bills"
        );

        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("1800.00"),
                TransactionCategory.TRANSPORT,
                "Transport"
        );

        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("900.00"),
                TransactionCategory.ENTERTAINMENT,
                "Entertainment"
        );

        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("800.00"),
                TransactionCategory.HEALTH,
                "Healthcare"
        );

        createMerchantTransaction(
                user,
                currentAccount,
                new BigDecimal("300.00"),
                TransactionCategory.EDUCATION,
                "Education"
        );

        Account refreshedCurrent =
                accountRepository
                        .findById(currentAccount.getId())
                        .orElseThrow();

        Account refreshedSavings =
                accountRepository
                        .findById(savingsAccount.getId())
                        .orElseThrow();

        return new DemoDataResponse(
                true,
                "Demo banking data generated successfully",
                refreshedCurrent.getId(),
                refreshedSavings.getId(),
                refreshedCurrent.getBalance(),
                refreshedSavings.getBalance()
        );
    }

    private boolean hasDemoData(User user) {

        return transactionRepository
                .findRecentTransactionsByUserId(user.getId())
                .stream()
                .anyMatch(transaction ->
                        transaction.getDescription() != null &&
                                transaction.getDescription()
                                        .startsWith("Demo ")
                );
    }

    private Account getOrCreateAccount(
            User user,
            AccountType accountType
    ) {

        return accountRepository
                .findByUserId(user.getId())
                .stream()
                .filter(account ->
                        account.getType() == accountType
                )
                .findFirst()
                .orElseGet(() -> {

                    Account account = Account.builder()
                            .accountNumber(
                                    generateAccountNumber()
                            )
                            .user(user)
                            .type(accountType)
                            .status(AccountStatus.ACTIVE)
                            .balance(BigDecimal.ZERO)
                            .currency("INR")
                            .build();

                    return accountRepository.save(account);
                });
    }

    private void addMoneyInternal(
            User user,
            Account targetAccount,
            BigDecimal amount,
            String description
    ) {

        Account fundingAccount =
                getOrCreateFundingAccount();

        if (fundingAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Demo funding account does not have enough balance"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .transactionReference(
                        generateTransactionReference()
                )
                .fromAccount(fundingAccount)
                .toAccount(targetAccount)
                .amount(amount)
                .currency("INR")
                .status(TransactionStatus.COMPLETED)
                .category(TransactionCategory.OTHER)
                .idempotencyKey(
                        "DEMO-FUND-" + UUID.randomUUID()
                )
                .description(description)
                .createdAt(now)
                .completedAt(now)
                .build();

        fundingAccount.setBalance(
                fundingAccount.getBalance().subtract(amount)
        );

        targetAccount.setBalance(
                targetAccount.getBalance().add(amount)
        );

        Transaction saved =
                transactionRepository.save(transaction);

        accountRepository.save(fundingAccount);
        accountRepository.save(targetAccount);

        createLedgerEntries(
                saved,
                fundingAccount,
                targetAccount,
                amount,
                now
        );
    }

    private void createTransfer(
            User user,
            Account source,
            Account destination,
            BigDecimal amount,
            TransactionCategory category,
            String description
    ) {

        Account lockedSource =
                accountRepository
                        .findByIdForUpdate(source.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Source account not found"
                                )
                        );

        Account lockedDestination =
                accountRepository
                        .findByIdForUpdate(destination.getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destination account not found"
                                )
                        );

        if (!lockedSource.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Source account does not belong to user"
            );
        }

        if (lockedSource.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient balance"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .transactionReference(
                        generateTransactionReference()
                )
                .fromAccount(lockedSource)
                .toAccount(lockedDestination)
                .amount(amount)
                .currency("INR")
                .status(TransactionStatus.COMPLETED)
                .category(category)
                .idempotencyKey(
                        "DEMO-TRANSFER-" + UUID.randomUUID()
                )
                .description(description)
                .createdAt(now)
                .completedAt(now)
                .build();

        lockedSource.setBalance(
                lockedSource.getBalance().subtract(amount)
        );

        lockedDestination.setBalance(
                lockedDestination.getBalance().add(amount)
        );

        Transaction saved =
                transactionRepository.save(transaction);

        accountRepository.save(lockedSource);
        accountRepository.save(lockedDestination);

        createLedgerEntries(
                saved,
                lockedSource,
                lockedDestination,
                amount,
                now
        );
    }

    private void createMerchantTransaction(
            User user,
            Account sourceAccount,
            BigDecimal amount,
            TransactionCategory category,
            String description
    ) {

        Account merchantAccount =
                getOrCreateMerchantAccount(category);

        createTransfer(
                user,
                sourceAccount,
                merchantAccount,
                amount,
                category,
                "Demo " + description
        );
    }

    private Account getOrCreateMerchantAccount(
            TransactionCategory category
    ) {

        String email =
                "demo-merchant-" +
                        category.name().toLowerCase() +
                        "@bankcore.internal";

        User merchantUser =
                userRepository.findByEmail(email)
                        .orElseGet(() -> {

                            User user = User.builder()
                                    .name(
                                            getMerchantName(category)
                                    )
                                    .email(email)
                                    .password(
                                            passwordEncoder.encode(
                                                    UUID.randomUUID()
                                                            .toString()
                                            )
                                    )
                                    .build();

                            return userRepository.save(user);
                        });

        return accountRepository
                .findByUserId(merchantUser.getId())
                .stream()
                .findFirst()
                .orElseGet(() -> {

                    Account account = Account.builder()
                            .accountNumber(
                                    generateMerchantAccountNumber(
                                            category
                                    )
                            )
                            .user(merchantUser)
                            .type(AccountType.CURRENT)
                            .status(AccountStatus.ACTIVE)
                            .balance(BigDecimal.ZERO)
                            .currency("INR")
                            .build();

                    return accountRepository.save(account);
                });
    }

    private Account getOrCreateFundingAccount() {

        User fundingUser =
                userRepository
                        .findByEmail(FUNDING_EMAIL)
                        .orElseGet(() -> {

                            User user = User.builder()
                                    .name("BankCore Demo Funding")
                                    .email(FUNDING_EMAIL)
                                    .password(
                                            passwordEncoder.encode(
                                                    UUID.randomUUID()
                                                            .toString()
                                            )
                                    )
                                    .build();

                            return userRepository.save(user);
                        });

        return accountRepository
                .findByAccountNumber(
                        FUNDING_ACCOUNT_NUMBER
                )
                .orElseGet(() -> {

                    Account account = Account.builder()
                            .accountNumber(
                                    FUNDING_ACCOUNT_NUMBER
                            )
                            .user(fundingUser)
                            .type(AccountType.CURRENT)
                            .status(AccountStatus.ACTIVE)
                            .balance(FUNDING_POOL)
                            .currency("INR")
                            .build();

                    return accountRepository.save(account);
                });
    }

    private void createLedgerEntries(
            Transaction transaction,
            Account source,
            Account destination,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {

        LedgerEntry debitEntry =
                LedgerEntry.builder()
                        .transaction(transaction)
                        .account(source)
                        .entryType(LedgerEntryType.DEBIT)
                        .amount(amount)
                        .createdAt(createdAt)
                        .build();

        LedgerEntry creditEntry =
                LedgerEntry.builder()
                        .transaction(transaction)
                        .account(destination)
                        .entryType(LedgerEntryType.CREDIT)
                        .amount(amount)
                        .createdAt(createdAt)
                        .build();

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }

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
                        .existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }

    private String generateMerchantAccountNumber(
            TransactionCategory category
    ) {

        String prefix =
                "BKMER" +
                        category.name()
                                .substring(
                                        0,
                                        Math.min(
                                                4,
                                                category.name().length()
                                        )
                                );

        String accountNumber;

        do {
            accountNumber =
                    prefix +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 10)
                                    .toUpperCase();

        } while (
                accountRepository
                        .existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }

    private String generateTransactionReference() {

        return "TXN-" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 20)
                        .toUpperCase();
    }

    private String getMerchantName(
            TransactionCategory category
    ) {

        return switch (category) {

            case FOOD ->
                    "Demo Food Store";

            case SHOPPING ->
                    "Demo Shopping Store";

            case BILLS ->
                    "Demo Utilities";

            case TRANSPORT ->
                    "Demo Transport";

            case ENTERTAINMENT ->
                    "Demo Entertainment";

            case HEALTH ->
                    "Demo Healthcare";

            case EDUCATION ->
                    "Demo Education";

            case TRANSFER ->
                    "Demo Transfer";

            case OTHER ->
                    "Demo Merchant";
        };
    }
}