package com.bankcore.controller;

import com.bankcore.dto.RecurringExpenseResponse;
import com.bankcore.service.RecurringExpenseService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-expenses")
@RequiredArgsConstructor
public class RecurringExpenseController {

    private final RecurringExpenseService
            recurringExpenseService;


    // ============================================================
    // GET MY RECURRING EXPENSES
    // ============================================================

    @GetMapping
    public ResponseEntity<List<RecurringExpenseResponse>>
    getMyRecurringExpenses() {

        return ResponseEntity.ok(
                recurringExpenseService
                        .getMyRecurringExpenses()
        );
    }
}