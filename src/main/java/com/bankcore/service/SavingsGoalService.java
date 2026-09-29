package com.bankcore.service;

import com.bankcore.dto.SavingsGoalAnalysisResponse;
import com.bankcore.dto.SavingsGoalRequest;
import com.bankcore.dto.SavingsGoalResponse;
import com.bankcore.entity.SavingsGoal;
import com.bankcore.entity.User;
import com.bankcore.repository.SavingsGoalRepository;
import com.bankcore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    private final UserRepository userRepository;


    // ============================================================
    // GET ALL MY SAVINGS GOALS
    // ============================================================

    @Transactional(readOnly = true)
    public List<SavingsGoalResponse> getMyGoals() {

        User user = getAuthenticatedUser();

        return savingsGoalRepository
                .findByUserIdOrderByTargetDateAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET ONE SAVINGS GOAL
    // ============================================================

    @Transactional(readOnly = true)
    public SavingsGoalResponse getMyGoal(
            String name
    ) {

        validateName(name);

        User user = getAuthenticatedUser();

        SavingsGoal goal =
                savingsGoalRepository
                        .findByUserIdAndName(
                                user.getId(),
                                name.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No savings goal found with name "
                                                + name
                                )
                        );

        return toResponse(goal);
    }


    // ============================================================
    // CREATE OR UPDATE SAVINGS GOAL
    // ============================================================

    @Transactional
    public SavingsGoalResponse saveGoal(
            SavingsGoalRequest request
    ) {

        validateRequest(request);

        User user = getAuthenticatedUser();

        String goalName =
                request.name().trim();

        SavingsGoal goal =
                savingsGoalRepository
                        .findByUserIdAndName(
                                user.getId(),
                                goalName
                        )
                        .orElseGet(() ->
                                SavingsGoal.builder()
                                        .user(user)
                                        .name(goalName)
                                        .currency("INR")
                                        .build()
                        );

        goal.setTargetAmount(
                request.targetAmount()
        );

        goal.setCurrentAmount(
                request.currentAmount()
        );

        goal.setTargetDate(
                request.targetDate()
        );

        SavingsGoal savedGoal =
                savingsGoalRepository.save(goal);

        return toResponse(savedGoal);
    }


    // ============================================================
    // DELETE SAVINGS GOAL
    // ============================================================

    @Transactional
    public void deleteGoal(
            String name
    ) {

        validateName(name);

        User user = getAuthenticatedUser();

        SavingsGoal goal =
                savingsGoalRepository
                        .findByUserIdAndName(
                                user.getId(),
                                name.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No savings goal found with name "
                                                + name
                                )
                        );

        savingsGoalRepository.delete(goal);
    }


    // ============================================================
    // ANALYZE SAVINGS GOAL
    // ============================================================

    @Transactional(readOnly = true)
    public SavingsGoalAnalysisResponse analyzeGoal(
            String name
    ) {

        validateName(name);

        User user = getAuthenticatedUser();

        SavingsGoal goal =
                savingsGoalRepository
                        .findByUserIdAndName(
                                user.getId(),
                                name.trim()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No savings goal found with name "
                                                + name
                                )
                        );

        LocalDate today =
                LocalDate.now();

        BigDecimal targetAmount =
                goal.getTargetAmount();

        BigDecimal currentAmount =
                goal.getCurrentAmount();

        BigDecimal remainingAmount =
                targetAmount
                        .subtract(currentAmount);

        BigDecimal progressPercentage =
                calculateProgressPercentage(
                        currentAmount,
                        targetAmount
                );

        long daysRemaining =
                Math.max(
                        0,
                        ChronoUnit.DAYS.between(
                                today,
                                goal.getTargetDate()
                        )
                );

        long monthsRemaining =
                calculateMonthsRemaining(
                        today,
                        goal.getTargetDate()
                );

        BigDecimal requiredDailySaving =
                calculateRequiredDailySaving(
                        remainingAmount,
                        daysRemaining
                );

        BigDecimal requiredMonthlySaving =
                calculateRequiredMonthlySaving(
                        remainingAmount,
                        monthsRemaining
                );

        String status =
                determineStatus(
                        targetAmount,
                        currentAmount,
                        today,
                        goal.getTargetDate()
                );

        return new SavingsGoalAnalysisResponse(
                goal.getId(),
                goal.getName(),
                targetAmount,
                currentAmount,
                remainingAmount,
                progressPercentage,
                goal.getTargetDate(),
                daysRemaining,
                monthsRemaining,
                requiredDailySaving,
                requiredMonthlySaving,
                status,
                goal.getCurrency()
        );
    }


    // ============================================================
    // PROGRESS CALCULATION
    // ============================================================

    private BigDecimal calculateProgressPercentage(
            BigDecimal currentAmount,
            BigDecimal targetAmount
    ) {

        if (targetAmount == null
                || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        return currentAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        targetAmount,
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // ============================================================
    // MONTH CALCULATION
    // ============================================================

    private long calculateMonthsRemaining(
            LocalDate today,
            LocalDate targetDate
    ) {

        if (!targetDate.isAfter(today)) {

            return 0;
        }

        long months =
                ChronoUnit.MONTHS.between(
                        today.withDayOfMonth(1),
                        targetDate.withDayOfMonth(1)
                );

        /*
         * If there is any remaining part of the target month,
         * count it as a saving month.
         *
         * Example:
         *
         * Today: 2026-09-29
         * Target: 2027-03-31
         *
         * Sep → Oct → Nov → Dec → Jan → Feb → Mar
         *
         * Required months = 6.
         */

        if (targetDate.getDayOfMonth()
                >= today.getDayOfMonth()) {

            return Math.max(1, months);

        }

        return Math.max(1, months);
    }


    // ============================================================
    // REQUIRED DAILY SAVING
    // ============================================================

    private BigDecimal calculateRequiredDailySaving(
            BigDecimal remainingAmount,
            long daysRemaining
    ) {

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        if (daysRemaining <= 0) {

            return remainingAmount
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return remainingAmount
                .divide(
                        BigDecimal.valueOf(daysRemaining),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // ============================================================
    // REQUIRED MONTHLY SAVING
    // ============================================================

    private BigDecimal calculateRequiredMonthlySaving(
            BigDecimal remainingAmount,
            long monthsRemaining
    ) {

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        if (monthsRemaining <= 0) {

            return remainingAmount
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );
        }

        return remainingAmount
                .divide(
                        BigDecimal.valueOf(monthsRemaining),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    // ============================================================
    // GOAL STATUS
    // ============================================================

    private String determineStatus(
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            LocalDate today,
            LocalDate targetDate
    ) {

        if (currentAmount.compareTo(targetAmount) >= 0) {

            return "COMPLETED";
        }

        if (targetDate.isBefore(today)) {

            return "DEADLINE_PASSED";
        }

        if (currentAmount.compareTo(BigDecimal.ZERO) == 0) {

            return "NOT_STARTED";
        }

        return "IN_PROGRESS";
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateRequest(
            SavingsGoalRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Savings goal request is required"
            );
        }

        validateName(request.name());

        if (request.targetAmount() == null) {

            throw new IllegalArgumentException(
                    "Target amount is required"
            );
        }

        if (request.targetAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Target amount must be greater than zero"
            );
        }

        if (request.currentAmount() == null) {

            throw new IllegalArgumentException(
                    "Current amount is required"
            );
        }

        if (request.currentAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Current amount cannot be negative"
            );
        }

        if (request.currentAmount()
                .compareTo(request.targetAmount()) > 0) {

            throw new IllegalArgumentException(
                    "Current amount cannot exceed target amount"
            );
        }

        if (request.targetDate() == null) {

            throw new IllegalArgumentException(
                    "Target date is required"
            );
        }

        if (request.targetDate()
                .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Target date cannot be in the past"
            );
        }
    }


    private void validateName(
            String name
    ) {

        if (name == null
                || name.isBlank()) {

            throw new IllegalArgumentException(
                    "Savings goal name is required"
            );
        }

        if (name.trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Savings goal name cannot exceed 100 characters"
            );
        }
    }


    // ============================================================
    // AUTHENTICATED USER
    // ============================================================

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user not found"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user does not exist"
                        )
                );
    }


    // ============================================================
    // ENTITY → RESPONSE
    // ============================================================

    private SavingsGoalResponse toResponse(
            SavingsGoal goal
    ) {

        BigDecimal targetAmount =
                goal.getTargetAmount();

        BigDecimal currentAmount =
                goal.getCurrentAmount();

        BigDecimal remainingAmount =
                targetAmount
                        .subtract(currentAmount);

        BigDecimal progressPercentage =
                calculateProgressPercentage(
                        currentAmount,
                        targetAmount
                );

        return new SavingsGoalResponse(
                goal.getId(),
                goal.getName(),
                targetAmount,
                currentAmount,
                remainingAmount,
                progressPercentage,
                goal.getTargetDate(),
                goal.getCurrency(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }
}