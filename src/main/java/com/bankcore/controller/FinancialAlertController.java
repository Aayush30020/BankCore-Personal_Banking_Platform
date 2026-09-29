package com.bankcore.controller;

import com.bankcore.dto.FinancialAlertResponse;
import com.bankcore.service.FinancialAlertService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/financial-alerts")
@RequiredArgsConstructor
public class FinancialAlertController {

    private final FinancialAlertService financialAlertService;


    // ============================================================
    // GET MY FINANCIAL ALERTS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<FinancialAlertResponse>>
    getMyFinancialAlerts() {

        return ResponseEntity.ok(
                financialAlertService
                        .getMyFinancialAlerts()
        );
    }
}