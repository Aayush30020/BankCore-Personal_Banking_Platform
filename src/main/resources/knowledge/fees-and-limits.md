# BankCore Fees and Transfer Limits

This document describes the general fee and transfer-limit policy used by the BankCore demo application.

## Transfer Fees

The current BankCore demo transfer flow does not apply a separate transfer fee.

The amount specified in a successful transfer is the amount deducted from the sender's account and credited to the recipient's account.

For example:

If a customer transfers ₹1,000:

- Sender is debited ₹1,000.
- Recipient is credited ₹1,000.
- No additional transfer fee is applied by the current BankCore demo implementation.

## Transfer Limits

The current BankCore demo application does not define a separate daily transfer-limit system.

Transfer validation is primarily based on:

- Account ownership.
- Account status.
- Valid transfer amount.
- Destination account availability.
- Sufficient available balance.
- Other backend validation rules.

BankCore AI must not invent a daily, monthly, or per-transaction limit that is not defined by the backend or this knowledge base.

## Minimum Transfer Amount

A transfer amount must be greater than zero.

A zero or negative transfer amount is not a valid money transfer.

## Sufficient Balance

A customer must have sufficient available balance to complete a transfer.

For example:

If the account balance is ₹1,000 and the customer attempts to transfer ₹1,500, the transfer cannot be completed because the available balance is insufficient.

BankCore AI must use the backend when determining a customer's actual balance.

## Currency

BankCore financial amounts are expressed in Indian Rupees (INR) unless another currency is explicitly provided by the application.

The currency symbol used for INR is ₹.

## AI Restrictions

BankCore AI can use this document to answer general questions such as:

- "Does BankCore charge a transfer fee?"
- "Is there a transfer limit?"
- "Can I transfer zero rupees?"
- "What happens if I don't have enough balance?"
- "What currency does BankCore use?"

BankCore AI must not invent fees or limits.

For questions involving a customer's actual account balance or a specific transaction, the banking backend is the source of truth.

For example:

Customer question:

"Can I transfer ₹5,000 right now?"

The AI should use the customer's actual account information and the appropriate banking tools before making a statement about whether the transfer can be completed.

Customer question:

"How much did my last transfer cost?"

The AI should use the customer's actual transaction information rather than relying only on this document.