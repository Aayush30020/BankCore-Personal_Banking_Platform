package com.bankcore.controller;

import com.bankcore.dto.FinancialHealthSummaryResponse;
import com.bankcore.service.FinancialHealthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/financial-health")
@RequiredArgsConstructor
public class FinancialHealthController {

    private final FinancialHealthService financialHealthService;


    // ============================================================
    // GET MY FINANCIAL HEALTH
    // ============================================================

    @GetMapping
    public ResponseEntity<FinancialHealthSummaryResponse>
    getFinancialHealth() {

        return ResponseEntity.ok(
                financialHealthService
                        .getFinancialHealthSummary()
        );
    }
}