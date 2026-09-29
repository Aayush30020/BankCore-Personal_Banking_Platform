import { useEffect, useMemo, useState } from "react";
import {
    CalendarDays,
    CheckCircle2,
    CircleDollarSign,
    Clock3,
    Edit3,
    Plus,
    RefreshCw,
    Sparkles,
    Target,
    Trash2,
    TrendingUp,
    Wallet,
    X,
} from "lucide-react";

import api from "../api";
import "./SavingsGoalsPage.css";


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


const getToday = () => {

    const date = new Date();

    const year =
        date.getFullYear();

    const month =
        String(date.getMonth() + 1)
            .padStart(2, "0");

    const day =
        String(date.getDate())
            .padStart(2, "0");

    return `${year}-${month}-${day}`;
};


// ============================================================
// STATUS HELPERS
// ============================================================

const getStatusLabel = (status) => {

    switch (status) {

        case "COMPLETED":
            return "Completed";

        case "DEADLINE_PASSED":
            return "Deadline Passed";

        case "NOT_STARTED":
            return "Not Started";

        case "IN_PROGRESS":
            return "In Progress";

        default:
            return status || "Unknown";
    }
};


const getStatusClass = (status) => {

    switch (status) {

        case "COMPLETED":
            return "completed";

        case "DEADLINE_PASSED":
            return "danger";

        case "NOT_STARTED":
            return "neutral";

        case "IN_PROGRESS":
            return "progress";

        default:
            return "neutral";
    }
};


// ============================================================
// DERIVE STATUS FOR GOAL CARD
// ============================================================

const getGoalStatus = (goal) => {

    const targetAmount =
        Number(
            goal?.targetAmount || 0
        );

    const currentAmount =
        Number(
            goal?.currentAmount || 0
        );

    const targetDate =
        goal?.targetDate;


    // Goal has already reached its target.
    if (
        targetAmount > 0 &&
        currentAmount >= targetAmount
    ) {

        return "COMPLETED";
    }


    // Target date has already passed.
    if (
        targetDate &&
        targetDate < getToday()
    ) {

        return "DEADLINE_PASSED";
    }


    // No money has been saved yet.
    if (
        currentAmount <= 0
    ) {

        return "NOT_STARTED";
    }


    // Goal has some progress and deadline
    // has not passed.
    return "IN_PROGRESS";
};


// ============================================================
// SAVINGS GOALS PAGE
// ============================================================

