package com.bankcore.config;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bankcore.demo.user-email:}")
    private String mainUserEmail;

    /*
     * IMPORTANT:
     *
     * Keep this FALSE during normal application operation.
     * We will temporarily change it to TRUE for one run.
     */
    private static final boolean ENABLE_DEMO_SEED = false;

    private static final String DEMO_PASSWORD =
            "DemoBankCore@2026";

    @Override
    @Transactional
    public void run(String... args) {

        if (!ENABLE_DEMO_SEED) {
            return;
        }

        System.out.println(
                "================================================="
        );
        System.out.println(
                "BANKCORE DEMO DATA SEEDER STARTING"
        );
        System.out.println(
                "================================================="
        );

        /*
         * ---------------------------------------------------------
         * Prevent duplicate demo data
         * ---------------------------------------------------------
         */

        if (userRepository
                .findByEmail("freshmart@demo.bankcore")
                .isPresent()) {

            System.out.println(
                    "Demo data already exists. Skipping."
            );

            return;
        }

        /*
         * ---------------------------------------------------------
         * Find your existing user
         * ---------------------------------------------------------
         */

        if (mainUserEmail == null ||
                mainUserEmail.isBlank()) {

            throw new IllegalStateException(
                    "bankcore.demo.user-email is not configured"
            );
        }

        User mainUser =
                userRepository
                        .findByEmail(mainUserEmail)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Main BankCore user not found: "
                                                + mainUserEmail
                                )
                        );

        /*
         * ---------------------------------------------------------
         * Find your active CURRENT account
         * ---------------------------------------------------------
         */

        Account mainCurrentAccount =
                accountRepository
                        .findByUserId(mainUser.getId())
                        .stream()
                        .filter(account ->
                                account.getType()
                                        == AccountType.CURRENT
                        )
                        .filter(account ->
                                account.getStatus()
                                        == AccountStatus.ACTIVE
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Active CURRENT account not found"
                                )
                        );

        /*
         * ---------------------------------------------------------
         * Total demo spending = ₹17,000
         * ---------------------------------------------------------
         */

        BigDecimal requiredBalance =
                new BigDecimal("17000.00");

        if (mainCurrentAccount
                .getBalance()
                .compareTo(requiredBalance) < 0) {

            throw new IllegalStateException(
                    "Current account needs at least ₹17,000 "
                            + "before running the demo seeder."
            );
        }

        /*
         * ---------------------------------------------------------
         * Create demo recipients
         * ---------------------------------------------------------
         */

        DemoRecipient freshMart =
                createRecipient(
                        "FreshMart",
                        "freshmart@demo.bankcore"
                );

        DemoRecipient foodHub =
                createRecipient(
                        "FoodHub",
                        "foodhub@demo.bankcore"
                );

        DemoRecipient metroRide =
                createRecipient(
                        "MetroRide",
                        "metroride@demo.bankcore"
                );

        DemoRecipient techStore =
                createRecipient(
                        "TechStore",
                        "techstore@demo.bankcore"
                );

        DemoRecipient utilityCare =
                createRecipient(
                        "UtilityCare",
                        "utilitycare@demo.bankcore"
                );

        DemoRecipient streamPlus =
                createRecipient(
                        "StreamPlus",
                        "streamplus@demo.bankcore"
                );

        DemoRecipient healthPlus =
                createRecipient(
                        "HealthPlus",
                        "healthplus@demo.bankcore"
                );

        DemoRecipient courseHub =
                createRecipient(
                        "CourseHub",
                        "coursehub@demo.bankcore"
                );

        /*
         * ---------------------------------------------------------
         * Create realistic transactions
         * ---------------------------------------------------------
         */

        createDemoTransaction(
                mainCurrentAccount,
                freshMart.account(),
                new BigDecimal("2500.00"),
                TransactionCategory.FOOD,
                "FreshMart groceries",
                27
        );

        createDemoTransaction(
                mainCurrentAccount,
                foodHub.account(),
                new BigDecimal("1500.00"),
                TransactionCategory.FOOD,
                "FoodHub dinner",
                23
        );

        createDemoTransaction(
                mainCurrentAccount,
                metroRide.account(),
                new BigDecimal("1800.00"),
                TransactionCategory.TRANSPORT,
                "MetroRide monthly travel",
                20
        );

        createDemoTransaction(
                mainCurrentAccount,
                techStore.account(),
                new BigDecimal("6000.00"),
                TransactionCategory.SHOPPING,
                "TechStore electronics purchase",
                16
        );

        createDemoTransaction(
                mainCurrentAccount,
                utilityCare.account(),
                new BigDecimal("3200.00"),
                TransactionCategory.BILLS,
                "UtilityCare monthly bill",
                12
        );

        createDemoTransaction(
                mainCurrentAccount,
                streamPlus.account(),
                new BigDecimal("900.00"),
                TransactionCategory.ENTERTAINMENT,
                "StreamPlus subscription",
                8
        );

        createDemoTransaction(
                mainCurrentAccount,
                healthPlus.account(),
                new BigDecimal("800.00"),
                TransactionCategory.HEALTH,
                "HealthPlus pharmacy",
                5
        );

        createDemoTransaction(
                mainCurrentAccount,
                courseHub.account(),
                new BigDecimal("300.00"),
                TransactionCategory.EDUCATION,
                "CourseHub learning material",
                3
        );

        accountRepository.save(mainCurrentAccount);

        System.out.println(
                "================================================="
        );
        System.out.println(
                "BANKCORE DEMO DATA SEEDING COMPLETED"
        );
        System.out.println(
                "Demo spending created: ₹17,000.00"
        );
        System.out.println(
                "Remaining CURRENT balance: ₹"
                        + mainCurrentAccount.getBalance()
        );
        System.out.println(
                "================================================="
        );
    }

    private DemoRecipient createRecipient(
            String name,
            String email
    ) {

        User user =
                User.builder()
                        .name(name)
                        .email(email)
                        .password(
                                passwordEncoder.encode(
                                        DEMO_PASSWORD
                                )
                        )
                        .status(User.UserStatus.ACTIVE)
                        .role(User.UserRole.USER)
                        .createdAt(LocalDateTime.now())
                        .build();

        user = userRepository.save(user);

        Account account =
                Account.builder()
                        .accountNumber(
                                generateAccountNumber()
                        )
                        .user(user)
                        .type(AccountType.CURRENT)
                        .status(AccountStatus.ACTIVE)
                        .balance(BigDecimal.ZERO)
                        .currency("INR")
                        .createdAt(LocalDateTime.now())
                        .build();

        account = accountRepository.save(account);

        return new DemoRecipient(user, account);
    }

    private void createDemoTransaction(
            Account sourceAccount,
            Account destinationAccount,
            BigDecimal amount,
            TransactionCategory category,
            String description,
            int daysAgo
    ) {

        if (sourceAccount
                .getBalance()
                .compareTo(amount) < 0) {

            throw new IllegalStateException(
                    "Insufficient balance for: "
                            + description
            );
        }

        LocalDateTime transactionTime =
                LocalDateTime.now()
                        .minusDays(daysAgo);

        /*
         * Transaction
         */

        Transaction transaction =
                Transaction.builder()
                        .transactionReference(
                                generateTransactionReference()
                        )
                        .fromAccount(sourceAccount)
                        .toAccount(destinationAccount)
                        .amount(amount)
                        .currency("INR")
                        .status(
                                TransactionStatus.COMPLETED
                        )
                        .category(category)
                        .idempotencyKey(
                                "DEMO-" +
                                        UUID.randomUUID()
                        )
                        .description(description)
                        .createdAt(transactionTime)
                        .completedAt(
                                transactionTime.plusSeconds(2)
                        )
                        .build();

        transaction =
                transactionRepository.save(transaction);

        /*
         * Balance changes
         */

        sourceAccount.setBalance(
                sourceAccount
                        .getBalance()
                        .subtract(amount)
        );

        destinationAccount.setBalance(
                destinationAccount
                        .getBalance()
                        .add(amount)
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        /*
         * DEBIT
         */

        LedgerEntry debitEntry =
                LedgerEntry.builder()
                        .transaction(transaction)
                        .account(sourceAccount)
                        .entryType(
                                LedgerEntryType.DEBIT
                        )
                        .amount(amount)
                        .createdAt(transactionTime)
                        .build();

        ledgerEntryRepository.save(debitEntry);

        /*
         * CREDIT
         */

        LedgerEntry creditEntry =
                LedgerEntry.builder()
                        .transaction(transaction)
                        .account(destinationAccount)
                        .entryType(
                                LedgerEntryType.CREDIT
                        )
                        .amount(amount)
                        .createdAt(transactionTime)
                        .build();

        ledgerEntryRepository.save(creditEntry);

        System.out.println(
                "Created: "
                        + description
                        + " | ₹"
                        + amount
                        + " | "
                        + category
        );
    }

    private String generateAccountNumber() {

        String accountNumber;

        do {

            accountNumber =
                    "BK"
                            + UUID.randomUUID()
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

    private String generateTransactionReference() {

        return "TXN-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16)
                .toUpperCase();
    }

    private record DemoRecipient(
            User user,
            Account account
    ) {
    }
}