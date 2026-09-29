package com.bankcore.repository;

import com.bankcore.entity.Budget;
import com.bankcore.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository
        extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserIdAndCategory(
            Long userId,
            TransactionCategory category
    );

    List<Budget> findByUserIdOrderByCategoryAsc(
            Long userId
    );

    boolean existsByUserIdAndCategory(
            Long userId,
            TransactionCategory category
    );
}