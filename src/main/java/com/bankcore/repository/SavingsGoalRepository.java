package com.bankcore.repository;

import com.bankcore.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository
        extends JpaRepository<SavingsGoal, Long> {


    // ============================================================
    // GET ALL GOALS FOR USER
    // ============================================================

    List<SavingsGoal> findByUserIdOrderByTargetDateAsc(
            Long userId
    );


    // ============================================================
    // GET ONE GOAL
    // ============================================================

    Optional<SavingsGoal> findByUserIdAndName(
            Long userId,
            String name
    );


    // ============================================================
    // CHECK EXISTENCE
    // ============================================================

    boolean existsByUserIdAndName(
            Long userId,
            String name
    );
}