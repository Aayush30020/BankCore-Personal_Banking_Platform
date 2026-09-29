package com.bankcore.service;

/**
 * Event published after a banking transaction has been created.
 *
 * The transaction ID is used instead of passing the entire
 * JPA entity because the asynchronous listener will execute
 * after the original transaction has completed.
 */
public record TransactionCategorizationEvent(
        Long transactionId
) {
}