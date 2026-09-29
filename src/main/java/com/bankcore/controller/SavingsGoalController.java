package com.bankcore.controller;

import com.bankcore.dto.SavingsGoalAnalysisResponse;
import com.bankcore.dto.SavingsGoalRequest;
import com.bankcore.dto.SavingsGoalResponse;
import com.bankcore.service.SavingsGoalService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;


    // ============================================================
    // GET ALL MY SAVINGS GOALS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponse>> getMyGoals() {

        return ResponseEntity.ok(
                savingsGoalService.getMyGoals()
        );
    }


    // ============================================================
    // GET ONE SAVINGS GOAL
    // ============================================================

    @GetMapping("/{name}")
    public ResponseEntity<SavingsGoalResponse> getMyGoal(
            @PathVariable String name
    ) {

        return ResponseEntity.ok(
                savingsGoalService.getMyGoal(name)
        );
    }


    // ============================================================
    // ANALYZE ONE SAVINGS GOAL
    // ============================================================

    @GetMapping("/analysis/{name}")
    public ResponseEntity<SavingsGoalAnalysisResponse> analyzeGoal(
            @PathVariable String name
    ) {

        return ResponseEntity.ok(
                savingsGoalService.analyzeGoal(name)
        );
    }


    // ============================================================
    // CREATE OR UPDATE SAVINGS GOAL
    // ============================================================

    @PutMapping
    public ResponseEntity<SavingsGoalResponse> saveGoal(
            @RequestBody SavingsGoalRequest request
    ) {

        return ResponseEntity.ok(
                savingsGoalService.saveGoal(request)
        );
    }


    // ============================================================
    // DELETE SAVINGS GOAL
    // ============================================================

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteGoal(
            @PathVariable String name
    ) {

        savingsGoalService.deleteGoal(name);

        return ResponseEntity
                .noContent()
                .build();
    }
}