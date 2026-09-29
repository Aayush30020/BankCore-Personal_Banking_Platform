# BankCore Account Types

BankCore provides customers with different types of bank accounts.

## Savings Account

A Savings Account is designed for customers who want to keep money safely in their account while maintaining access to their funds.

Key characteristics:

- Suitable for personal banking.
- Customers can deposit and withdraw money.
- Customers can transfer money to other BankCore accounts.
- The account has a current available balance.
- Transactions performed on the account are recorded in the BankCore transaction history.
- The account can be ACTIVE, BLOCKED, or CLOSED depending on its status.

## Current Account

A Current Account is designed primarily for frequent financial transactions.

Key characteristics:

- Suitable for customers who perform frequent transfers.
- Customers can maintain an available balance.
- Customers can transfer money to other eligible BankCore accounts.
- Transactions are recorded in the transaction history.
- The account status determines whether transactions are permitted.

## Account Balance

The account balance represents the amount of money currently available in the account.

When a successful transfer is made:

- The sender's account balance decreases by the transfer amount.
- The recipient's account balance increases by the transfer amount.

The BankCore backend is the source of truth for account balances.

BankCore AI must never invent or estimate an account balance.

When a customer asks about their actual account balance, the banking account tool must be used instead of relying on this document.

## Account Status

BankCore accounts can have the following statuses:

### ACTIVE

An ACTIVE account can normally be used for permitted banking operations.

### BLOCKED

A BLOCKED account cannot be used for operations that require an active account.

### CLOSED

A CLOSED account is no longer available for normal banking operations.

## Account Ownership

Every BankCore account belongs to a specific authenticated customer.

Customers can access financial information only for accounts they own.

BankCore AI must never disclose another customer's account information.

## Account Information and AI

General questions about account types can be answered using the BankCore knowledge base.

Examples:

- "What is a savings account?"
- "What is a current account?"
- "What account statuses exist in BankCore?"
- "What does an active account mean?"

Questions about a customer's actual account must use authenticated banking tools.

Examples:

- "What is my balance?"
- "Show my accounts."
- "What is my account number?"

The AI must use backend banking tools for these questions.