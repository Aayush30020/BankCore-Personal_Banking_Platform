import { useEffect, useMemo, useState } from "react";
import {
    Activity,
    ArrowDownLeft,
    ArrowUpRight,
    BarChart3,
    CheckCircle2,
    Clock3,
    RefreshCw,
    TrendingDown,
    TrendingUp,
    Wallet,
    X,
} from "lucide-react";

import api from "../api";
import "./FinancialHealthPage.css";


// ============================================================
// HELPERS
// ============================================================

const formatCurrency = (value) => {

    const amount = Number(value || 0);

    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(amount);
};


const formatDate = (value) => {

    if (!value) {
        return "—";
    }

    const date = new Date(`${value}T00:00:00`);

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
};


const formatPercentage = (value) => {

    if (
        value === null ||
        value === undefined ||
        !Number.isFinite(Number(value))
    ) {
        return "—";
    }

    return `${Number(value).toFixed(2)}%`;
};


// ============================================================
// FINANCIAL HEALTH PAGE
// ============================================================

export default function FinancialHealthPage() {

    const [data, setData] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [refreshing, setRefreshing] =
        useState(false);

    const [error, setError] =
        useState("");


    // ========================================================
    // FETCH FINANCIAL HEALTH
    // ========================================================

    const fetchFinancialHealth = async (
        showRefreshing = false
    ) => {

        try {

            if (showRefreshing) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const response =
                await api.get(
                    "/financial-health"
                );

            setData(
                response.data
            );

        } catch (err) {

            console.error(
                "Failed to load financial health:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to load your financial health summary."
            );

        } finally {

            setLoading(false);
            setRefreshing(false);
        }
    };


    useEffect(() => {

        fetchFinancialHealth();

    }, []);


    // ========================================================
    // DERIVED DISPLAY VALUES
    // ========================================================

    const comparison = useMemo(() => {

        if (!data?.spendingComparison) {
            return null;
        }

        const value =
            Number(
                data.spendingComparison.percentageChange
            );

        if (!Number.isFinite(value)) {
            return {
                value: null,
                direction: "neutral",
            };
        }

        return {
            value,
            direction:
                value > 0
                    ? "up"
                    : value < 0
                        ? "down"
                        : "neutral",
        };

    }, [data]);


    // ========================================================
    // LOADING
    // ========================================================

    if (loading) {

        return (
            <div className="financial-health-page">

                <div className="financial-health-loading">

                    <RefreshCw
                        size={26}
                        className="spin"
                    />

                    <h2>
                        Loading financial health
                    </h2>

                    <p>
                        Analyzing your latest banking activity...
                    </p>

                </div>

            </div>
        );
    }


    // ========================================================
    // ERROR WITHOUT DATA
    // ========================================================

    if (error && !data) {

        return (
            <div className="financial-health-page">

                <div className="financial-health-error-page">

                    <div className="financial-health-error-icon">
                        <X size={24} />
                    </div>

                    <h2>
                        Unable to load financial health
                    </h2>

                    <p>
                        {error}
                    </p>

                    <button
                        className="financial-health-primary-button"
                        onClick={() =>
                            fetchFinancialHealth()
                        }
                    >
                        <RefreshCw size={16} />
                        Try Again
                    </button>

                </div>

            </div>
        );
    }


    // ========================================================
    // BACKEND DATA
    // ========================================================

    const accountSummary =
        data?.accountSummary;

    const financialInsights =
        data?.financialInsights;

    const overallSpending =
        data?.overallSpending;

    const currentMonth =
        data?.currentMonthSpending;

    const previousMonth =
        data?.previousMonthSpending;

    const spendingComparison =
        data?.spendingComparison;


    // ========================================================
    // RENDER
    // ========================================================

    return (
        <div className="financial-health-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <div className="financial-health-header">

                <div>

                    <div className="financial-health-eyebrow">
                        FINANCIAL OVERVIEW
                    </div>

                    <h1>
                        Financial Health
                    </h1>

                    <p>
                        Understand your balance, cash flow,
                        spending activity, and month-over-month
                        financial movement.
                    </p>

                </div>


                <button
                    className="financial-health-refresh-button"
                    onClick={() =>
                        fetchFinancialHealth(true)
                    }
                    disabled={refreshing}
                >

                    <RefreshCw
                        size={16}
                        className={
                            refreshing
                                ? "spin"
                                : ""
                        }
                    />

                    {refreshing
                        ? "Refreshing..."
                        : "Refresh"}

                </button>

            </div>


            {/* ==================================================
                ERROR BANNER
            ================================================== */}

            {error && (

                <div className="financial-health-error">

                    <div className="financial-health-error-icon">
                        <X size={17} />
                    </div>

                    <div>

                        <strong>
                            Financial health could not be
                            fully refreshed.
                        </strong>

                        <span>
                            {error}
                        </span>

                    </div>

                    <button
                        onClick={() =>
                            setError("")
                        }
                    >
                        <X size={16} />
                    </button>

                </div>
            )}


            {/* ==================================================
                PRIMARY SUMMARY
            ================================================== */}

            <div className="financial-health-summary-grid">

                {/* TOTAL BALANCE */}

                <div className="financial-health-summary-card">

                    <div className="financial-health-summary-icon blue">
                        <Wallet size={21} />
                    </div>

                    <div>

                        <span>
                            Total Balance
                        </span>

                        <strong>
                            {formatCurrency(
                                accountSummary?.totalBalance
                            )}
                        </strong>

                        <small>
                            Across your accounts
                        </small>

                    </div>

                </div>


                {/* ACCOUNTS */}

                <div className="financial-health-summary-card">

                    <div className="financial-health-summary-icon purple">
                        <Activity size={21} />
                    </div>

                    <div>

                        <span>
                            Accounts
                        </span>

                        <strong>
                            {accountSummary?.accountCount ?? 0}
                        </strong>

                        <small>
                            Active accounts
                        </small>

                    </div>

                </div>


                {/* TOTAL SENT */}

                <div className="financial-health-summary-card">

                    <div className="financial-health-summary-icon orange">
                        <ArrowUpRight size={21} />
                    </div>

                    <div>

                        <span>
                            Total Sent
                        </span>

                        <strong>
                            {formatCurrency(
                                financialInsights?.totalSent
                            )}
                        </strong>

                        <small>
                            {financialInsights?.sentTransactionCount ?? 0}
                            {" "}
                            outgoing transactions
                        </small>

                    </div>

                </div>


                {/* TOTAL RECEIVED */}

                <div className="financial-health-summary-card">

                    <div className="financial-health-summary-icon green">
                        <ArrowDownLeft size={21} />
                    </div>

                    <div>

                        <span>
                            Total Received
                        </span>

                        <strong>
                            {formatCurrency(
                                financialInsights?.totalReceived
                            )}
                        </strong>

                        <small>
                            {financialInsights?.receivedTransactionCount ?? 0}
                            {" "}
                            incoming transactions
                        </small>

                    </div>

                </div>

            </div>


            {/* ==================================================
                CASH FLOW
            ================================================== */}

            <div className="financial-health-section">

                <div className="financial-health-section-header">

                    <div>

                        <h2>
                            Cash Flow
                        </h2>

                        <p>
                            Backend-calculated movement of money
                            through your accounts.
                        </p>

                    </div>

                    <div className="financial-health-section-icon">
                        <Activity size={19} />
                    </div>

                </div>


                <div className="financial-health-cash-grid">

                    <div className="financial-health-cash-card">

                        <div className="cash-card-icon sent">
                            <ArrowUpRight size={19} />
                        </div>

                        <div>

                            <span>
                                Money Sent
                            </span>

                            <strong>
                                {formatCurrency(
                                    financialInsights?.totalSent
                                )}
                            </strong>

                        </div>

                    </div>


                    <div className="financial-health-cash-card">

                        <div className="cash-card-icon received">
                            <ArrowDownLeft size={19} />
                        </div>

                        <div>

                            <span>
                                Money Received
                            </span>

                            <strong>
                                {formatCurrency(
                                    financialInsights?.totalReceived
                                )}
                            </strong>

                        </div>

                    </div>


                    <div className="financial-health-cash-card">

                        <div className="cash-card-icon flow">
                            <TrendingUp size={19} />
                        </div>

                        <div>

                            <span>
                                Net Flow
                            </span>

                            <strong>
                                {formatCurrency(
                                    financialInsights?.netFlow
                                )}
                            </strong>

                        </div>

                    </div>


                    <div className="financial-health-cash-card">

                        <div className="cash-card-icon volume">
                            <BarChart3 size={19} />
                        </div>

                        <div>

                            <span>
                                Transaction Volume
                            </span>

                            <strong>
                                {formatCurrency(
                                    financialInsights?.totalTransactionVolume
                                )}
                            </strong>

                        </div>

                    </div>

                </div>

            </div>


            {/* ==================================================
                OVERALL SPENDING
            ================================================== */}

            <div className="financial-health-section">

                <div className="financial-health-section-header">

                    <div>

                        <h2>
                            Overall Spending
                        </h2>

                        <p>
                            Summary of your available completed
                            spending activity.
                        </p>

                    </div>

                    <div className="financial-health-section-icon">
                        <BarChart3 size={19} />
                    </div>

                </div>


                <div className="financial-health-spending-grid">

                    <div className="financial-health-spending-main">

                        <span>
                            Total Spent
                        </span>

                        <strong>
                            {formatCurrency(
                                overallSpending?.totalSpent
                            )}
                        </strong>

                        <small>
                            {overallSpending?.transactionCount ?? 0}
                            {" "}
                            transactions
                        </small>

                    </div>


                    <div className="financial-health-metric-card">

                        <span>
                            Average Transaction
                        </span>

                        <strong>
                            {formatCurrency(
                                overallSpending?.averageTransactionAmount
                            )}
                        </strong>

                    </div>


                    <div className="financial-health-metric-card">

                        <span>
                            Largest Transaction
                        </span>

                        <strong>
                            {formatCurrency(
                                overallSpending?.largestTransactionAmount
                            )}
                        </strong>

                    </div>


                    <div className="financial-health-metric-card">

                        <span>
                            Smallest Transaction
                        </span>

                        <strong>
                            {formatCurrency(
                                overallSpending?.smallestTransactionAmount
                            )}
                        </strong>

                    </div>


                    <div className="financial-health-metric-card">

                        <span>
                            Net Spending
                        </span>

                        <strong>
                            {formatCurrency(
                                overallSpending?.netSpending
                            )}
                        </strong>

                    </div>

                </div>

            </div>


            {/* ==================================================
                MONTHLY SPENDING
            ================================================== */}

            <div className="financial-health-period-grid">

                {/* CURRENT MONTH */}

                <div className="financial-health-period-card">

                    <div className="period-card-header">

                        <div>

                            <span>
                                CURRENT PERIOD
                            </span>

                            <h3>
                                Current Month
                            </h3>

                        </div>

                        <div className="period-icon current">
                            <Clock3 size={18} />
                        </div>

                    </div>


                    <div className="period-date">

                        {formatDate(
                            currentMonth?.startDate
                        )}

                        {" — "}

                        {formatDate(
                            currentMonth?.endDate
                        )}

                    </div>


                    <div className="period-total">

                        <span>
                            Total Spent
                        </span>

                        <strong>
                            {formatCurrency(
                                currentMonth?.analysis?.totalSpent
                            )}
                        </strong>

                    </div>


                    <div className="period-stats">

                        <div>

                            <span>
                                Transactions
                            </span>

                            <strong>
                                {
                                    currentMonth
                                        ?.analysis
                                        ?.transactionCount ??
                                    0
                                }
                            </strong>

                        </div>


                        <div>

                            <span>
                                Average
                            </span>

                            <strong>
                                {formatCurrency(
                                    currentMonth
                                        ?.analysis
                                        ?.averageTransactionAmount
                                )}
                            </strong>

                        </div>

                    </div>


                    <div className="period-details">

                        <div>

                            <span>
                                Largest
                            </span>

                            <strong>
                                {formatCurrency(
                                    currentMonth
                                        ?.analysis
                                        ?.largestTransactionAmount
                                )}
                            </strong>

                        </div>


                        <div>

                            <span>
                                Smallest
                            </span>

                            <strong>
                                {formatCurrency(
                                    currentMonth
                                        ?.analysis
                                        ?.smallestTransactionAmount
                                )}
                            </strong>

                        </div>

                    </div>

                </div>


                {/* PREVIOUS MONTH */}

                <div className="financial-health-period-card">

                    <div className="period-card-header">

                        <div>

                            <span>
                                PREVIOUS PERIOD
                            </span>

                            <h3>
                                Previous Month
                            </h3>

                        </div>

                        <div className="period-icon previous">
                            <Clock3 size={18} />
                        </div>

                    </div>


                    <div className="period-date">

                        {formatDate(
                            previousMonth?.startDate
                        )}

                        {" — "}

                        {formatDate(
                            previousMonth?.endDate
                        )}

                    </div>


                    <div className="period-total">

                        <span>
                            Total Spent
                        </span>

                        <strong>
                            {formatCurrency(
                                previousMonth?.analysis?.totalSpent
                            )}
                        </strong>

                    </div>


                    <div className="period-stats">

                        <div>

                            <span>
                                Transactions
                            </span>

                            <strong>
                                {
                                    previousMonth
                                        ?.analysis
                                        ?.transactionCount ??
                                    0
                                }
                            </strong>

                        </div>


                        <div>

                            <span>
                                Average
                            </span>

                            <strong>
                                {formatCurrency(
                                    previousMonth
                                        ?.analysis
                                        ?.averageTransactionAmount
                                )}
                            </strong>

                        </div>

                    </div>


                    <div className="period-details">

                        <div>

                            <span>
                                Largest
                            </span>

                            <strong>
                                {formatCurrency(
                                    previousMonth
                                        ?.analysis
                                        ?.largestTransactionAmount
                                )}
                            </strong>

                        </div>


                        <div>

                            <span>
                                Smallest
                            </span>

                            <strong>
                                {formatCurrency(
                                    previousMonth
                                        ?.analysis
                                        ?.smallestTransactionAmount
                                )}
                            </strong>

                        </div>

                    </div>

                </div>

            </div>


            {/* ==================================================
                SPENDING COMPARISON
            ================================================== */}

            <div className="financial-health-section">

                <div className="financial-health-section-header">

                    <div>

                        <h2>
                            Spending Comparison
                        </h2>

                        <p>
                            Backend-calculated comparison between
                            the current and previous periods.
                        </p>

                    </div>

                    <div className="financial-health-section-icon">
                        {comparison?.direction === "down" ? (
                            <TrendingDown size={19} />
                        ) : (
                            <TrendingUp size={19} />
                        )}
                    </div>

                </div>


                <div className="comparison-card">

                    <div className="comparison-period">

                        <span>
                            Current Period
                        </span>

                        <strong>
                            {formatCurrency(
                                spendingComparison?.currentPeriodSpent
                            )}
                        </strong>

                        <small>
                            {formatDate(
                                spendingComparison?.currentPeriodStart
                            )}
                            {" — "}
                            {formatDate(
                                spendingComparison?.currentPeriodEnd
                            )}
                        </small>

                        <small>
                            {
                                spendingComparison
                                    ?.currentPeriodTransactionCount ??
                                0
                            }
                            {" "}
                            transactions
                        </small>

                    </div>


                    <div className="comparison-center">

                        <div
                            className={`comparison-change ${
                                comparison?.direction || "neutral"
                            }`}
                        >

                            {comparison?.direction === "down" ? (
                                <TrendingDown size={18} />
                            ) : comparison?.direction === "up" ? (
                                <TrendingUp size={18} />
                            ) : (
                                <Activity size={18} />
                            )}

                            <strong>
                                {formatPercentage(
                                    spendingComparison?.percentageChange
                                )}
                            </strong>

                        </div>

                        <span>
                            Period change
                        </span>

                        <strong className="comparison-difference">
                            {formatCurrency(
                                spendingComparison?.difference
                            )}
                        </strong>

                    </div>


                    <div className="comparison-period">

                        <span>
                            Previous Period
                        </span>

                        <strong>
                            {formatCurrency(
                                spendingComparison?.previousPeriodSpent
                            )}
                        </strong>

                        <small>
                            {formatDate(
                                spendingComparison?.previousPeriodStart
                            )}
                            {" — "}
                            {formatDate(
                                spendingComparison?.previousPeriodEnd
                            )}
                        </small>

                        <small>
                            {
                                spendingComparison
                                    ?.previousPeriodTransactionCount ??
                                0
                            }
                            {" "}
                            transactions
                        </small>

                    </div>

                </div>

            </div>


            {/* ==================================================
                FOOTER STATUS
            ================================================== */}

            <div className="financial-health-footer">

                <CheckCircle2 size={17} />

                <div>

                    <strong>
                        Financial health summary generated
                        from your BankCore data
                    </strong>

                    <span>
                        Financial values and comparisons shown
                        above are provided by the BankCore backend.
                    </span>

                </div>

            </div>

        </div>
    );
}