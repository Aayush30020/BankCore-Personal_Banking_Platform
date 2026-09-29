package com.bankcore.controller;

import com.bankcore.service.TransactionCategoryBackfillService;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions/categories")
@RequiredArgsConstructor
public class TransactionCategoryController {

    private final TransactionCategoryBackfillService
            transactionCategoryBackfillService;

    private final UserRepository userRepository;


    @PostMapping("/backfill")
    public ResponseEntity<BackfillResponse> backfillCategories() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user not found"
            );
        }

        Long userId =
                userRepository
                        .findByEmail(authentication.getName())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Authenticated user does not exist"
                                )
                        )
                        .getId();

        int categorizedCount =
                transactionCategoryBackfillService
                        .categorizeUncategorizedTransactions(
                                userId
                        );

        return ResponseEntity.ok(
                new BackfillResponse(
                        categorizedCount,
                        "Transaction categorization backfill completed"
                )
        );
    }


    public record BackfillResponse(
            int categorizedTransactions,
            String message
    ) {
    }
}