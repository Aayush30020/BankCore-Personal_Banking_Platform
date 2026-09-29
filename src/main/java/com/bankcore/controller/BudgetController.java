package com.bankcore.controller;

import com.bankcore.dto.BudgetAnalysisResponse;
import com.bankcore.dto.BudgetRequest;
import com.bankcore.dto.BudgetResponse;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.service.BudgetAnalysisService;
import com.bankcore.service.BudgetService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    private final BudgetAnalysisService budgetAnalysisService;


    // ============================================================
    // GET ALL MY BUDGETS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getMyBudgets() {

        return ResponseEntity.ok(
                budgetService.getMyBudgets()
        );
    }


    // ============================================================
    // GET BUDGET BY CATEGORY
    // ============================================================

    @GetMapping("/{category}")
    public ResponseEntity<BudgetResponse> getMyBudget(
            @PathVariable TransactionCategory category
    ) {

        return ResponseEntity.ok(
                budgetService.getMyBudget(category)
        );
    }


    // ============================================================
    // CREATE OR UPDATE BUDGET
    // ============================================================

    @PutMapping
    public ResponseEntity<BudgetResponse> saveBudget(
            @RequestBody BudgetRequest request
    ) {

        return ResponseEntity.ok(
                budgetService.saveBudget(request)
        );
    }


    // ============================================================
    // DELETE BUDGET
    // ============================================================

    @DeleteMapping("/{category}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable TransactionCategory category
    ) {

        budgetService.deleteBudget(category);

        return ResponseEntity
                .noContent()
                .build();
    }


    // ============================================================
    // BUDGET ANALYSIS - ALL BUDGETS
    // ============================================================

    @GetMapping("/analysis")
    public ResponseEntity<List<BudgetAnalysisResponse>>
    getMyBudgetAnalysis() {

        return ResponseEntity.ok(
                budgetAnalysisService
                        .getMyBudgetAnalysis()
        );
    }


    // ============================================================
    // BUDGET ANALYSIS - ONE CATEGORY
    // ============================================================

    @GetMapping("/analysis/{category}")
    public ResponseEntity<BudgetAnalysisResponse>
    getMyBudgetAnalysis(
            @PathVariable TransactionCategory category
    ) {

        return ResponseEntity.ok(
                budgetAnalysisService
                        .getMyBudgetAnalysis(category)
        );
    }
}