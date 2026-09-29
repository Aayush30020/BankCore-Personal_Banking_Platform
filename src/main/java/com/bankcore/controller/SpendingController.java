package com.bankcore.controller;

import com.bankcore.dto.CategorySpendingResponse;
import com.bankcore.dto.SpendingAnalysisResponse;
import com.bankcore.service.CategorySpendingService;
import com.bankcore.service.SpendingAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spending")
@RequiredArgsConstructor
public class SpendingController {

    private final SpendingAnalysisService spendingAnalysisService;

    private final CategorySpendingService categorySpendingService;


    // ============================================================
    // OVERALL SPENDING ANALYSIS
    // ============================================================

    @GetMapping("/analysis")
    public ResponseEntity<SpendingAnalysisResponse>
    getSpendingAnalysis() {

        return ResponseEntity.ok(
                spendingAnalysisService
                        .getSpendingAnalysis()
        );
    }


    // ============================================================
    // CATEGORY-WISE SPENDING
    // ============================================================

    @GetMapping("/categories")
    public ResponseEntity<CategorySpendingResponse>
    getCategorySpending() {

        return ResponseEntity.ok(
                categorySpendingService
                        .getMyCategorySpending()
        );
    }
}