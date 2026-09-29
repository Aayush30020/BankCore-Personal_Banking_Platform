package com.bankcore.service;

import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.entity.TransactionStatus;
import com.bankcore.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionCategoryBackfillService {

    private final TransactionRepository transactionRepository;

    private final TransactionCategorizationService
            transactionCategorizationService;


    /**
     * Categorizes all completed transactions that currently
     * do not have a category.
     *
     * This is intended for existing historical transactions
     * created before AI categorization was introduced.
     */
    @Transactional
    public int categorizeUncategorizedTransactions(
            Long userId
    ) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        List<Transaction> transactions =
                transactionRepository
                        .findUncategorizedTransactionsByUserId(
                                userId,
                                TransactionStatus.COMPLETED
                        );

        int categorizedCount = 0;

        for (Transaction transaction : transactions) {

            try {

                TransactionCategory category =
                        transactionCategorizationService.categorize(
                                transaction.getDescription()
                        );

                transaction.setCategory(category);

                transactionRepository.save(transaction);

                categorizedCount++;

            } catch (Exception e) {

                /*
                 * One AI failure should not prevent
                 * other historical transactions from
                 * being categorized.
                 */
                System.err.println(
                        "Failed to categorize transaction "
                                + transaction.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }

        return categorizedCount;
    }
}