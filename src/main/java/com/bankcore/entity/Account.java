package com.bankcore.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(
                        name = "idx_account_number",
                        columnList = "account_number"
                ),
                @Index(
                        name = "idx_account_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "account_number",
            nullable = false,
            unique = true,
            length = 20
    )
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "account_type",
            nullable = false,
            length = 20
    )
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    @Builder.Default
    private AccountStatus status =
            AccountStatus.ACTIVE;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal balance =
            BigDecimal.ZERO;

    @Column(
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "INR";

    @Version
    private Long version;

    @Column(
            nullable = false,
            updatable = false
    )
    @Builder.Default
    private LocalDateTime createdAt =
            LocalDateTime.now();
}