package com.bankcore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(
                        name = "idx_transaction_reference",
                        columnList = "transaction_reference"
                ),
                @Index(
                        name = "idx_transaction_from_account",
                        columnList = "from_account_id"
                ),
                @Index(
                        name = "idx_transaction_to_account",
                        columnList = "to_account_id"
                ),
                @Index(
                        name = "idx_transaction_idempotency",
                        columnList = "idempotency_key"
                ),
                @Index(
                        name = "idx_transaction_category",
                        columnList = "category"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public transaction reference.
     *
     * Example:
     * TXN-8F7A2C...
     */
    @Column(
            name = "transaction_reference",
            nullable = false,
            unique = true,
            length = 40
    )
    private String transactionReference;

    /**
     * Account from which money is debited.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "from_account_id",
            nullable = false
    )
    private Account fromAccount;

    /**
     * Account to which money is credited.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "to_account_id",
            nullable = false
    )
    private Account toAccount;

    /**
     * Amount transferred.
     */
    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    /**
     * Currency of the transaction.
     */
    @Column(
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "INR";

    /**
     * Current state of the transaction.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private TransactionStatus status =
            TransactionStatus.PENDING;

    /**
     * AI-generated transaction category.
     *
     * This field is intentionally nullable because
     * transaction categorization happens after the
     * financial transaction is completed.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            length = 30
    )
    private TransactionCategory category;

    /**
     * Prevents duplicate processing of
     * the same client request.
     */
    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String idempotencyKey;

    /**
     * Optional description supplied by the user.
     */
    @Column(length = 255)
    private String description;

    /**
     * Creation timestamp.
     */
    @Column(
            nullable = false,
            updatable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();

    /**
     * Timestamp when transaction completed.
     */
    private LocalDateTime completedAt;

    /**
     * Optimistic locking.
     *
     * Useful when multiple requests attempt
     * to modify the same transaction.
     */
    @Version
    private Long version;
}