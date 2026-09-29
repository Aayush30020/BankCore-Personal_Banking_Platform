import { useEffect, useMemo, useState } from "react";
import {
    ArrowDownLeft,
    ArrowUpRight,
    CheckCircle2,
    Clock3,
    CreditCard,
    Search,
    XCircle,
    RefreshCw,
    Copy,
} from "lucide-react";

import api from "../api";
import "./TransactionsPage.css";


// ============================================================
// HELPERS
// ============================================================

const formatCurrency = (amount, currency = "INR") => {
    const numericAmount = Number(amount || 0);

    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency,
        maximumFractionDigits: 2,
    }).format(numericAmount);
};


const formatDate = (dateValue) => {
    if (!dateValue) {
        return "—";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return "—";
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });
};


const formatTime = (dateValue) => {
    if (!dateValue) {
        return "";
    }

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleTimeString("en-IN", {
        hour: "2-digit",
        minute: "2-digit",
    });
};


const shortenAccount = (accountNumber) => {
    if (!accountNumber) {
        return "Unknown account";
    }

    if (accountNumber.length <= 12) {
        return accountNumber;
    }

    return `${accountNumber.slice(0, 6)}••••${accountNumber.slice(-4)}`;
};


// ============================================================
// COMPONENT
// ============================================================