export default function SavingsGoalsPage() {

    const [goals, setGoals] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [refreshing, setRefreshing] =
        useState(false);

    const [error, setError] =
        useState("");

    const [showForm, setShowForm] =
        useState(false);

    const [saving, setSaving] =
        useState(false);

    const [deletingGoal, setDeletingGoal] =
        useState("");

    const [editingGoal, setEditingGoal] =
        useState(null);

    const [selectedGoal, setSelectedGoal] =
        useState(null);

    const [analysis, setAnalysis] =
        useState(null);

    const [analysisLoading, setAnalysisLoading] =
        useState(false);

    const [formError, setFormError] =
        useState("");


    const [form, setForm] =
        useState({
            name: "",
            targetAmount: "",
            currentAmount: "",
            targetDate: "",
        });


    // ========================================================
    // FETCH GOALS
    // ========================================================

    const fetchGoals = async (
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
                    "/savings-goals"
                );

            setGoals(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Failed to load savings goals:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to load your savings goals."
            );

        } finally {

            setLoading(false);
            setRefreshing(false);
        }
    };


    useEffect(() => {

        fetchGoals();

    }, []);


    // ========================================================
    // SUMMARY
    // ========================================================

    const summary = useMemo(() => {

        const totalTarget =
            goals.reduce(
                (sum, goal) =>
                    sum +
                    Number(
                        goal.targetAmount || 0
                    ),
                0
            );

        const totalSaved =
            goals.reduce(
                (sum, goal) =>
                    sum +
                    Number(
                        goal.currentAmount || 0
                    ),
                0
            );

        const totalRemaining =
            goals.reduce(
                (sum, goal) =>
                    sum +
                    Number(
                        goal.remainingAmount || 0
                    ),
                0
            );

        const completed =
            goals.filter(
                (goal) =>
                    getGoalStatus(goal) ===
                    "COMPLETED"
            ).length;

        return {
            totalTarget,
            totalSaved,
            totalRemaining,
            completed,
        };

    }, [goals]);


    // ========================================================
    // FORM
    // ========================================================

    const openCreateForm = () => {

        setEditingGoal(null);

        setForm({
            name: "",
            targetAmount: "",
            currentAmount: "0",
            targetDate: "",
        });

        setFormError("");

        setShowForm(true);
    };


    const openEditForm = (goal) => {

        setEditingGoal(goal);

        setForm({
            name: goal.name || "",
            targetAmount:
                goal.targetAmount ?? "",
            currentAmount:
                goal.currentAmount ?? "0",
            targetDate:
                goal.targetDate || "",
        });

        setFormError("");

        setShowForm(true);
    };


    const closeForm = () => {

        if (saving) {
            return;
        }

        setShowForm(false);

        setEditingGoal(null);

        setFormError("");
    };


    const handleFormChange = (
        event
    ) => {

        const {
            name,
            value,
        } = event.target;

        setForm((previous) => ({
            ...previous,
            [name]: value,
        }));
    };


    // ========================================================
    // SAVE GOAL
    // ========================================================

    const handleSaveGoal = async (
        event
    ) => {

        event.preventDefault();

        setFormError("");

        const name =
            form.name.trim();

        const targetAmount =
            Number(form.targetAmount);

        const currentAmount =
            Number(form.currentAmount);

        const targetDate =
            form.targetDate;


        if (!name) {

            setFormError(
                "Please enter a goal name."
            );

            return;
        }


        if (
            !Number.isFinite(targetAmount) ||
            targetAmount <= 0
        ) {

            setFormError(
                "Target amount must be greater than zero."
            );

            return;
        }


        if (
            !Number.isFinite(currentAmount) ||
            currentAmount < 0
        ) {

            setFormError(
                "Current saved amount cannot be negative."
            );

            return;
        }


        if (
            currentAmount >
            targetAmount
        ) {

            setFormError(
                "Current saved amount cannot exceed the target amount."
            );

            return;
        }


        if (!targetDate) {

            setFormError(
                "Please select a target date."
            );

            return;
        }


        if (
            targetDate <
            getToday()
        ) {

            setFormError(
                "Target date cannot be in the past."
            );

            return;
        }


        try {

            setSaving(true);

            const response =
                await api.put(
                    "/savings-goals",
                    {
                        name,
                        targetAmount,
                        currentAmount,
                        targetDate,
                    }
                );

            const savedGoal =
                response.data;


            setGoals((previous) => {

                const existingIndex =
                    previous.findIndex(
                        (goal) =>
                            goal.name ===
                            savedGoal.name
                    );

                if (
                    existingIndex ===
                    -1
                ) {

                    return [
                        ...previous,
                        savedGoal,
                    ].sort(
                        (a, b) =>
                            new Date(a.targetDate) -
                            new Date(b.targetDate)
                    );
                }


                return previous
                    .map((goal) =>
                        goal.name ===
                        savedGoal.name
                            ? savedGoal
                            : goal
                    )
                    .sort(
                        (a, b) =>
                            new Date(a.targetDate) -
                            new Date(b.targetDate)
                    );
            });


            setShowForm(false);

            setEditingGoal(null);

        } catch (err) {

            console.error(
                "Failed to save savings goal:",
                err
            );

            setFormError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to save the savings goal."
            );

        } finally {

            setSaving(false);
        }
    };


    // ========================================================
    // DELETE GOAL
    // ========================================================

    const handleDeleteGoal = async (
        goal
    ) => {

        const confirmed =
            window.confirm(
                `Delete the savings goal "${goal.name}"?`
            );

        if (!confirmed) {
            return;
        }


        try {

            setDeletingGoal(
                goal.name
            );

            await api.delete(
                `/savings-goals/${encodeURIComponent(
                    goal.name
                )}`
            );

            setGoals((previous) =>
                previous.filter(
                    (item) =>
                        item.name !==
                        goal.name
                )
            );


            if (
                selectedGoal?.name ===
                goal.name
            ) {

                setSelectedGoal(null);
                setAnalysis(null);
            }

        } catch (err) {

            console.error(
                "Failed to delete savings goal:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to delete the savings goal."
            );

        } finally {

            setDeletingGoal("");
        }
    };


    // ========================================================
    // ANALYZE GOAL
    // ========================================================

    const handleAnalyzeGoal = async (
        goal
    ) => {

        try {

            setSelectedGoal(goal);

            setAnalysis(null);

            setAnalysisLoading(true);

            const response =
                await api.get(
                    `/savings-goals/analysis/${encodeURIComponent(
                        goal.name
                    )}`
                );

            setAnalysis(
                response.data
            );

        } catch (err) {

            console.error(
                "Failed to analyze savings goal:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to analyze this savings goal."
            );

        } finally {

            setAnalysisLoading(false);
        }
    };


    // ========================================================
    // LOADING
    // ========================================================

    if (loading) {

        return (
            <div className="savings-page">

                <div className="savings-loading">

                    <RefreshCw
                        size={25}
                        className="spin"
                    />

                    <h2>
                        Loading savings goals
                    </h2>

                    <p>
                        Fetching your latest savings
                        progress...
                    </p>

                </div>

            </div>
        );
    }


    // ========================================================
    // RENDER
    // ========================================================

    return (
        <div className="savings-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <div className="savings-header">

                <div>

                    <div className="savings-eyebrow">
                        FINANCIAL PLANNING
                    </div>

                    <h1>
                        Savings Goals
                    </h1>

                    <p>
                        Set targets, track your progress,
                        and understand what you need to save.
                    </p>

                </div>


                <div className="savings-header-actions">

                    <button
                        className="savings-refresh-button"
                        onClick={() =>
                            fetchGoals(true)
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


                    <button
                        className="savings-primary-button"
                        onClick={openCreateForm}
                    >

                        <Plus size={17} />

                        New Goal

                    </button>

                </div>

            </div>


            {/* ==================================================
                ERROR
            ================================================== */}

            {error && (

                <div className="savings-error">

                    <span>
                        {error}
                    </span>

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
                SUMMARY CARDS
            ================================================== */}

            <div className="savings-summary-grid">

                <div className="savings-summary-card">

                    <div className="summary-icon blue">
                        <Target size={20} />
                    </div>

                    <div>

                        <span>
                            Total Goals
                        </span>

                        <strong>
                            {goals.length}
                        </strong>

                        <small>
                            Active savings targets
                        </small>

                    </div>

                </div>


                <div className="savings-summary-card">

                    <div className="summary-icon purple">
                        <CircleDollarSign
                            size={20}
                        />
                    </div>

                    <div>

                        <span>
                            Total Target
                        </span>

                        <strong>
                            {formatCurrency(
                                summary.totalTarget
                            )}
                        </strong>

                        <small>
                            Across all goals
                        </small>

                    </div>

                </div>


                <div className="savings-summary-card">

                    <div className="summary-icon green">
                        <Wallet size={20} />
                    </div>

                    <div>

                        <span>
                            Total Saved
                        </span>

                        <strong>
                            {formatCurrency(
                                summary.totalSaved
                            )}
                        </strong>

                        <small>
                            Current progress
                        </small>

                    </div>

                </div>


                <div className="savings-summary-card">

                    <div className="summary-icon orange">
                        <TrendingUp size={20} />
                    </div>

                    <div>

                        <span>
                            Remaining
                        </span>

                        <strong>
                            {formatCurrency(
                                summary.totalRemaining
                            )}
                        </strong>

                        <small>
                            Amount left to save
                        </small>

                    </div>

                </div>

            </div>


            {/* ==================================================
                GOALS
            ================================================== */}

            {goals.length === 0 ? (

                <div className="savings-empty">

                    <div className="savings-empty-icon">
                        <Target size={30} />
                    </div>

                    <h2>
                        No savings goals yet
                    </h2>

                    <p>
                        Create your first savings goal
                        and start tracking your progress.
                    </p>

                    <button
                        className="savings-primary-button"
                        onClick={openCreateForm}
                    >
                        <Plus size={17} />
                        Create Savings Goal
                    </button>

                </div>

            ) : (

                <div className="savings-content-grid">

                    <div className="savings-goals-section">

                        <div className="savings-section-header">

                            <div>

                                <h2>
                                    Your Goals
                                </h2>

                                <p>
                                    Track each savings target.
                                </p>

                            </div>

                        </div>


                        <div className="savings-goals-list">

                            {goals.map(
                                (goal) => {

                                    const progress =
                                        Math.min(
                                            100,
                                            Math.max(
                                                0,
                                                Number(
                                                    goal.progressPercentage ||
                                                    0
                                                )
                                            )
                                        );

                                    const goalStatus =
                                        getGoalStatus(
                                            goal
                                        );

                                    const isSelected =
                                        selectedGoal?.name ===
                                        goal.name;

                                    return (
                                        <div
                                            key={
                                                goal.id ||
                                                goal.name
                                            }
                                            className={`savings-goal-card ${
                                                isSelected
                                                    ? "selected"
                                                    : ""
                                            }`}
                                        >

                                            <div className="goal-card-top">

                                                <div className="goal-title-area">

                                                    <div className="goal-icon">
                                                        <Target
                                                            size={20}
                                                        />
                                                    </div>

                                                    <div>

                                                        <h3>
                                                            {goal.name}
                                                        </h3>

                                                        <span>
                                                            Target date{" "}
                                                            {formatDate(
                                                                goal.targetDate
                                                            )}
                                                        </span>

                                                    </div>

                                                </div>


                                                <div
                                                    className={`goal-status ${getStatusClass(
                                                        goalStatus
                                                    )}`}
                                                >
                                                    {getStatusLabel(
                                                        goalStatus
                                                    )}
                                                </div>

                                            </div>


                                            <div className="goal-progress-row">

                                                <div className="goal-progress-track">

                                                    <div
                                                        className="goal-progress-fill"
                                                        style={{
                                                            width: `${progress}%`,
                                                        }}
                                                    />

                                                </div>

                                                <strong>
                                                    {progress.toFixed(
                                                        0
                                                    )}
                                                    %
                                                </strong>

                                            </div>


                                            <div className="goal-amount-row">

                                                <div>

                                                    <span>
                                                        Saved
                                                    </span>

                                                    <strong>
                                                        {formatCurrency(
                                                            goal.currentAmount
                                                        )}
                                                    </strong>

                                                </div>


                                                <div>

                                                    <span>
                                                        Target
                                                    </span>

                                                    <strong>
                                                        {formatCurrency(
                                                            goal.targetAmount
                                                        )}
                                                    </strong>

                                                </div>


                                                <div>

                                                    <span>
                                                        Remaining
                                                    </span>

                                                    <strong>
                                                        {formatCurrency(
                                                            goal.remainingAmount
                                                        )}
                                                    </strong>

                                                </div>

                                            </div>


                                            <div className="goal-card-actions">

                                                <button
                                                    className="goal-analyze-button"
                                                    onClick={() =>
                                                        handleAnalyzeGoal(
                                                            goal
                                                        )
                                                    }
                                                >

                                                    <Sparkles
                                                        size={15}
                                                    />

                                                    Analyze

                                                </button>


                                                <button
                                                    className="goal-edit-button"
                                                    onClick={() =>
                                                        openEditForm(
                                                            goal
                                                        )
                                                    }
                                                >

                                                    <Edit3
                                                        size={15}
                                                    />

                                                    Edit

                                                </button>


                                                <button
                                                    className="goal-delete-button"
                                                    onClick={() =>
                                                        handleDeleteGoal(
                                                            goal
                                                        )
                                                    }
                                                    disabled={
                                                        deletingGoal ===
                                                        goal.name
                                                    }
                                                >

                                                    {deletingGoal ===
                                                    goal.name ? (
                                                        <RefreshCw
                                                            size={15}
                                                            className="spin"
                                                        />
                                                    ) : (
                                                        <Trash2
                                                            size={15}
                                                        />
                                                    )}

                                                    Delete

                                                </button>

                                            </div>

                                        </div>
                                    );
                                }
                            )}

                        </div>

                    </div>


                    {/* ==================================================
                        ANALYSIS
                    ================================================== */}

                    <div className="savings-analysis-panel">

                        {!selectedGoal ? (

                            <div className="analysis-empty">

                                <div className="analysis-empty-icon">
                                    <Sparkles
                                        size={25}
                                    />
                                </div>

                                <h3>
                                    Goal Analysis
                                </h3>

                                <p>
                                    Select Analyze on a goal
                                    to see how much you need
                                    to save each day and month.
                                </p>

                            </div>

                        ) : analysisLoading ? (

                            <div className="analysis-loading">

                                <RefreshCw
                                    size={23}
                                    className="spin"
                                />

                                <h3>
                                    Analyzing goal
                                </h3>

                                <p>
                                    Calculating your savings
                                    requirements...
                                </p>

                            </div>

                        ) : analysis ? (

                            <>

                                <div className="analysis-header">

                                    <div>

                                        <span>
                                            GOAL ANALYSIS
                                        </span>

                                        <h2>
                                            {analysis.name}
                                        </h2>

                                    </div>

                                    <div className="analysis-sparkle">
                                        <Sparkles
                                            size={19}
                                        />
                                    </div>

                                </div>


                                <div className="analysis-progress">

                                    <div className="analysis-progress-circle">

                                        <strong>
                                            {Number(
                                                analysis.progressPercentage ||
                                                0
                                            ).toFixed(0)}
                                            %
                                        </strong>

                                        <span>
                                            complete
                                        </span>

                                    </div>

                                </div>


                                <div className="analysis-stats">

                                    <div>

                                        <span>
                                            Target
                                        </span>

                                        <strong>
                                            {formatCurrency(
                                                analysis.targetAmount
                                            )}
                                        </strong>

                                    </div>


                                    <div>

                                        <span>
                                            Saved
                                        </span>

                                        <strong>
                                            {formatCurrency(
                                                analysis.currentAmount
                                            )}
                                        </strong>

                                    </div>


                                    <div>

                                        <span>
                                            Remaining
                                        </span>

                                        <strong>
                                            {formatCurrency(
                                                analysis.remainingAmount
                                            )}
                                        </strong>

                                    </div>

                                </div>


                                <div className="analysis-divider" />


                                <div className="analysis-details">

                                    <div className="analysis-detail-row">

                                        <div>
                                            <CalendarDays
                                                size={16}
                                            />

                                            <span>
                                                Target date
                                            </span>
                                        </div>

                                        <strong>
                                            {formatDate(
                                                analysis.targetDate
                                            )}
                                        </strong>

                                    </div>


                                    <div className="analysis-detail-row">

                                        <div>
                                            <Clock3
                                                size={16}
                                            />

                                            <span>
                                                Days remaining
                                            </span>
                                        </div>

                                        <strong>
                                            {analysis.daysRemaining}
                                        </strong>

                                    </div>


                                    <div className="analysis-detail-row">

                                        <div>
                                            <CalendarDays
                                                size={16}
                                            />

                                            <span>
                                                Months remaining
                                            </span>
                                        </div>

                                        <strong>
                                            {analysis.monthsRemaining}
                                        </strong>

                                    </div>


                                    <div className="analysis-detail-row highlight">

                                        <div>
                                            <TrendingUp
                                                size={16}
                                            />

                                            <span>
                                                Required monthly saving
                                            </span>
                                        </div>

                                        <strong>
                                            {formatCurrency(
                                                analysis.requiredMonthlySaving
                                            )}
                                        </strong>

                                    </div>


                                    <div className="analysis-detail-row highlight">

                                        <div>
                                            <Wallet
                                                size={16}
                                            />

                                            <span>
                                                Required daily saving
                                            </span>
                                        </div>

                                        <strong>
                                            {formatCurrency(
                                                analysis.requiredDailySaving
                                            )}
                                        </strong>

                                    </div>

                                </div>


                                <div
                                    className={`analysis-status ${getStatusClass(
                                        analysis.status
                                    )}`}
                                >

                                    <CheckCircle2
                                        size={17}
                                    />

                                    <span>
                                        {getStatusLabel(
                                            analysis.status
                                        )}
                                    </span>

                                </div>

                            </>

                        ) : null}

                    </div>

                </div>
            )}


            {/* ==================================================
                CREATE / EDIT MODAL
            ================================================== */}

            {showForm && (

                <div
                    className="savings-modal-overlay"
                    onMouseDown={(event) => {

                        if (
                            event.target ===
                            event.currentTarget
                        ) {
                            closeForm();
                        }

                    }}
                >

                    <div className="savings-modal">

                        <div className="savings-modal-header">

                            <div>

                                <span>
                                    {editingGoal
                                        ? "UPDATE GOAL"
                                        : "NEW SAVINGS GOAL"}
                                </span>

                                <h2>
                                    {editingGoal
                                        ? "Edit savings goal"
                                        : "Create savings goal"}
                                </h2>

                            </div>

                            <button
                                className="modal-close-button"
                                onClick={closeForm}
                                disabled={saving}
                            >
                                <X size={19} />
                            </button>

                        </div>


                        <form
                            onSubmit={
                                handleSaveGoal
                            }
                        >

                            <div className="savings-form-grid">

                                <div className="savings-form-field full">

                                    <label>
                                        Goal name
                                    </label>

                                    <input
                                        type="text"
                                        name="name"
                                        value={
                                            form.name
                                        }
                                        onChange={
                                            handleFormChange
                                        }
                                        placeholder="e.g. Emergency Fund"
                                        maxLength={100}
                                        disabled={
                                            saving
                                        }
                                    />

                                </div>


                                <div className="savings-form-field">

                                    <label>
                                        Target amount
                                    </label>

                                    <div className="currency-input">

                                        <span>
                                            ₹
                                        </span>

                                        <input
                                            type="number"
                                            name="targetAmount"
                                            value={
                                                form.targetAmount
                                            }
                                            onChange={
                                                handleFormChange
                                            }
                                            placeholder="50000"
                                            min="0.01"
                                            step="0.01"
                                            disabled={
                                                saving
                                            }
                                        />

                                    </div>

                                </div>


                                <div className="savings-form-field">

                                    <label>
                                        Current saved
                                    </label>

                                    <div className="currency-input">

                                        <span>
                                            ₹
                                        </span>

                                        <input
                                            type="number"
                                            name="currentAmount"
                                            value={
                                                form.currentAmount
                                            }
                                            onChange={
                                                handleFormChange
                                            }
                                            placeholder="10000"
                                            min="0"
                                            step="0.01"
                                            disabled={
                                                saving
                                            }
                                        />

                                    </div>

                                </div>


                                <div className="savings-form-field full">

                                    <label>
                                        Target date
                                    </label>

                                    <input
                                        type="date"
                                        name="targetDate"
                                        value={
                                            form.targetDate
                                        }
                                        min={
                                            getToday()
                                        }
                                        onChange={
                                            handleFormChange
                                        }
                                        disabled={
                                            saving
                                        }
                                    />

                                </div>

                            </div>


                            {formError && (

                                <div className="savings-form-error">
                                    {formError}
                                </div>

                            )}


                            <div className="savings-modal-footer">

                                <button
                                    type="button"
                                    className="savings-cancel-button"
                                    onClick={
                                        closeForm
                                    }
                                    disabled={
                                        saving
                                    }
                                >
                                    Cancel
                                </button>


                                <button
                                    type="submit"
                                    className="savings-primary-button"
                                    disabled={
                                        saving
                                    }
                                >

                                    {saving ? (
                                        <>
                                            <RefreshCw
                                                size={16}
                                                className="spin"
                                            />

                                            Saving...
                                        </>
                                    ) : (
                                        <>
                                            <CheckCircle2
                                                size={16}
                                            />

                                            {editingGoal
                                                ? "Update Goal"
                                                : "Create Goal"}
                                        </>
                                    )}

                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

        </div>
    );
}