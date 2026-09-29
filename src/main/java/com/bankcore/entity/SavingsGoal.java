package com.bankcore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "savings_goals",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_savings_goal_user_name",
                        columnNames = {
                                "user_id",
                                "name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_savings_goal_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_savings_goal_target_date",
                        columnList = "target_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // ============================================================
    // OWNER
    // ============================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;


    // ============================================================
    // GOAL INFORMATION
    // ============================================================

    @Column(
            nullable = false,
            length = 100
    )
    private String name;


    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal targetAmount;


    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal currentAmount = BigDecimal.ZERO;


    @Column(
            name = "target_date",
            nullable = false
    )
    private LocalDate targetDate;


    // ============================================================
    // CURRENCY
    // ============================================================

    @Column(
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "INR";


    // ============================================================
    // TIMESTAMPS
    // ============================================================

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