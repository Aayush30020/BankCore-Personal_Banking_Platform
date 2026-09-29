import { useEffect, useMemo, useState } from "react";

import {
    AlertTriangle,
    CheckCircle2,
    ChevronRight,
    CircleDollarSign,
    Clock3,
    Edit3,
    Plus,
    RefreshCw,
    Trash2,
    Wallet,
    X,
    XCircle,
} from "lucide-react";

import api from "../api";

import "./BudgetPage.css";


// ============================================================
// CATEGORY CONFIG
// ============================================================

const CATEGORY_META = {

    FOOD: {
        label: "Food",
    },

    SHOPPING: {
        label: "Shopping",
    },

    BILLS: {
        label: "Bills",
    },

    TRANSPORT: {
        label: "Transport",
    },

    ENTERTAINMENT: {
        label: "Entertainment",
    },

    HEALTH: {
        label: "Health",
    },

    EDUCATION: {
        label: "Education",
    },

    TRANSFER: {
        label: "Transfer",
    },

    OTHER: {
        label: "Other",
    },
};


const CATEGORY_OPTIONS = Object.keys(
    CATEGORY_META
);


// ============================================================
// HELPERS
// ============================================================

const formatCurrency = (
    amount,
    currency = "INR"
) => {

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency,
            maximumFractionDigits: 2,
        }
    ).format(
        Number(amount || 0)
    );
};


const formatPercentage = (
    value
) => {

    return `${Number(value || 0).toFixed(1)}%`;
};


const getCategoryLabel = (
    category
) => {

    return (
        CATEGORY_META[category]?.label ||
        category ||
        "Other"
    );
};


const getStatusMeta = (
    status
) => {

    switch (status) {

        case "EXCEEDED":
            return {
                label: "Exceeded",
                className: "exceeded",
                icon: XCircle,
            };

        case "AT_LIMIT":
            return {
                label: "At Limit",
                className: "at-limit",
                icon: AlertTriangle,
            };

        case "NEAR_LIMIT":
            return {
                label: "Near Limit",
                className: "near-limit",
                icon: Clock3,
            };

        case "ON_TRACK":
        default:
            return {
                label: "On Track",
                className: "on-track",
                icon: CheckCircle2,
            };
    }
};


// ============================================================
// COMPONENT
// ============================================================

