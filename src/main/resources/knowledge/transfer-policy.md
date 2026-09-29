# BankCore Transfer Policy

BankCore allows authenticated customers to transfer money between eligible BankCore accounts.

## Transfer Requirements

A transfer can be completed only when the following conditions are satisfied:

- The sender is authenticated.
- The sender owns the source account.
- The source account is ACTIVE.
- The destination account exists.
- The destination account is eligible to receive money.
- The transfer amount is greater than zero.
- The sender has sufficient available balance.
- The transfer request passes the BankCore transaction validation rules.

## Transfer Process

A successful transfer follows these general steps:

1. The authenticated customer initiates the transfer.
2. BankCore verifies ownership of the source account.
3. BankCore validates the source and destination accounts.
4. BankCore checks the transfer amount.
5. BankCore checks whether the sender has sufficient balance.
6. BankCore locks the relevant accounts during the transfer.
7. The sender's balance is decreased by the transfer amount.
8. The recipient's balance is increased by the transfer amount.
9. A transaction record is created.
10. Corresponding ledger entries are recorded.
11. The transaction is marked as completed.

The BankCore backend is the source of truth for all transfer results.

## Insufficient Balance

If the sender does not have enough available balance, the transfer must not be completed.

The customer's balance must not become negative as a result of a transfer.

BankCore AI must not claim that a transfer succeeded unless the backend confirms it.

## Account Ownership

A customer can initiate transfers only from accounts they own.

A customer must not be able to use another customer's account as the source account.

BankCore AI must never expose another customer's account information.

## Transaction Status

A successfully completed transfer has a `COMPLETED` transaction status.

A transaction that is not completed must not be described by the AI as a successful transfer.

The backend transaction status is the source of truth.

## Duplicate Requests

BankCore supports idempotent transfer requests.

An idempotency key can be used to prevent the same transfer request from being processed multiple times.

If the same valid idempotency key is submitted again for the same transfer operation, BankCore can recognize the existing transaction instead of creating another transfer.

## Transaction Security

BankCore uses database transactions to maintain consistency during money transfers.

Account locking is used when necessary to prevent concurrent operations from incorrectly modifying balances.

The transfer operation is designed so that the balance updates and ledger records remain consistent.

## AI Restrictions

BankCore AI can explain the general transfer policy using this knowledge base.

Examples:

- "How does a BankCore transfer work?"
- "What happens during a transfer?"
- "What happens if I don't have enough balance?"
- "Can I transfer money from someone else's account?"

However, this document must not be used to determine a customer's actual financial state.

For questions such as:

- "Did my transfer succeed?"
- "What was my last transfer?"
- "How much did I transfer?"
- "What is my current balance?"

BankCore AI must use the appropriate authenticated banking tools.

BankCore AI must never invent a transfer result, balance, transaction amount, or transaction status.