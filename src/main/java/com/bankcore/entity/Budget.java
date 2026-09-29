package com.bankcore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "budgets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_budget_user_category",
                        columnNames = {
                                "user_id",
                                "category"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_budget_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_budget_category",
                        columnList = "category"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TransactionCategory category;

    @Column(
            name = "monthly_limit",
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal monthlyLimit;

    @Column(
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "INR";

    @Column(
            nullable = false,
            updatable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt =
            LocalDateTime.now();

    @PreUpdate
    public void updateTimestamp() {

        updatedAt = LocalDateTime.now();
    }
}