export default function BudgetPage() {

    const [budgets, setBudgets] =
        useState([]);

    const [analysis, setAnalysis] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [refreshing, setRefreshing] =
        useState(false);

    const [error, setError] =
        useState("");

    const [showModal, setShowModal] =
        useState(false);

    const [editingBudget, setEditingBudget] =
        useState(null);

    const [saving, setSaving] =
        useState(false);

    const [deletingCategory, setDeletingCategory] =
        useState(null);

    const [formError, setFormError] =
        useState("");

    const [form, setForm] = useState({
        category: "FOOD",
        monthlyLimit: "",
    });


    // ========================================================
    // FETCH DATA
    // ========================================================

    const fetchBudgets = async (
        showRefreshLoader = false
    ) => {

        try {

            if (showRefreshLoader) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const [
                budgetsResponse,
                analysisResponse,
            ] = await Promise.all([

                api.get(
                    "/budgets"
                ),

                api.get(
                    "/budgets/analysis"
                ),
            ]);


            setBudgets(
                Array.isArray(
                    budgetsResponse.data
                )
                    ? budgetsResponse.data
                    : []
            );

            setAnalysis(
                Array.isArray(
                    analysisResponse.data
                )
                    ? analysisResponse.data
                    : []
            );

        } catch (err) {

            console.error(
                "Failed to load budgets:",
                err
            );

            setError(
                err.response?.data?.message ||
                "Unable to load your budgets."
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

        fetchBudgets();

    }, []);


    // ========================================================
    // SUMMARY
    // ========================================================

    const summary = useMemo(() => {

        let totalLimit = 0;
        let totalSpent = 0;
        let totalRemaining = 0;

        let exceeded = 0;
        let nearLimit = 0;
        let onTrack = 0;
        let atLimit = 0;

        analysis.forEach(
            (item) => {

                totalLimit += Number(
                    item.monthlyLimit || 0
                );

                totalSpent += Number(
                    item.currentMonthSpending || 0
                );

                totalRemaining += Number(
                    item.remainingAmount || 0
                );

                switch (item.status) {

                    case "EXCEEDED":
                        exceeded++;
                        break;

                    case "NEAR_LIMIT":
                        nearLimit++;
                        break;

                    case "AT_LIMIT":
                        atLimit++;
                        break;

                    case "ON_TRACK":
                    default:
                        onTrack++;
                        break;
                }
            }
        );


        const usagePercentage =
            totalLimit > 0
                ? (totalSpent / totalLimit) * 100
                : 0;


        return {
            totalLimit,
            totalSpent,
            totalRemaining,
            usagePercentage,
            exceeded,
            nearLimit,
            atLimit,
            onTrack,
            totalBudgets: analysis.length,
        };

    }, [analysis]);


    // ========================================================
    // OPEN CREATE MODAL
    // ========================================================

    const openCreateModal = () => {

        setEditingBudget(null);

        setForm({
            category:
                CATEGORY_OPTIONS.find(
                    (category) =>
                        !budgets.some(
                            (budget) =>
                                budget.category ===
                                category
                        )
                ) ||
                "FOOD",

            monthlyLimit: "",
        });

        setFormError("");

        setShowModal(true);
    };


    // ========================================================
    // OPEN EDIT MODAL
    // ========================================================

    const openEditModal = (
        budget
    ) => {

        setEditingBudget(
            budget
        );

        setForm({
            category:
            budget.category,

            monthlyLimit:
                String(
                    budget.monthlyLimit
                ),
        });

        setFormError("");

        setShowModal(true);
    };


    // ========================================================
    // CLOSE MODAL
    // ========================================================

    const closeModal = () => {

        if (saving) {
            return;
        }

        setShowModal(false);

        setEditingBudget(null);

        setFormError("");
    };


    // ========================================================
    // FORM CHANGE
    // ========================================================

    const handleFormChange = (
        event
    ) => {

        const {
            name,
            value,
        } = event.target;

        setForm(
            (previous) => ({
                ...previous,
                [name]: value,
            })
        );
    };


    // ========================================================
    // SAVE BUDGET
    // ========================================================

    const handleSaveBudget = async (
        event
    ) => {

        event.preventDefault();

        setFormError("");

        const monthlyLimit =
            Number(
                form.monthlyLimit
            );


        if (!form.category) {

            setFormError(
                "Please select a category."
            );

            return;
        }


        if (
            !Number.isFinite(
                monthlyLimit
            ) ||
            monthlyLimit <= 0
        ) {

            setFormError(
                "Monthly budget must be greater than ₹0."
            );

            return;
        }


        try {

            setSaving(true);

            await api.put(
                "/budgets",
                {
                    category:
                    form.category,

                    monthlyLimit:
                    monthlyLimit,
                }
            );


            closeModal();

            await fetchBudgets();

        } catch (err) {

            console.error(
                "Failed to save budget:",
                err
            );

            setFormError(
                err.response?.data?.message ||
                "Unable to save the budget."
            );

        } finally {

            setSaving(false);
        }
    };


    // ========================================================
    // DELETE BUDGET
    // ========================================================

    const handleDeleteBudget = async (
        category
    ) => {

        const confirmed =
            window.confirm(
                `Delete the ${getCategoryLabel(
                    category
                )} budget?`
            );

        if (!confirmed) {
            return;
        }


        try {

            setDeletingCategory(
                category
            );

            await api.delete(
                `/budgets/${category}`
            );

            await fetchBudgets();

        } catch (err) {

            console.error(
                "Failed to delete budget:",
                err
            );

            window.alert(
                err.response?.data?.message ||
                "Unable to delete the budget."
            );

        } finally {

            setDeletingCategory(
                null
            );
        }
    };


    // ========================================================
    // LOADING
    // ========================================================

    if (loading) {

        return (
            <div className="budget-page">

                <div className="budget-loading">

                    <div className="budget-spinner" />

                    <p>
                        Loading your budgets...
                    </p>

                </div>

            </div>
        );
    }


    // ========================================================
    // ERROR
    // ========================================================

    if (error) {

        return (
            <div className="budget-page">

                <div className="budget-error">

                    <Wallet
                        size={34}
                    />

                    <h2>
                        Unable to load budgets
                    </h2>

                    <p>
                        {error}
                    </p>

                    <button
                        onClick={() =>
                            fetchBudgets()
                        }
                    >
                        Try Again
                    </button>

                </div>

            </div>
        );
    }


    // ========================================================
    // RENDER
    // ========================================================

    return (
        <div className="budget-page">


            {/* ==================================================
                HEADER
            ================================================== */}

            <div className="budget-header">

                <div>

                    <div className="budget-eyebrow">
                        BUDGET MANAGEMENT
                    </div>

                    <h1>
                        Your Budgets
                    </h1>

                    <p>
                        Set monthly spending limits
                        and keep track of your
                        financial goals.
                    </p>

                </div>


                <div className="budget-header-actions">

                    <button
                        className="budget-refresh-button"
                        onClick={() =>
                            fetchBudgets(true)
                        }
                        disabled={refreshing}
                    >

                        <RefreshCw
                            size={16}
                            className={
                                refreshing
                                    ? "budget-refresh-spin"
                                    : ""
                            }
                        />

                        {refreshing
                            ? "Refreshing..."
                            : "Refresh"}

                    </button>


                    <button
                        className="budget-add-button"
                        onClick={
                            openCreateModal
                        }
                    >

                        <Plus
                            size={17}
                        />

                        Add Budget

                    </button>

                </div>

            </div>


            {/* ==================================================
                SUMMARY CARDS
            ================================================== */}

            <div className="budget-summary-grid">


                <div className="budget-summary-card">

                    <div className="budget-summary-icon blue">

                        <Wallet
                            size={19}
                        />

                    </div>

                    <div>

                        <span>
                            Total Monthly Limits
                        </span>

                        <strong>
                            {formatCurrency(
                                summary.totalLimit
                            )}
                        </strong>

                        <small>
                            Across{" "}
                            {summary.totalBudgets}{" "}
                            budget
                            {summary.totalBudgets !== 1
                                ? "s"
                                : ""}
                        </small>

                    </div>

                </div>


                <div className="budget-summary-card">

                    <div className="budget-summary-icon purple">

                        <CircleDollarSign
                            size={19}
                        />

                    </div>

                    <div>

                        <span>
                            Current Month Spending
                        </span>

                        <strong>
                            {formatCurrency(
                                summary.totalSpent
                            )}
                        </strong>

                        <small>
                            {formatPercentage(
                                summary.usagePercentage
                            )}{" "}
                            of total limits
                        </small>

                    </div>

                </div>


                <div className="budget-summary-card">

                    <div className="budget-summary-icon green">

                        <CheckCircle2
                            size={19}
                        />

                    </div>

                    <div>

                        <span>
                            Remaining Budget
                        </span>

                        <strong>
                            {formatCurrency(
                                summary.totalRemaining
                            )}
                        </strong>

                        <small>
                            Across configured budgets
                        </small>

                    </div>

                </div>

            </div>


            {/* ==================================================
                STATUS OVERVIEW
            ================================================== */}

            <div className="budget-status-overview">

                <div className="budget-status-overview-header">

                    <div>

                        <h2>
                            Budget Status
                        </h2>

                        <p>
                            Current-month budget
                            utilization.
                        </p>

                    </div>

                    <strong>
                        {formatPercentage(
                            summary.usagePercentage
                        )}
                    </strong>

                </div>


                <div className="budget-overall-progress">

                    <div
                        className={
                            `budget-overall-progress-bar ${
                                summary.usagePercentage >= 100
                                    ? "danger"
                                    : summary.usagePercentage >= 80
                                        ? "warning"
                                        : ""
                            }`
                        }
                        style={{
                            width:
                                `${Math.min(
                                    summary.usagePercentage,
                                    100
                                )}%`,
                        }}
                    />

                </div>


                <div className="budget-status-counts">

                    <span className="status-count on-track">

                        <CheckCircle2
                            size={14}
                        />

                        {summary.onTrack} On Track

                    </span>


                    <span className="status-count near-limit">

                        <Clock3
                            size={14}
                        />

                        {summary.nearLimit} Near Limit

                    </span>


                    <span className="status-count at-limit">

                        <AlertTriangle
                            size={14}
                        />

                        {summary.atLimit} At Limit

                    </span>


                    <span className="status-count exceeded">

                        <XCircle
                            size={14}
                        />

                        {summary.exceeded} Exceeded

                    </span>

                </div>

            </div>


            {/* ==================================================
                EMPTY STATE
            ================================================== */}

            {analysis.length === 0 && (

                <div className="budget-empty">

                    <div className="budget-empty-icon">

                        <Wallet
                            size={28}
                        />

                    </div>

                    <h2>
                        No budgets configured
                    </h2>

                    <p>
                        Create your first monthly
                        spending budget to start
                        tracking your spending.
                    </p>

                    <button
                        onClick={
                            openCreateModal
                        }
                    >

                        <Plus
                            size={17}
                        />

                        Create Your First Budget

                    </button>

                </div>
            )}


            {/* ==================================================
                BUDGET LIST
            ================================================== */}

            {analysis.length > 0 && (

                <div className="budget-list-section">

                    <div className="budget-list-header">

                        <div>

                            <h2>
                                Your Budget Limits
                            </h2>

                            <p>
                                Monitor each category
                                against its monthly
                                limit.
                            </p>

                        </div>

                    </div>


                    <div className="budget-grid">

                        {analysis.map(
                            (item) => {

                                const status =
                                    getStatusMeta(
                                        item.status
                                    );

                                const StatusIcon =
                                    status.icon;

                                const limit =
                                    Number(
                                        item.monthlyLimit ||
                                        0
                                    );

                                const spent =
                                    Number(
                                        item.currentMonthSpending ||
                                        0
                                    );

                                const remaining =
                                    Number(
                                        item.remainingAmount ||
                                        0
                                    );

                                const usage =
                                    Number(
                                        item.usagePercentage ||
                                        0
                                    );

                                const isExceeded =
                                    item.status ===
                                    "EXCEEDED";


                                return (

                                    <div
                                        className="budget-card"
                                        key={
                                            item.category
                                        }
                                    >


                                        {/* CARD HEADER */}

                                        <div className="budget-card-header">

                                            <div>

                                                <div className="budget-category-label">

                                                    <span className="budget-category-dot" />

                                                    {getCategoryLabel(
                                                        item.category
                                                    )}

                                                </div>

                                                <small>
                                                    Monthly budget
                                                </small>

                                            </div>


                                            <div
                                                className={
                                                    `budget-status-badge ${status.className}`
                                                }
                                            >

                                                <StatusIcon
                                                    size={14}
                                                />

                                                {status.label}

                                            </div>

                                        </div>


                                        {/* AMOUNTS */}

                                        <div className="budget-amount-row">

                                            <div>

                                                <span>
                                                    Spent
                                                </span>

                                                <strong>
                                                    {formatCurrency(
                                                        spent,
                                                        item.currency
                                                    )}
                                                </strong>

                                            </div>


                                            <div className="budget-limit-amount">

                                                <span>
                                                    Limit
                                                </span>

                                                <strong>
                                                    {formatCurrency(
                                                        limit,
                                                        item.currency
                                                    )}
                                                </strong>

                                            </div>

                                        </div>


                                        {/* PROGRESS */}

                                        <div className="budget-progress-section">

                                            <div className="budget-progress-label">

                                                <span>
                                                    Budget usage
                                                </span>

                                                <strong>
                                                    {formatPercentage(
                                                        usage
                                                    )}
                                                </strong>

                                            </div>


                                            <div className="budget-progress-track">

                                                <div
                                                    className={
                                                        `budget-progress-fill ${
                                                            isExceeded
                                                                ? "danger"
                                                                : usage >= 80
                                                                    ? "warning"
                                                                    : ""
                                                        }`
                                                    }
                                                    style={{
                                                        width:
                                                            `${Math.min(
                                                                usage,
                                                                100
                                                            )}%`,
                                                    }}
                                                />

                                            </div>

                                        </div>


                                        {/* FOOTER */}

                                        <div className="budget-card-footer">

                                            <div>

                                                <span>
                                                    {remaining >= 0
                                                        ? "Remaining"
                                                        : "Over budget"}
                                                </span>

                                                <strong
                                                    className={
                                                        remaining < 0
                                                            ? "negative"
                                                            : ""
                                                    }
                                                >
                                                    {formatCurrency(
                                                        Math.abs(
                                                            remaining
                                                        ),
                                                        item.currency
                                                    )}
                                                </strong>

                                            </div>


                                            <div>

                                                <span>
                                                    Transactions
                                                </span>

                                                <strong>
                                                    {
                                                        item.transactionCount
                                                    }
                                                </strong>

                                            </div>

                                        </div>


                                        {/* ACTIONS */}

                                        <div className="budget-card-actions">

                                            <button
                                                className="budget-edit-button"
                                                onClick={() =>
                                                    openEditModal(
                                                        {
                                                            category:
                                                            item.category,

                                                            monthlyLimit:
                                                            item.monthlyLimit,
                                                        }
                                                    )
                                                }
                                            >

                                                <Edit3
                                                    size={14}
                                                />

                                                Edit

                                            </button>


                                            <button
                                                className="budget-delete-button"
                                                onClick={() =>
                                                    handleDeleteBudget(
                                                        item.category
                                                    )
                                                }
                                                disabled={
                                                    deletingCategory ===
                                                    item.category
                                                }
                                            >

                                                <Trash2
                                                    size={14}
                                                />

                                                {deletingCategory ===
                                                item.category
                                                    ? "Deleting..."
                                                    : "Delete"}

                                            </button>


                                            <ChevronRight
                                                size={16}
                                                className="budget-card-chevron"
                                            />

                                        </div>

                                    </div>
                                );
                            }
                        )}

                    </div>

                </div>
            )}


            {/* ==================================================
                MODAL
            ================================================== */}

            {showModal && (

                <div
                    className="budget-modal-overlay"
                    onMouseDown={(event) => {

                        if (
                            event.target ===
                            event.currentTarget
                        ) {
                            closeModal();
                        }

                    }}
                >

                    <div className="budget-modal">

                        <div className="budget-modal-header">

                            <div>

                                <div className="budget-modal-eyebrow">
                                    {editingBudget
                                        ? "UPDATE BUDGET"
                                        : "NEW BUDGET"}
                                </div>

                                <h2>
                                    {editingBudget
                                        ? "Edit Budget"
                                        : "Create Budget"}
                                </h2>

                                <p>
                                    Set a monthly spending
                                    limit for a category.
                                </p>

                            </div>


                            <button
                                className="budget-modal-close"
                                onClick={
                                    closeModal
                                }
                                disabled={saving}
                            >

                                <X
                                    size={19}
                                />

                            </button>

                        </div>


                        <form
                            className="budget-form"
                            onSubmit={
                                handleSaveBudget
                            }
                        >


                            {/* CATEGORY */}

                            <label>

                                <span>
                                    Category
                                </span>

                                <select
                                    name="category"
                                    value={
                                        form.category
                                    }
                                    onChange={
                                        handleFormChange
                                    }
                                    disabled={
                                        saving ||
                                        Boolean(
                                            editingBudget
                                        )
                                    }
                                >

                                    {CATEGORY_OPTIONS.map(
                                        (category) => {

                                            const alreadyExists =
                                                budgets.some(
                                                    (budget) =>
                                                        budget.category ===
                                                        category
                                                );

                                            const isEditing =
                                                editingBudget?.category ===
                                                category;

                                            return (

                                                <option
                                                    key={
                                                        category
                                                    }
                                                    value={
                                                        category
                                                    }
                                                    disabled={
                                                        alreadyExists &&
                                                        !isEditing
                                                    }
                                                >
                                                    {getCategoryLabel(
                                                        category
                                                    )}
                                                    {alreadyExists &&
                                                    !isEditing
                                                        ? " — already configured"
                                                        : ""}
                                                </option>
                                            );
                                        }
                                    )}

                                </select>

                            </label>


                            {/* LIMIT */}

                            <label>

                                <span>
                                    Monthly Limit
                                </span>

                                <div className="budget-input-wrapper">

                                    <span>
                                        ₹
                                    </span>

                                    <input
                                        type="number"
                                        name="monthlyLimit"
                                        value={
                                            form.monthlyLimit
                                        }
                                        onChange={
                                            handleFormChange
                                        }
                                        placeholder="5000"
                                        min="1"
                                        step="0.01"
                                        disabled={
                                            saving
                                        }
                                    />

                                </div>

                            </label>


                            {/* ERROR */}

                            {formError && (

                                <div className="budget-form-error">

                                    <AlertTriangle
                                        size={15}
                                    />

                                    {formError}

                                </div>
                            )}


                            {/* ACTIONS */}

                            <div className="budget-modal-actions">

                                <button
                                    type="button"
                                    className="budget-cancel-button"
                                    onClick={
                                        closeModal
                                    }
                                    disabled={
                                        saving
                                    }
                                >
                                    Cancel
                                </button>


                                <button
                                    type="submit"
                                    className="budget-save-button"
                                    disabled={
                                        saving
                                    }
                                >

                                    {saving
                                        ? "Saving..."
                                        : editingBudget
                                            ? "Update Budget"
                                            : "Create Budget"}

                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

        </div>
    );
}