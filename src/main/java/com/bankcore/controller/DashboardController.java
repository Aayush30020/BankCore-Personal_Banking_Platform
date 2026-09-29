package com.bankcore.controller;

import com.bankcore.dto.DashboardResponse;
import com.bankcore.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;


    // ============================================================
    // GET MY DASHBOARD
    // ============================================================

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {

        return ResponseEntity.ok(
                dashboardService.getDashboard()
        );
    }
}