export default function TransactionsPage() {

    const [transactions, setTransactions] = useState([]);

    const [loading, setLoading] = useState(true);

    const [refreshing, setRefreshing] = useState(false);

    const [error, setError] = useState("");

    const [searchTerm, setSearchTerm] = useState("");

    const [directionFilter, setDirectionFilter] =
        useState("ALL");

    const [statusFilter, setStatusFilter] =
        useState("ALL");


    // ========================================================
    // FETCH TRANSACTIONS
    // ========================================================

    const fetchTransactions = async (
        showRefreshLoader = false
    ) => {

        try {

            if (showRefreshLoader) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const response =
                await api.get("/transactions/recent");

            setTransactions(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Failed to load transactions:",
                err
            );

            setError(
                err.response?.data?.message ||
                "Unable to load transactions."
            );

        } finally {

            setLoading(false);
            setRefreshing(false);
        }
    };


    // ========================================================
    // INITIAL LOAD
    // ========================================================

    useEffect(() => {
        fetchTransactions();
    }, []);


    // ========================================================
    // FILTER TRANSACTIONS
    // ========================================================

    const filteredTransactions = useMemo(() => {

        const search =
            searchTerm
                .trim()
                .toLowerCase();

        return transactions.filter(
            (transaction) => {

                const matchesDirection =
                    directionFilter === "ALL" ||
                    transaction.direction ===
                    directionFilter;

                const matchesStatus =
                    statusFilter === "ALL" ||
                    transaction.status ===
                    statusFilter;

                const searchableText = [
                    transaction.transactionReference,
                    transaction.description,
                    transaction.counterpartyAccountNumber,
                    transaction.direction,
                    transaction.status,
                ]
                    .filter(Boolean)
                    .join(" ")
                    .toLowerCase();

                const matchesSearch =
                    !search ||
                    searchableText.includes(search);

                return (
                    matchesDirection &&
                    matchesStatus &&
                    matchesSearch
                );
            }
        );

    }, [
        transactions,
        searchTerm,
        directionFilter,
        statusFilter,
    ]);


    // ========================================================
    // SUMMARY
    // ========================================================

    const summary = useMemo(() => {

        let sent = 0;
        let received = 0;

        let sentCount = 0;
        let receivedCount = 0;

        transactions.forEach(
            (transaction) => {

                const amount =
                    Number(
                        transaction.amount || 0
                    );

                if (
                    transaction.direction ===
                    "SENT"
                ) {

                    sent += amount;
                    sentCount++;

                } else if (
                    transaction.direction ===
                    "RECEIVED"
                ) {

                    received += amount;
                    receivedCount++;
                }
            }
        );

        return {
            sent,
            received,
            sentCount,
            receivedCount,
            total: transactions.length,
        };

    }, [transactions]);


    // ========================================================
    // COPY REFERENCE
    // ========================================================

    const copyReference = async (reference) => {

        if (!reference) {
            return;
        }

        try {

            await navigator.clipboard.writeText(
                reference
            );

        } catch (error) {

            console.error(
                "Unable to copy reference:",
                error
            );
        }
    };


    // ========================================================
    // STATUS ICON
    // ========================================================

    const renderStatusIcon = (status) => {

        switch (status) {

            case "COMPLETED":

                return (
                    <CheckCircle2 size={14} />
                );

            case "PENDING":

                return (
                    <Clock3 size={14} />
                );

            case "FAILED":
            case "CANCELLED":

                return (
                    <XCircle size={14} />
                );

            default:

                return (
                    <Clock3 size={14} />
                );
        }
    };


    // ========================================================
    // RENDER
    // ========================================================

    return (
        <div className="transactions-page">

            {/* ==================================================
                PAGE HEADER
            ================================================== */}

            <div className="transactions-header">

                <div className="transactions-heading">

                    <div className="transactions-eyebrow">
                        TRANSACTION HISTORY
                    </div>

                    <h1>
                        Your Transactions
                    </h1>

                    <p>
                        View and track your recent
                        BankCore transactions.
                    </p>

                </div>

                <button
                    className="transactions-refresh-button"
                    onClick={() =>
                        fetchTransactions(true)
                    }
                    disabled={refreshing}
                >

                    <RefreshCw
                        size={17}
                        className={
                            refreshing
                                ? "refresh-spinning"
                                : ""
                        }
                    />

                    {refreshing
                        ? "Refreshing..."
                        : "Refresh"}

                </button>

            </div>


            {/* ==================================================
                SUMMARY CARDS
            ================================================== */}

            <div className="transaction-summary-grid">

                <div className="transaction-summary-card sent-card">

                    <div className="summary-icon sent">
                        <ArrowUpRight size={19} />
                    </div>

                    <div className="summary-content">

                        <span>
                            Money Sent
                        </span>

                        <strong>
                            {formatCurrency(summary.sent)}
                        </strong>

                        <small>
                            {summary.sentCount}{" "}
                            transaction
                            {summary.sentCount !== 1
                                ? "s"
                                : ""}
                        </small>

                    </div>

                </div>


                <div className="transaction-summary-card received-card">

                    <div className="summary-icon received">
                        <ArrowDownLeft size={19} />
                    </div>

                    <div className="summary-content">

                        <span>
                            Money Received
                        </span>

                        <strong>
                            {formatCurrency(summary.received)}
                        </strong>

                        <small>
                            {summary.receivedCount}{" "}
                            transaction
                            {summary.receivedCount !== 1
                                ? "s"
                                : ""}
                        </small>

                    </div>

                </div>


                <div className="transaction-summary-card total-card">

                    <div className="summary-icon total">
                        <CreditCard size={19} />
                    </div>

                    <div className="summary-content">

                        <span>
                            Total Transactions
                        </span>

                        <strong>
                            {summary.total}
                        </strong>

                        <small>
                            Recent activity
                        </small>

                    </div>

                </div>

            </div>


            {/* ==================================================
                MAIN TRANSACTIONS CARD
            ================================================== */}

            <div className="transactions-card">

                <div className="transactions-card-header">

                    <div>

                        <div className="section-label">
                            ACTIVITY
                        </div>

                        <h2>
                            Recent Transactions
                        </h2>

                        <p>
                            Your latest banking activity
                            and transaction details.
                        </p>

                    </div>

                    <div className="transaction-count-badge">
                        {filteredTransactions.length}
                        {" "}
                        shown
                    </div>

                </div>


                {/* ==================================================
                    FILTERS
                ================================================== */}

                <div className="transaction-filters">

                    <div className="transaction-search">

                        <Search size={17} />

                        <input
                            type="text"
                            placeholder="Search transactions..."
                            value={searchTerm}
                            onChange={(event) =>
                                setSearchTerm(
                                    event.target.value
                                )
                            }
                        />

                        {searchTerm && (

                            <button
                                type="button"
                                className="clear-search"
                                onClick={() =>
                                    setSearchTerm("")
                                }
                            >

                                <XCircle size={16} />

                            </button>

                        )}

                    </div>


                    <select
                        value={directionFilter}
                        onChange={(event) =>
                            setDirectionFilter(
                                event.target.value
                            )
                        }
                    >

                        <option value="ALL">
                            All Transactions
                        </option>

                        <option value="SENT">
                            Sent
                        </option>

                        <option value="RECEIVED">
                            Received
                        </option>

                    </select>


                    <select
                        value={statusFilter}
                        onChange={(event) =>
                            setStatusFilter(
                                event.target.value
                            )
                        }
                    >

                        <option value="ALL">
                            All Statuses
                        </option>

                        <option value="COMPLETED">
                            Completed
                        </option>

                        <option value="PENDING">
                            Pending
                        </option>

                        <option value="FAILED">
                            Failed
                        </option>

                        <option value="CANCELLED">
                            Cancelled
                        </option>

                    </select>

                </div>


                {/* ==================================================
                    LOADING
                ================================================== */}

                {loading && (

                    <div className="transactions-state">

                        <div className="loading-spinner" />

                        <p>
                            Loading your transactions...
                        </p>

                    </div>

                )}


                {/* ==================================================
                    ERROR
                ================================================== */}

                {!loading && error && (

                    <div className="transactions-state error-state">

                        <div className="state-icon error">
                            <XCircle size={28} />
                        </div>

                        <h3>
                            Unable to load transactions
                        </h3>

                        <p>
                            {error}
                        </p>

                        <button
                            onClick={() =>
                                fetchTransactions()
                            }
                        >
                            Try Again
                        </button>

                    </div>

                )}


                {/* ==================================================
                    EMPTY
                ================================================== */}

                {!loading &&
                    !error &&
                    filteredTransactions.length === 0 && (

                        <div className="transactions-state">

                            <div className="state-icon">
                                <CreditCard size={30} />
                            </div>

                            <h3>
                                No transactions found
                            </h3>

                            <p>
                                {transactions.length === 0
                                    ? "Your recent transactions will appear here."
                                    : "Try changing your search or filters."}
                            </p>

                        </div>
                    )}


                {/* ==================================================
                    TRANSACTION LIST
                ================================================== */}

                {!loading &&
                    !error &&
                    filteredTransactions.length > 0 && (

                        <div className="transaction-list">

                            {filteredTransactions.map(
                                (transaction, index) => {

                                    const isSent =
                                        transaction.direction ===
                                        "SENT";

                                    return (

                                        <div
                                            className="transaction-row"
                                            key={
                                                transaction.transactionReference ||
                                                index
                                            }
                                        >

                                            {/* ICON */}

                                            <div
                                                className={
                                                    `transaction-direction-icon ${
                                                        isSent
                                                            ? "sent"
                                                            : "received"
                                                    }`
                                                }
                                            >

                                                {isSent ? (
                                                    <ArrowUpRight
                                                        size={19}
                                                    />
                                                ) : (
                                                    <ArrowDownLeft
                                                        size={19}
                                                    />
                                                )}

                                            </div>


                                            {/* MAIN INFO */}

                                            <div className="transaction-main">

                                                <div className="transaction-title-row">

                                                    <strong>

                                                        {transaction.description ||
                                                            (isSent
                                                                ? "Money sent"
                                                                : "Money received")}

                                                    </strong>

                                                    <span
                                                        className={
                                                            `transaction-direction-label ${
                                                                isSent
                                                                    ? "sent"
                                                                    : "received"
                                                            }`
                                                        }
                                                    >
                                                        {isSent
                                                            ? "SENT"
                                                            : "RECEIVED"}
                                                    </span>

                                                </div>


                                                <div className="transaction-meta">

                                                    <span>
                                                        {isSent
                                                            ? "To"
                                                            : "From"}{" "}
                                                        <strong>
                                                            {shortenAccount(
                                                                transaction.counterpartyAccountNumber
                                                            )}
                                                        </strong>
                                                    </span>

                                                    <span className="meta-dot">
                                                        •
                                                    </span>

                                                    <span>
                                                        {formatDate(
                                                            transaction.completedAt ||
                                                            transaction.createdAt
                                                        )}
                                                    </span>

                                                    <span className="meta-dot">
                                                        •
                                                    </span>

                                                    <span>
                                                        {formatTime(
                                                            transaction.completedAt ||
                                                            transaction.createdAt
                                                        )}
                                                    </span>

                                                </div>


                                                <div className="transaction-reference">

                                                    <span>
                                                        {transaction.transactionReference}
                                                    </span>

                                                    <button
                                                        type="button"
                                                        title="Copy transaction reference"
                                                        onClick={() =>
                                                            copyReference(
                                                                transaction.transactionReference
                                                            )
                                                        }
                                                    >
                                                        <Copy size={13} />
                                                    </button>

                                                </div>

                                            </div>


                                            {/* AMOUNT + STATUS */}

                                            <div className="transaction-right">

                                                <strong
                                                    className={
                                                        `transaction-amount ${
                                                            isSent
                                                                ? "sent"
                                                                : "received"
                                                        }`
                                                    }
                                                >

                                                    {isSent
                                                        ? "-"
                                                        : "+"}

                                                    {formatCurrency(
                                                        transaction.amount,
                                                        transaction.currency
                                                    )}

                                                </strong>


                                                <span
                                                    className={
                                                        `transaction-status ${
                                                            (
                                                                transaction.status ||
                                                                ""
                                                            ).toLowerCase()
                                                        }`
                                                    }
                                                >

                                                    {renderStatusIcon(
                                                        transaction.status
                                                    )}

                                                    {transaction.status ||
                                                        "UNKNOWN"}

                                                </span>

                                            </div>

                                        </div>
                                    );
                                }
                            )}

                        </div>
                    )}

            </div>

        </div>
    );
}