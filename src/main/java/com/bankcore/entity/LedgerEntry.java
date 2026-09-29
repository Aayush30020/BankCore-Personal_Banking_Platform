package com.bankcore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(
                        name = "idx_ledger_transaction",
                        columnList = "transaction_id"
                ),
                @Index(
                        name = "idx_ledger_account",
                        columnList = "account_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Transaction to which this ledger entry belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "transaction_id",
            nullable = false
    )
    private Transaction transaction;

    /**
     * Account affected by this ledger entry.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false
    )
    private Account account;

    /**
     * DEBIT or CREDIT.
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "entry_type",
            nullable = false,
            length = 10
    )
    private LedgerEntryType entryType;

    /**
     * Amount represented by this ledger entry.
     */
    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            updatable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();
}