package com.bankcore.controller;

import com.bankcore.dto.CashFlowForecastResponse;
import com.bankcore.service.CashFlowForecastService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cash-flow")
@RequiredArgsConstructor
public class CashFlowForecastController {


    private final CashFlowForecastService cashFlowForecastService;


    // ============================================================
    // GET CASH-FLOW FORECAST
    // ============================================================

    @GetMapping("/forecast")
    public ResponseEntity<CashFlowForecastResponse>
    getMyCashFlowForecast() {

        return ResponseEntity.ok(
                cashFlowForecastService
                        .getMyCashFlowForecast()
        );
    }
}