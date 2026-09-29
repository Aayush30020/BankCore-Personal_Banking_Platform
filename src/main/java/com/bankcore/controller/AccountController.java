package com.bankcore.controller;

import com.bankcore.dto.AccountResponse;
import com.bankcore.dto.CreateAccountRequest;
import com.bankcore.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;


    // ============================================================
    // CREATE ACCOUNT
    // ============================================================

    /**
     * Create a bank account for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {

        AccountResponse response =
                accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    // ============================================================
    // GET MY ACCOUNTS
    // ============================================================

    /**
     * Get all accounts belonging to the authenticated user.
     *
     * GET /api/accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts() {

        return ResponseEntity.ok(
                accountService.getMyAccounts()
        );
    }


    // ============================================================
    // GET MY ACCOUNTS - ALIAS
    // ============================================================

    /**
     * Alias endpoint for the authenticated user's accounts.
     *
     * GET /api/accounts/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<AccountResponse>> getMyAccountsMe() {

        return ResponseEntity.ok(
                accountService.getMyAccounts()
        );
    }


    // ============================================================
    // RECIPIENT LOOKUP
    // ============================================================

    /**
     * Verify a recipient using their bank account number.
     *
     * Example:
     *
     * GET /api/accounts/lookup?accountNumber=BKDD...
     *
     * Returns only safe recipient information.
     */
    @GetMapping("/lookup")
    public ResponseEntity<AccountService.RecipientAccount>
    lookupRecipient(
            @RequestParam String accountNumber
    ) {

        return ResponseEntity.ok(
                accountService.lookupRecipient(
                        accountNumber
                )
        );
    }


    // ============================================================
    // GET SPECIFIC ACCOUNT
    // ============================================================

    /**
     * Get a specific account belonging to the
     * authenticated user.
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getMyAccount(
            @PathVariable Long accountId
    ) {

        return ResponseEntity.ok(
                accountService.getMyAccount(accountId)
        );
    }
}