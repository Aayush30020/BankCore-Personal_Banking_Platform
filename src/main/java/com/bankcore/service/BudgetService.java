package com.bankcore.service;

import com.bankcore.dto.BudgetRequest;
import com.bankcore.dto.BudgetResponse;
import com.bankcore.entity.Budget;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.User;
import com.bankcore.repository.BudgetRepository;
import com.bankcore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    private final UserRepository userRepository;


    // ============================================================
    // GET ALL MY BUDGETS
    // ============================================================

    @Transactional(readOnly = true)
    public List<BudgetResponse> getMyBudgets() {

        User user = getAuthenticatedUser();

        return budgetRepository
                .findByUserIdOrderByCategoryAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }


    // ============================================================
    // GET BUDGET BY CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    public BudgetResponse getMyBudget(
            TransactionCategory category
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Budget category is required"
            );
        }

        User user = getAuthenticatedUser();

        Budget budget =
                budgetRepository
                        .findByUserIdAndCategory(
                                user.getId(),
                                category
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No budget found for category "
                                                + category
                                )
                        );

        return toResponse(budget);
    }


    // ============================================================
    // CREATE OR UPDATE BUDGET
    // ============================================================

    @Transactional
    public BudgetResponse saveBudget(
            BudgetRequest request
    ) {

        validateRequest(request);

        User user = getAuthenticatedUser();

        Budget budget =
                budgetRepository
                        .findByUserIdAndCategory(
                                user.getId(),
                                request.category()
                        )
                        .orElseGet(() ->
                                Budget.builder()
                                        .user(user)
                                        .category(
                                                request.category()
                                        )
                                        .currency("INR")
                                        .build()
                        );

        budget.setMonthlyLimit(
                request.monthlyLimit()
        );

        Budget savedBudget =
                budgetRepository.save(budget);

        return toResponse(savedBudget);
    }


    // ============================================================
    // DELETE BUDGET
    // ============================================================

    @Transactional
    public void deleteBudget(
            TransactionCategory category
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Budget category is required"
            );
        }

        User user = getAuthenticatedUser();

        Budget budget =
                budgetRepository
                        .findByUserIdAndCategory(
                                user.getId(),
                                category
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No budget found for category "
                                                + category
                                )
                        );

        budgetRepository.delete(budget);
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private void validateRequest(
            BudgetRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Budget request is required"
            );
        }

        if (request.category() == null) {
            throw new IllegalArgumentException(
                    "Budget category is required"
            );
        }

        if (request.monthlyLimit() == null) {
            throw new IllegalArgumentException(
                    "Monthly budget limit is required"
            );
        }

        if (request.monthlyLimit()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Monthly budget limit must be greater than zero"
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
    // ENTITY → DTO
    // ============================================================

    private BudgetResponse toResponse(
            Budget budget
    ) {

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory(),
                budget.getMonthlyLimit(),
                budget.getCurrency(),
                budget.getCreatedAt(),
                budget.getUpdatedAt()
        );
    }
}