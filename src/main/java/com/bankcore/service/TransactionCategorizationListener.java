package com.bankcore.service;

import com.bankcore.entity.Transaction;
import com.bankcore.entity.TransactionCategory;
import com.bankcore.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TransactionCategorizationListener {

    private final TransactionRepository transactionRepository;
    private final TransactionCategorizationService categorizationService;

    @Async
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleTransactionCreated(
            TransactionCategorizationEvent event
    ) {

        Transaction transaction =
                transactionRepository
                        .findById(event.transactionId())
                        .orElse(null);

        if (transaction == null) {
            return;
        }

        if (transaction.getDescription() == null
                || transaction.getDescription().isBlank()) {

            transaction.setCategory(
                    TransactionCategory.OTHER
            );

            transactionRepository.save(transaction);
            return;
        }

        try {

            TransactionCategory category =
                    categorizationService.categorize(
                            transaction.getDescription()
                    );

            transaction.setCategory(category);

            transactionRepository.save(transaction);

        } catch (Exception e) {

            /*
             * AI categorization is non-critical.
             *
             * The financial transaction has already
             * been committed successfully, so an AI
             * failure must never roll it back.
             */
            System.err.println(
                    "Failed to categorize transaction "
                            + transaction.getId()
                            + ": "
                            + e.getMessage()
            );
        }
    }
}