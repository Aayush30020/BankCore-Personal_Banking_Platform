package com.bankcore.controller;

import com.bankcore.dto.AddMoneyRequest;
import com.bankcore.dto.AddMoneyResponse;
import com.bankcore.dto.DemoDataResponse;
import com.bankcore.service.DemoBankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo-banking")
@RequiredArgsConstructor
public class DemoBankingController {

    private final DemoBankingService demoBankingService;

    /**
     * Add simulated money to the authenticated user's account.
     *
     * POST /api/demo-banking/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<AddMoneyResponse> addMoney(
            @Valid @RequestBody AddMoneyRequest request
    ) {

        AddMoneyResponse response =
                demoBankingService.addMoney(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Generate a complete demo banking profile.
     *
     * POST /api/demo-banking/demo-data
     */
    @PostMapping("/demo-data")
    public ResponseEntity<DemoDataResponse> generateDemoData() {

        DemoDataResponse response =
                demoBankingService.generateDemoData();

        return ResponseEntity.ok(response);
    }
}