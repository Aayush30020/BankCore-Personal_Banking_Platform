package com.bankcore.service;

import com.bankcore.dto.CashFlowForecastResponse;
import com.bankcore.dto.CashFlowForecastResponse.ForecastStatus;
import com.bankcore.entity.Account;
import com.bankcore.entity.AccountStatus;
import com.bankcore.entity.TransactionStatus;
import com.bankcore.entity.User;
import com.bankcore.repository.AccountRepository;
import com.bankcore.repository.TransactionRepository;
import com.bankcore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CashFlowForecastService {


    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    // ============================================================
    // CASH-FLOW FORECAST
    // ============================================================

    @Transactional(readOnly = true)
    public CashFlowForecastResponse getMyCashFlowForecast() {

        User user =
                getAuthenticatedUser();

        Long userId =
                user.getId();


        // ========================================================
        // CURRENT DATE
        // ========================================================

        LocalDate today =
                LocalDate.now();


        YearMonth currentMonth =
                YearMonth.from(today);


        LocalDate periodStart =
                currentMonth.atDay(1);

        LocalDate periodEnd =
                today;


        int totalDaysInMonth =
                currentMonth.lengthOfMonth();


        int daysElapsed =
                today.getDayOfMonth();


        int daysRemaining =
                totalDaysInMonth
                        - daysElapsed;


        // ========================================================
        // CURRENT BALANCE
        // ========================================================

        BigDecimal currentBalance =
                getCurrentBalance(userId);


        // ========================================================
        // CURRENT MONTH SPENDING
        // ========================================================

        LocalDateTime startDateTime =
                periodStart.atStartOfDay();


        LocalDateTime endDateTime =
                today
                        .plusDays(1)
                        .atStartOfDay();


        BigDecimal currentMonthSpending =
                transactionRepository
                        .getTotalSentBetweenDates(
                                userId,
                                startDateTime,
                                endDateTime
                        );


        if (currentMonthSpending == null) {

            currentMonthSpending =
                    BigDecimal.ZERO;
        }


        // ========================================================
        // NO SPENDING DATA
        // ========================================================

        if (currentMonthSpending.compareTo(
                BigDecimal.ZERO
        ) == 0) {

            return new CashFlowForecastResponse(

                    currentBalance,

                    BigDecimal.ZERO,

                    BigDecimal.ZERO,

                    BigDecimal.ZERO,

                    BigDecimal.ZERO,

                    currentBalance,

                    daysElapsed,

                    daysRemaining,

                    totalDaysInMonth,

                    periodStart,

                    periodEnd,

                    ForecastStatus.NO_SPENDING_DATA
            );
        }


        // ========================================================
        // AVERAGE DAILY SPENDING
        // ========================================================

        BigDecimal averageDailySpending =
                currentMonthSpending
                        .divide(
                                BigDecimal.valueOf(
                                        daysElapsed
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // PROJECTED MONTH-END SPENDING
        // ========================================================

        BigDecimal projectedMonthEndSpending =
                averageDailySpending
                        .multiply(
                                BigDecimal.valueOf(
                                        totalDaysInMonth
                                )
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // PROJECTED REMAINING SPENDING
        // ========================================================

        BigDecimal projectedRemainingSpending =
                projectedMonthEndSpending
                        .subtract(
                                currentMonthSpending
                        );


        if (projectedRemainingSpending.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            projectedRemainingSpending =
                    BigDecimal.ZERO;
        }


        projectedRemainingSpending =
                projectedRemainingSpending
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // PROJECTED END-OF-MONTH BALANCE
        // ========================================================

        BigDecimal projectedEndOfMonthBalance =
                currentBalance
                        .subtract(
                                projectedRemainingSpending
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // ========================================================
        // FORECAST STATUS
        // ========================================================

        ForecastStatus status =
                determineStatus(
                        currentBalance,
                        projectedEndOfMonthBalance
                );


        // ========================================================
        // RESPONSE
        // ========================================================

        return new CashFlowForecastResponse(

                currentBalance,

                currentMonthSpending,

                averageDailySpending,

                projectedMonthEndSpending,

                projectedRemainingSpending,

                projectedEndOfMonthBalance,

                daysElapsed,

                daysRemaining,

                totalDaysInMonth,

                periodStart,

                periodEnd,

                status
        );
    }


    // ============================================================
    // CURRENT BALANCE
    // ============================================================

    private BigDecimal getCurrentBalance(
            Long userId
    ) {

        List<Account> accounts =
                accountRepository
                        .findByUserId(userId);


        return accounts
                .stream()
                .filter(
                        account ->
                                account.getStatus()
                                        == AccountStatus.ACTIVE
                )
                .map(Account::getBalance)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    // ============================================================
    // FORECAST STATUS
    // ============================================================

    private ForecastStatus determineStatus(
            BigDecimal currentBalance,
            BigDecimal projectedEndOfMonthBalance
    ) {

        if (projectedEndOfMonthBalance.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            return ForecastStatus
                    .PROJECTED_NEGATIVE_BALANCE;
        }


        /*
         * Consider the projected balance "low" when it is
         * 10% or less of the current available balance.
         *
         * This is only a backend status indicator.
         * It is not financial advice.
         */

        if (currentBalance.compareTo(
                BigDecimal.ZERO
        ) > 0) {

            BigDecimal tenPercentOfCurrentBalance =
                    currentBalance
                            .multiply(
                                    BigDecimal.valueOf(0.10)
                            );


            if (projectedEndOfMonthBalance
                    .compareTo(
                            tenPercentOfCurrentBalance
                    ) <= 0) {

                return ForecastStatus
                        .PROJECTED_LOW_BALANCE;
            }
        }


        return ForecastStatus
                .PROJECTED_POSITIVE_BALANCE;
    }


    // ============================================================
    // AUTHENTICATED USER
    // ============================================================

    private User getAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated user not found"
            );
        }


        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user does not exist"
                        )
                );
    }
}