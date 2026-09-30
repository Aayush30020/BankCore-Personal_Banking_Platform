import { useEffect, useState } from "react";
import api from "../api";
import "./AccountsPage.css";

const DEMO_BUDGETS = [
    {
        category: "FOOD",
        monthlyLimit: 8000,
    },
    {
        category: "SHOPPING",
        monthlyLimit: 10000,
    },
    {
        category: "BILLS",
        monthlyLimit: 6000,
    },
    {
        category: "TRANSPORT",
        monthlyLimit: 5000,
    },
    {
        category: "ENTERTAINMENT",
        monthlyLimit: 3000,
    },
    {
        category: "HEALTH",
        monthlyLimit: 3000,
    },
    {
        category: "EDUCATION",
        monthlyLimit: 2000,
    },
];

const DEMO_SAVINGS_GOALS = [
    {
        name: "Emergency Fund",
        targetAmount: 100000,
        currentAmount: 40000,
        monthsFromNow: 2,
    },
    {
        name: "Laptop",
        targetAmount: 80000,
        currentAmount: 25000,
        monthsFromNow: 6,
    },
    {
        name: "Vacation Fund",
        targetAmount: 50000,
        currentAmount: 15000,
        monthsFromNow: 3,
    },
];

function getFutureDate(monthsFromNow) {
    const date = new Date();

    date.setMonth(
        date.getMonth() + monthsFromNow
    );

    return date.toISOString().split("T")[0];
}

