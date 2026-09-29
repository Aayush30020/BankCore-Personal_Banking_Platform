# BankCore Transaction Status

BankCore transactions represent money transfers between BankCore accounts.

## Completed

A transaction with status `COMPLETED` has been successfully processed by the BankCore backend.

For a completed transfer:

- The sender's balance is reduced by the transfer amount.
- The recipient's balance is increased by the transfer amount.
- The transaction is recorded.
- Corresponding ledger entries are recorded.

The backend transaction record is the source of truth.

## Pending

A pending transaction represents a transaction that has not yet reached a completed state.

BankCore AI must not describe a pending transaction as successful.

The AI should rely on the backend transaction status when explaining the actual state of a customer's transaction.

## Failed

A failed transaction was not successfully completed.

A failed transaction must not be described as a successful transfer.

The customer's actual transaction status must always be obtained from the backend.

## Reversed

A reversed transaction represents a transaction whose financial effect has been reversed by the banking system.

BankCore AI must not assume that a transaction was reversed unless the backend explicitly provides that status.

## Transaction Direction

For a customer, transactions can generally be viewed in two directions.

### SENT

A `SENT` transaction means money was transferred from one of the customer's accounts to another customer's account.

Example:

Customer A sends ₹1,000 to Customer B.

For Customer A:

- Direction: `SENT`
- Amount: ₹1,000

### RECEIVED

A `RECEIVED` transaction means money was transferred into one of the customer's accounts from another customer's account.

Example:

Customer B receives ₹1,000 from Customer A.

For Customer B:

- Direction: `RECEIVED`
- Amount: ₹1,000

## Transaction History

BankCore maintains transaction records containing information such as:

- Transaction reference
- Amount
- Direction
- Transaction status
- Date and time
- Other transaction details available through the backend

When a customer asks about their actual transaction history, BankCore AI must use authenticated banking tools.

The knowledge base must never be used to invent a customer's transaction history.

## Transaction Status and AI

General questions can be answered using this knowledge document.

Examples:

- "What does completed mean?"
- "What does a pending transaction mean?"
- "What does failed mean?"
- "What is a SENT transaction?"
- "What is a RECEIVED transaction?"

Customer-specific questions must use banking tools.

Examples:

- "Was my last transaction completed?"
- "Show my recent transactions."
- "Did I receive money?"
- "How much did I send?"

The backend is always the source of truth for actual customer transactions.