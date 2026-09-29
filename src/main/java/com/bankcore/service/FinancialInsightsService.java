package com.bankcore.service;

import com.bankcore.dto.FinancialInsightsResponse;
import com.bankcore.entity.User;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FinancialInsightsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FinancialInsightsResponse getMyFinancialInsights() {

        User user = getAuthenticatedUser();

        Long userId = user.getId();

        BigDecimal totalSent =
                transactionRepository.getTotalSentByUserId(userId);

        BigDecimal totalReceived =
                transactionRepository.getTotalReceivedByUserId(userId);

        long sentCount =
                transactionRepository.countSentByUserId(userId);

        long receivedCount =
                transactionRepository.countReceivedByUserId(userId);

        BigDecimal netFlow =
                totalReceived.subtract(totalSent);

        BigDecimal totalTransactionVolume =
                totalSent.add(totalReceived);

        return new FinancialInsightsResponse(
                totalSent,
                totalReceived,
                netFlow,
                totalTransactionVolume,
                sentCount,
                receivedCount
        );
    }

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user not found"
            );
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user does not exist"
                        )
                );
    }
}