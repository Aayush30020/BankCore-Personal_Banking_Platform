package com.bankcore.repository;

import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(
            String idempotencyKey
    );

    Optional<Transaction> findByTransactionReference(
            String transactionReference
    );


    // ============================================================
    // RECENT TRANSACTIONS
    // ============================================================

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
               OR t.toAccount.user.id = :userId
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findRecentTransactionsByUserId(
            @Param("userId") Long userId
    );


    // ============================================================
    // FINANCIAL INSIGHTS
    //
    // These methods intentionally INCLUDE TRANSFERS.
    //
    // They represent money movement, not actual spending.
    // ============================================================

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
            """)
    BigDecimal getTotalSentByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.toAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
            """)
    BigDecimal getTotalReceivedByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
            """)
    long countSentByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.toAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
            """)
    long countReceivedByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT MAX(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
            """)
    BigDecimal getLargestSentTransactionByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT MIN(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
            """)
    BigDecimal getSmallestSentTransactionByUserId(
            @Param("userId") Long userId
    );


    // ============================================================
    // ACTUAL SPENDING
    //
    // TRANSFER is explicitly excluded.
    //
    // These methods are used by spending analytics.
    // ============================================================

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
            """)
    BigDecimal getTotalSpendingByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
            """)
    long countSpendingByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT MAX(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
            """)
    BigDecimal getLargestSpendingTransactionByUserId(
            @Param("userId") Long userId
    );

    @Query("""
            SELECT MIN(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
            """)
    BigDecimal getSmallestSpendingTransactionByUserId(
            @Param("userId") Long userId
    );


    // ============================================================
    // ACTUAL SPENDING FOR DATE RANGE
    // ============================================================

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal getTotalSpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    long countSpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT MAX(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal getLargestSpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT MIN(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND (
                    t.category IS NULL
                    OR t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
                  )
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal getSmallestSpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    // ============================================================
    // ORIGINAL SENT TRANSACTION DATE-RANGE METHODS
    //
    // These intentionally INCLUDE TRANSFERS.
    // They remain available for money-movement analytics.
    // ============================================================

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal getTotalSentBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    long countSentBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT MAX(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal getLargestSentBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
            SELECT MIN(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = com.bankcore.entity.TransactionStatus.COMPLETED
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal getSmallestSentBetweenDates(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    // ============================================================
    // TRANSACTION SEARCH
    // ============================================================

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE
                (
                    t.fromAccount.user.id = :userId
                    OR
                    t.toAccount.user.id = :userId
                )

                AND t.status =
                    com.bankcore.entity.TransactionStatus.COMPLETED

                AND (
                    :direction IS NULL

                    OR

                    (
                        :direction = 'SENT'
                        AND t.fromAccount.user.id = :userId
                    )

                    OR

                    (
                        :direction = 'RECEIVED'
                        AND t.toAccount.user.id = :userId
                    )
                )

                AND (
                    :minAmount IS NULL
                    OR t.amount >= :minAmount
                )

                AND (
                    :maxAmount IS NULL
                    OR t.amount <= :maxAmount
                )

                AND (
                    :counterparty IS NULL

                    OR

                    (
                        t.fromAccount.user.id = :userId
                        AND
                        LOWER(t.toAccount.user.name)
                        LIKE
                        CONCAT(
                            '%',
                            CAST(:counterparty AS string),
                            '%'
                        )
                    )

                    OR

                    (
                        t.toAccount.user.id = :userId
                        AND
                        LOWER(t.fromAccount.user.name)
                        LIKE
                        CONCAT(
                            '%',
                            CAST(:counterparty AS string),
                            '%'
                        )
                    )
                )

            ORDER BY t.createdAt DESC
            """)
    List<Transaction> searchMyTransactions(
            @Param("userId") Long userId,
            @Param("direction") String direction,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("counterparty") String counterparty
    );


    // ============================================================
    // CATEGORY TRANSACTIONS
    // ============================================================

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category = :category
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findUserTransactionsByCategory(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("category") TransactionCategory category
    );


    // ============================================================
    // CATEGORY SPENDING
    //
    // TRANSFER is excluded because this query represents
    // actual spending categories.
    // ============================================================

    @Query("""
            SELECT
                t.category,
                COALESCE(SUM(t.amount), 0),
                COUNT(t.id)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category IS NOT NULL
              AND t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
            GROUP BY t.category
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> findCategorySpending(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status
    );


    @Query("""
            SELECT
                COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category = :category
              AND t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
            """)
    BigDecimal findSpendingByCategory(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("category") TransactionCategory category
    );


    @Query("""
            SELECT COUNT(t.id)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category = :category
              AND t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
            """)
    long countSpendingTransactionsByCategory(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("category") TransactionCategory category
    );


    // ============================================================
    // UNCATEGORIZED TRANSACTIONS
    // ============================================================

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE
                (
                    t.fromAccount.user.id = :userId
                    OR
                    t.toAccount.user.id = :userId
                )
            AND t.status = :status
            AND t.category IS NULL
            ORDER BY t.createdAt ASC
            """)
    List<Transaction> findUncategorizedTransactionsByUserId(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status
    );


    // ============================================================
    // CATEGORY SPENDING FOR DATE RANGE
    //
    // TRANSFER is excluded.
    // ============================================================

    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category = :category
              AND t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    BigDecimal findCategorySpendingAmountBetweenDates(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("category") TransactionCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    @Query("""
            SELECT COUNT(t.id)
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category = :category
              AND t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            """)
    long findCategorySpendingCountBetweenDates(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("category") TransactionCategory category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    // ============================================================
    // CATEGORY SPENDING BETWEEN DATES
    //
    // Used by SpendingInsightsService.
    // TRANSFER is excluded.
    // ============================================================

    @Query("""
            SELECT new com.bankcore.dto.CategorySpendingAggregate(
                t.category,
                SUM(t.amount),
                COUNT(t.id)
            )
            FROM Transaction t
            WHERE t.fromAccount.user.id = :userId
              AND t.status = :status
              AND t.category IS NOT NULL
              AND t.category <> com.bankcore.entity.TransactionCategory.TRANSFER
              AND t.createdAt >= :startDate
              AND t.createdAt < :endDate
            GROUP BY t.category
            ORDER BY SUM(t.amount) DESC
            """)
    List<com.bankcore.dto.CategorySpendingAggregate>
    findCategorySpendingBetweenDates(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}