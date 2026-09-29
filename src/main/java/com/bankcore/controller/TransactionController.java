package com.bankcore.controller;

import com.bankcore.dto.TransactionHistoryResponse;
import com.bankcore.service.TransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionQueryService transactionQueryService;

    // ============================================================
    // GET MY RECENT TRANSACTIONS
    // ============================================================

    @GetMapping("/recent")
    public ResponseEntity<List<TransactionHistoryResponse>>
    getMyRecentTransactions() {

        return ResponseEntity.ok(
                transactionQueryService
                        .getMyRecentTransactions(20)
        );
    }
}