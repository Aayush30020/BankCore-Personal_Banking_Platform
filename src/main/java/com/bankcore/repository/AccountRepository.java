package com.bankcore.repository;

import com.bankcore.entity.Account;
import com.bankcore.entity.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository
        extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(
            String accountNumber
    );

    /**
     * Finds an active account using its account number.
     *
     * Used for recipient verification before a transfer.
     */
    Optional<Account> findByAccountNumberAndStatus(
            String accountNumber,
            AccountStatus status
    );

    Optional<Account> findByIdAndUserId(
            Long accountId,
            Long userId
    );

    List<Account> findByUserId(
            Long userId
    );

    List<Account> findByUserIdAndStatus(
            Long userId,
            AccountStatus status
    );

    boolean existsByAccountNumber(
            String accountNumber
    );

    /**
     * Locks the account row until the current
     * database transaction completes.
     *
     * This prevents two concurrent transfers from
     * modifying the same balance at the same time.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :accountId
            """)
    Optional<Account> findByIdForUpdate(
            @Param("accountId") Long accountId
    );
}