function AccountsPage() {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(true);

    const [showAddMoney, setShowAddMoney] = useState(false);
    const [selectedAccount, setSelectedAccount] = useState(null);

    const [amount, setAmount] = useState("");
    const [description, setDescription] = useState("");

    const [addingMoney, setAddingMoney] = useState(false);
    const [generatingDemo, setGeneratingDemo] = useState(false);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        loadAccounts();
    }, []);

    const loadAccounts = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get("/accounts");

            setAccounts(response.data || []);
        } catch (err) {
            console.error(
                "Failed to load accounts:",
                err
            );

            setError(
                err.response?.data?.message ||
                "Unable to load your accounts."
            );
        } finally {
            setLoading(false);
        }
    };

    const openAddMoney = (account) => {
        setSelectedAccount(account);
        setAmount("");
        setDescription("");
        setMessage("");
        setError("");
        setShowAddMoney(true);
    };

    const closeAddMoney = () => {
        if (addingMoney) return;

        setShowAddMoney(false);
        setSelectedAccount(null);
        setAmount("");
        setDescription("");
        setMessage("");
        setError("");
    };

    const handleAddMoney = async (event) => {
        event.preventDefault();

        if (!selectedAccount) return;

        const numericAmount = Number(amount);

        if (!numericAmount || numericAmount <= 0) {
            setError(
                "Enter an amount greater than ₹0."
            );
            return;
        }

        if (numericAmount > 1000000) {
            setError(
                "A single demo deposit cannot exceed ₹10,00,000."
            );
            return;
        }

        try {
            setAddingMoney(true);
            setError("");
            setMessage("");

            const response = await api.post(
                "/demo-banking/deposit",
                {
                    accountId: selectedAccount.id,
                    amount: numericAmount,
                    description:
                        description.trim() ||
                        "Demo account funding",
                }
            );

            setMessage(
                response.data?.message ||
                "Money added successfully."
            );

            await loadAccounts();

            setTimeout(() => {
                closeAddMoney();
            }, 900);

        } catch (err) {
            console.error(
                "Failed to add money:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to add money right now."
            );
        } finally {
            setAddingMoney(false);
        }
    };

    /*
     * ---------------------------------------------------------
     * CREATE / UPDATE DEMO BUDGETS
     * ---------------------------------------------------------
     *
     * PUT is intentionally used because the existing budget
     * endpoint creates the budget if it doesn't exist and
     * updates it if it already exists.
     *
     * Therefore pressing Generate Demo Data multiple times
     * will not create duplicate budgets.
     */
    const createDemoBudgets = async () => {
        await Promise.all(
            DEMO_BUDGETS.map((budget) =>
                api.put("/budgets", {
                    category: budget.category,
                    monthlyLimit: budget.monthlyLimit,
                })
            )
        );
    };

    /*
     * ---------------------------------------------------------
     * CREATE / UPDATE DEMO SAVINGS GOALS
     * ---------------------------------------------------------
     *
     * Same idea as budgets: PUT performs create/update.
     */
    const createDemoSavingsGoals = async () => {
        await Promise.all(
            DEMO_SAVINGS_GOALS.map((goal) =>
                api.put("/savings-goals", {
                    name: goal.name,
                    targetAmount: goal.targetAmount,
                    currentAmount: goal.currentAmount,
                    targetDate: getFutureDate(
                        goal.monthsFromNow
                    ),
                })
            )
        );
    };

    /*
     * ---------------------------------------------------------
     * GENERATE COMPLETE DEMO DATA
     * ---------------------------------------------------------
     */
    const generateDemoData = async () => {
        const confirmed = window.confirm(
            "Generate the complete BankCore demo? This will prepare sample transactions, spending, budgets, and savings goals for your account."
        );

        if (!confirmed) return;

        try {
            setGeneratingDemo(true);
            setError("");
            setMessage("");

            /*
             * 1. Generate banking transactions.
             *
             * If demo transactions already exist, the backend
             * safely returns without creating duplicates.
             */
            const bankingResponse = await api.post(
                "/demo-banking/demo-data"
            );

            /*
             * 2. Create/update demo budgets.
             */
            await createDemoBudgets();

            /*
             * 3. Create/update demo savings goals.
             */
            await createDemoSavingsGoals();

            const bankingMessage =
                bankingResponse.data?.created
                    ? "Demo banking data generated successfully."
                    : "Demo banking data already exists.";

            setMessage(
                `${bankingMessage} Budgets and savings goals are ready.`
            );

            await loadAccounts();

        } catch (err) {
            console.error(
                "Failed to generate complete demo data:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to generate the complete demo data."
            );
        } finally {
            setGeneratingDemo(false);
        }
    };

    const formatCurrency = (
        amount,
        currency = "INR"
    ) => {
        return new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency,
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        }).format(amount || 0);
    };

    const formatDate = (date) => {
        if (!date) return "—";

        return new Date(date).toLocaleDateString(
            "en-IN",
            {
                day: "2-digit",
                month: "short",
                year: "numeric",
            }
        );
    };

    const getAccountTypeLabel = (type) => {
        if (!type) return "Account";

        return type
            .toString()
            .toLowerCase()
            .replace(
                /^\w/,
                (char) => char.toUpperCase()
            );
    };

    const getAccountInitial = (type) => {
        if (!type) return "A";

        return type
            .toString()
            .charAt(0)
            .toUpperCase();
    };

    const maskAccountNumber = (
        accountNumber
    ) => {
        if (!accountNumber) return "—";

        const value = String(accountNumber);

        if (value.length <= 8) {
            return value;
        }

        const firstFour = value.slice(0, 4);
        const lastFour = value.slice(-4);

        return `${firstFour} •••• •••• ${lastFour}`;
    };

    const getAccountTypeClass = (type) => {
        return (
            type?.toString().toLowerCase() ||
            "account"
        );
    };

    if (loading) {
        return (
            <div className="accounts-page">

                <div className="accounts-loading-state">

                    <div className="accounts-loading-spinner" />

                    <p>
                        Loading your accounts...
                    </p>

                </div>

            </div>
        );
    }

    return (
        <div className="accounts-page">

            {/* =================================================
                PAGE HEADER
            ================================================= */}

            <div className="accounts-header">

                <div className="accounts-heading">

                    <div className="accounts-eyebrow">
                        PERSONAL BANKING
                    </div>

                    <h1>
                        My Accounts
                    </h1>

                    <p>
                        View balances, manage accounts and
                        add demo funds securely.
                    </p>

                </div>

                <div className="accounts-header-actions">

                    <button
                        type="button"
                        className="demo-data-button"
                        onClick={generateDemoData}
                        disabled={generatingDemo}
                    >
                        <span className="button-icon">
                            {generatingDemo
                                ? "…"
                                : "✦"}
                        </span>

                        {generatingDemo
                            ? "Preparing Demo..."
                            : "Generate Demo Data"}
                    </button>

                    {accounts.length > 0 && (
                        <button
                            type="button"
                            className="add-money-header-button"
                            onClick={() =>
                                openAddMoney(
                                    accounts[0]
                                )
                            }
                        >
                            <span className="button-icon">
                                +
                            </span>

                            Add Money
                        </button>
                    )}

                </div>

            </div>

            {/* =================================================
                SUCCESS MESSAGE
            ================================================= */}

            {message && (
                <div className="accounts-success">

                    <span className="message-icon">
                        ✓
                    </span>

                    <span>
                        {message}
                    </span>

                    <button
                        type="button"
                        onClick={() =>
                            setMessage("")
                        }
                    >
                        ×
                    </button>

                </div>
            )}

            {/* =================================================
                ERROR MESSAGE
            ================================================= */}

            {error && (
                <div className="accounts-error">

                    <span className="message-icon">
                        !
                    </span>

                    <span>
                        {error}
                    </span>

                    <button
                        type="button"
                        onClick={() =>
                            setError("")
                        }
                    >
                        ×
                    </button>

                </div>
            )}

            {/* =================================================
                ACCOUNTS
            ================================================= */}

            {accounts.length === 0 ? (

                <div className="empty-accounts">

                    <div className="empty-account-icon">
                        $
                    </div>

                    <h2>
                        No accounts yet
                    </h2>

                    <p>
                        Your banking accounts will appear here.
                    </p>

                </div>

            ) : (

                <div className="accounts-grid">

                    {accounts.map((account) => (

                        <article
                            className={`account-card ${getAccountTypeClass(
                                account.type
                            )}`}
                            key={account.id}
                        >

                            <div className="account-card-glow" />

                            {/* CARD TOP */}

                            <div className="account-card-top">

                                <div className="account-identity">

                                    <div
                                        className={`account-icon ${getAccountTypeClass(
                                            account.type
                                        )}`}
                                    >
                                        {getAccountInitial(
                                            account.type
                                        )}
                                    </div>

                                    <div>

                                        <span className="account-type">
                                            {getAccountTypeLabel(
                                                account.type
                                            )}
                                        </span>

                                        <div className="account-number">
                                            {maskAccountNumber(
                                                account.accountNumber
                                            )}
                                        </div>

                                    </div>

                                </div>

                                <span
                                    className={`account-status ${
                                        account.status?.toLowerCase() ||
                                        ""
                                    }`}
                                >
                                    <span className="status-dot" />

                                    {account.status}
                                </span>

                            </div>

                            {/* BALANCE */}

                            <div className="account-balance-section">

                                <span className="balance-label">
                                    Available balance
                                </span>

                                <strong className="account-balance">
                                    {formatCurrency(
                                        account.balance,
                                        account.currency
                                    )}
                                </strong>

                            </div>

                            {/* META */}

                            <div className="account-meta">

                                <div className="account-meta-item">

                                    <span>
                                        Currency
                                    </span>

                                    <strong>
                                        {account.currency ||
                                            "INR"}
                                    </strong>

                                </div>

                                <div className="account-meta-item">

                                    <span>
                                        Opened
                                    </span>

                                    <strong>
                                        {formatDate(
                                            account.createdAt
                                        )}
                                    </strong>

                                </div>

                            </div>

                            {/* ACTION */}

                            <button
                                type="button"
                                className="account-add-money-button"
                                onClick={() =>
                                    openAddMoney(
                                        account
                                    )
                                }
                                disabled={
                                    account.status !==
                                    "ACTIVE"
                                }
                            >
                                <span>
                                    + Add Money
                                </span>

                                <span className="action-arrow">
                                    →
                                </span>

                            </button>

                        </article>

                    ))}

                </div>

            )}

            {/* =================================================
                ADD MONEY MODAL
            ================================================= */}

            {showAddMoney &&
                selectedAccount && (

                    <div
                        className="modal-overlay"
                        onMouseDown={(event) => {

                            if (
                                event.target ===
                                event.currentTarget
                            ) {
                                closeAddMoney();
                            }

                        }}
                    >

                        <div
                            className="add-money-modal"
                            role="dialog"
                            aria-modal="true"
                            aria-labelledby="add-money-title"
                        >

                            {/* MODAL HEADER */}

                            <div className="modal-header">

                                <div className="modal-heading">

                                    <div className="modal-eyebrow">
                                        DEMO FUNDING
                                    </div>

                                    <h2 id="add-money-title">
                                        Add Money
                                    </h2>

                                    <p>
                                        Simulate a deposit into
                                        your BankCore account.
                                    </p>

                                </div>

                                <button
                                    type="button"
                                    className="modal-close"
                                    onClick={
                                        closeAddMoney
                                    }
                                    disabled={
                                        addingMoney
                                    }
                                    aria-label="Close"
                                >
                                    ×
                                </button>

                            </div>

                            {/* SELECTED ACCOUNT */}

                            <div className="selected-account-card">

                                <div className="selected-account-top">

                                    <div className="selected-account-icon">
                                        {getAccountInitial(
                                            selectedAccount.type
                                        )}
                                    </div>

                                    <div>

                                        <span>
                                            {getAccountTypeLabel(
                                                selectedAccount.type
                                            )}
                                        </span>

                                        <strong>
                                            {maskAccountNumber(
                                                selectedAccount.accountNumber
                                            )}
                                        </strong>

                                    </div>

                                    <span className="selected-active">
                                        ACTIVE
                                    </span>

                                </div>

                                <div className="selected-account-balance">

                                    <span>
                                        Current balance
                                    </span>

                                    <strong>
                                        {formatCurrency(
                                            selectedAccount.balance,
                                            selectedAccount.currency
                                        )}
                                    </strong>

                                </div>

                            </div>

                            {/* FORM */}

                            <form
                                onSubmit={
                                    handleAddMoney
                                }
                                className="add-money-form"
                            >

                                <label>

                                    <span className="form-label">
                                        Amount
                                    </span>

                                    <div className="amount-input-wrapper">

                                        <span className="currency-symbol">
                                            ₹
                                        </span>

                                        <input
                                            type="number"
                                            min="1"
                                            max="1000000"
                                            step="0.01"
                                            value={amount}
                                            onChange={(
                                                event
                                            ) =>
                                                setAmount(
                                                    event
                                                        .target
                                                        .value
                                                )
                                            }
                                            placeholder="0.00"
                                            disabled={
                                                addingMoney
                                            }
                                            required
                                            autoFocus
                                        />

                                    </div>

                                    <span className="field-hint">
                                        Maximum demo deposit:
                                        ₹10,00,000
                                    </span>

                                </label>

                                <label>

                                    <span className="form-label">
                                        Description
                                    </span>

                                    <input
                                        type="text"
                                        maxLength="255"
                                        value={description}
                                        onChange={(
                                            event
                                        ) =>
                                            setDescription(
                                                event
                                                    .target
                                                    .value
                                            )
                                        }
                                        placeholder="e.g. Demo salary credit"
                                        disabled={
                                            addingMoney
                                        }
                                    />

                                    <span className="field-hint">
                                        Optional
                                    </span>

                                </label>

                                {error && (
                                    <div className="modal-error">

                                        <span>
                                            !
                                        </span>

                                        {error}

                                    </div>
                                )}

                                {message && (
                                    <div className="modal-success">

                                        <span>
                                            ✓
                                        </span>

                                        {message}

                                    </div>
                                )}

                                {/* ACTIONS */}

                                <div className="modal-actions">

                                    <button
                                        type="button"
                                        className="cancel-button"
                                        onClick={
                                            closeAddMoney
                                        }
                                        disabled={
                                            addingMoney
                                        }
                                    >
                                        Cancel
                                    </button>

                                    <button
                                        type="submit"
                                        className="confirm-add-money-button"
                                        disabled={
                                            addingMoney ||
                                            !amount
                                        }
                                    >
                                        {addingMoney ? (
                                            <>
                                                <span className="button-spinner" />

                                                Processing...
                                            </>
                                        ) : (
                                            <>
                                                Add Money

                                                <span>
                                                    →
                                                </span>
                                            </>
                                        )}
                                    </button>

                                </div>

                            </form>

                            <div className="modal-security-note">

                                <span>
                                    ⌁
                                </span>

                                Demo transactions are recorded
                                securely in your BankCore ledger.

                            </div>

                        </div>

                    </div>
                )}

        </div>
    );
}

export default AccountsPage;