import {
    CreditCard,
    WalletCards,
    CheckCircle2,
    CalendarDays,
    ShieldCheck,
    ArrowUpRight,
    Plus,
    X,
    Loader2,
    Building2,
} from "lucide-react";

import {
    useEffect,
    useState,
} from "react";

import {
    useNavigate,
} from "react-router-dom";

import api from "../api";
import "./AccountsPage.css";

// ============================================================
// ACCOUNTS PAGE
// ============================================================

function AccountsPage() {

    const navigate = useNavigate();


    // ==========================================================
    // ACCOUNT STATE
    // ==========================================================

    const [accounts, setAccounts] = useState([]);

    const [totalBalance, setTotalBalance] = useState(0);

    const [loading, setLoading] = useState(true);

    const [error, setError] = useState("");


    // ==========================================================
    // CREATE ACCOUNT STATE
    // ==========================================================

    const [showCreateModal, setShowCreateModal] =
        useState(false);

    const [accountType, setAccountType] =
        useState("SAVINGS");

    const [creatingAccount, setCreatingAccount] =
        useState(false);

    const [createError, setCreateError] =
        useState("");

    const [createSuccess, setCreateSuccess] =
        useState("");


    // ==========================================================
    // LOAD ACCOUNTS
    // ==========================================================

    const loadAccounts = async () => {

        try {

            setLoading(true);
            setError("");


            const response = await api.get(
                "/accounts/me"
            );


            const loadedAccounts =
                Array.isArray(response.data)
                    ? response.data
                    : response.data?.accounts || [];


            setAccounts(
                loadedAccounts
            );


            const calculatedBalance =
                loadedAccounts.reduce(
                    (total, account) =>
                        total +
                        Number(
                            account.balance || 0
                        ),
                    0
                );


            setTotalBalance(
                Number(
                    response.data?.totalBalance ??
                    calculatedBalance
                )
            );

        } catch (error) {

            console.error(
                "Failed to load accounts:",
                error
            );


            if (
                error.response?.status === 401
            ) {

                navigate(
                    "/login",
                    {
                        replace: true,
                    }
                );

                return;
            }


            setError(
                error.response?.data?.message ||
                "Unable to load your account information."
            );

        } finally {

            setLoading(false);

        }
    };


    // ==========================================================
    // INITIAL LOAD
    // ==========================================================

    useEffect(() => {

        loadAccounts();

    }, []);


    // ==========================================================
    // OPEN CREATE MODAL
    // ==========================================================

    const openCreateModal = () => {

        if (creatingAccount) {
            return;
        }


        setAccountType("SAVINGS");

        setCreateError("");

        setCreateSuccess("");

        setShowCreateModal(true);

    };


    // ==========================================================
    // CLOSE CREATE MODAL
    // ==========================================================

    const closeCreateModal = () => {

        if (creatingAccount) {
            return;
        }


        setShowCreateModal(false);

        setCreateError("");

        setCreateSuccess("");

    };


    // ==========================================================
    // CREATE ACCOUNT
    // ==========================================================

    const createAccount = async () => {

        if (creatingAccount) {
            return;
        }


        try {

            setCreatingAccount(true);

            setCreateError("");

            setCreateSuccess("");


            const response = await api.post(
                "/accounts",
                {
                    type: accountType,
                }
            );


            console.log(
                "Account created successfully:",
                response.data
            );


            setCreateSuccess(
                "Your new account has been created successfully."
            );


            await loadAccounts();


            setTimeout(() => {

                setShowCreateModal(false);

                setCreateSuccess("");

            }, 1200);

        } catch (error) {

            console.error(
                "Failed to create account:",
                error
            );


            if (
                error.response?.status === 401
            ) {

                navigate(
                    "/login",
                    {
                        replace: true,
                    }
                );

                return;
            }


            const backendMessage =
                error.response?.data?.message ||
                error.response?.data?.error;


            setCreateError(
                backendMessage ||
                "Unable to create the account. Please try again."
            );

        } finally {

            setCreatingAccount(false);

        }
    };


    // ==========================================================
    // FORMAT CURRENCY
    // ==========================================================

    const formatCurrency = (value) => {

        return new Intl.NumberFormat(
            "en-IN",
            {
                style: "currency",
                currency: "INR",
                maximumFractionDigits: 2,
            }
        ).format(
            Number(value || 0)
        );

    };


    // ==========================================================
    // FORMAT DATE
    // ==========================================================

    const formatDate = (value) => {

        if (!value) {
            return "—";
        }


        const date = new Date(value);


        if (
            Number.isNaN(
                date.getTime()
            )
        ) {

            return "—";

        }


        return date.toLocaleDateString(
            "en-IN",
            {
                day: "2-digit",
                month: "long",
                year: "numeric",
            }
        );

    };


    // ==========================================================
    // ACCOUNT TYPE
    // ==========================================================

    const formatAccountType = (type) => {

        if (!type) {
            return "Bank Account";
        }


        return String(type)
            .toLowerCase()
            .replaceAll("_", " ")
            .replace(
                /\b\w/g,
                (letter) =>
                    letter.toUpperCase()
            );

    };


    // ==========================================================
    // LOADING
    // ==========================================================

    if (loading) {

        return (
            <div className="dashboard-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            BANKING
                        </p>

                        <h1>
                            Your Accounts
                        </h1>

                        <p className="page-description">
                            Loading your account information...
                        </p>

                    </div>

                </section>


                <div className="dashboard-panel">

                    <div className="empty-state">

                        <div className="empty-icon">
                            <CreditCard size={22} />
                        </div>

                        <h3>
                            Loading accounts
                        </h3>

                        <p>
                            Connecting securely to BankCore.
                        </p>

                    </div>

                </div>

            </div>
        );

    }


    // ==========================================================
    // ERROR
    // ==========================================================

    if (error) {

        return (
            <div className="dashboard-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            BANKING
                        </p>

                        <h1>
                            Your Accounts
                        </h1>

                        <p className="page-description">
                            Manage your BankCore accounts.
                        </p>

                    </div>

                </section>


                <div className="dashboard-panel">

                    <div className="empty-state">

                        <div className="empty-icon">
                            <ShieldCheck size={22} />
                        </div>

                        <h3>
                            Unable to load accounts
                        </h3>

                        <p>
                            {error}
                        </p>


                        <button
                            type="button"
                            className="open-account-button"
                            onClick={loadAccounts}
                        >
                            Try again
                        </button>

                    </div>

                </div>

            </div>
        );

    }


    // ==========================================================
    // PAGE
    // ==========================================================

    return (
        <div className="dashboard-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <section className="page-header">

                <div>

                    <p className="eyebrow">
                        BANKING
                    </p>

                    <h1>
                        Your Accounts
                    </h1>

                    <p className="page-description">
                        View and manage your BankCore accounts.
                    </p>

                </div>


                {/* ==================================================
                    OPEN NEW ACCOUNT
                ================================================== */}

                <button
                    type="button"
                    className="open-account-button"
                    onClick={() => {
                        setAccountType("SAVINGS");
                        setCreateError("");
                        setCreateSuccess("");
                        setShowCreateModal(true);
                    }}
                    aria-label="Open a new bank account"
                >

                    <Plus size={17} />

                    <span>
                        Open New Account
                    </span>

                </button>

            </section>


            {/* ==================================================
                TOTAL BALANCE
            ================================================== */}

            <section className="dashboard-grid">

                <div className="balance-card">

                    <div className="card-top">

                        <div>

                            <p className="card-label">
                                TOTAL BALANCE
                            </p>

                            <div className="balance-value">
                                {formatCurrency(
                                    totalBalance
                                )}
                            </div>

                            <p className="balance-note">
                                Across all your accounts
                            </p>

                        </div>


                        <div className="card-icon balance-icon">

                            <WalletCards size={21} />

                        </div>

                    </div>


                    <div className="balance-footer">

                        <div className="balance-status">

                            <span className="status-dot" />

                            {accounts.length}{" "}

                            {accounts.length === 1
                                ? "account"
                                : "accounts"}

                        </div>

                        <span>
                            INR
                        </span>

                    </div>

                </div>

            </section>


            {/* ==================================================
                ACCOUNT CARDS
            ================================================== */}

            <section className="quick-actions">

                <div className="section-heading">

                    <div>

                        <h2>
                            Your Accounts
                        </h2>

                        <p>
                            Active BankCore accounts
                        </p>

                    </div>

                </div>


                {accounts.length === 0 ? (

                    <div className="dashboard-panel">

                        <div className="empty-state">

                            <div className="empty-icon">
                                <CreditCard size={22} />
                            </div>

                            <h3>
                                No accounts found
                            </h3>

                            <p>
                                There are currently no accounts
                                associated with your BankCore profile.
                            </p>


                            <button
                                type="button"
                                className="open-account-button"
                                onClick={() => {
                                    setAccountType("SAVINGS");
                                    setCreateError("");
                                    setCreateSuccess("");
                                    setShowCreateModal(true);
                                }}
                            >

                                <Plus size={17} />

                                Open New Account

                            </button>

                        </div>

                    </div>

                ) : (

                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns:
                                "repeat(auto-fit, minmax(340px, 1fr))",
                            gap: "18px",
                        }}
                    >

                        {accounts.map(
                            (account) => (

                                <div
                                    key={account.id}
                                    className="dashboard-panel"
                                    style={{
                                        position: "relative",
                                        overflow: "hidden",
                                    }}
                                >

                                    {/* CARD HEADER */}

                                    <div
                                        style={{
                                            display: "flex",
                                            alignItems: "flex-start",
                                            justifyContent: "space-between",
                                            marginBottom: "28px",
                                        }}
                                    >

                                        <div
                                            className="card-icon balance-icon"
                                        >

                                            <CreditCard
                                                size={21}
                                            />

                                        </div>


                                        <div
                                            style={{
                                                display: "flex",
                                                alignItems: "center",
                                                gap: "6px",
                                                fontSize: "12px",
                                                fontWeight: 600,
                                            }}
                                        >

                                            <CheckCircle2
                                                size={15}
                                                style={{
                                                    color: "#22c55e",
                                                }}
                                            />

                                            {account.status ||
                                                "ACTIVE"}

                                        </div>

                                    </div>


                                    {/* ACCOUNT TYPE */}

                                    <p
                                        className="card-label"
                                        style={{
                                            marginBottom: "8px",
                                        }}
                                    >

                                        {formatAccountType(
                                            account.type
                                        )}

                                    </p>


                                    {/* BALANCE */}

                                    <div
                                        style={{
                                            fontSize: "30px",
                                            fontWeight: 700,
                                            letterSpacing: "-0.03em",
                                            marginBottom: "20px",
                                        }}
                                    >

                                        {formatCurrency(
                                            account.balance
                                        )}

                                    </div>


                                    {/* ACCOUNT NUMBER */}

                                    <div
                                        style={{
                                            padding: "14px",
                                            borderRadius: "10px",
                                            background:
                                                "rgba(255,255,255,0.035)",
                                            marginBottom: "18px",
                                        }}
                                    >

                                        <div
                                            style={{
                                                fontSize: "10px",
                                                letterSpacing: "0.12em",
                                                textTransform: "uppercase",
                                                opacity: 0.5,
                                                marginBottom: "6px",
                                            }}
                                        >
                                            Account Number
                                        </div>


                                        <div
                                            style={{
                                                fontSize: "14px",
                                                fontWeight: 600,
                                                letterSpacing: "0.04em",
                                            }}
                                        >

                                            {account.accountNumber ||
                                                "—"}

                                        </div>

                                    </div>


                                    {/* ACCOUNT DETAILS */}

                                    <div
                                        style={{
                                            display: "grid",
                                            gridTemplateColumns:
                                                "1fr 1fr",
                                            gap: "12px",
                                        }}
                                    >

                                        <AccountDetail
                                            icon={CalendarDays}
                                            label="Opened"
                                            value={
                                                formatDate(
                                                    account.createdAt
                                                )
                                            }
                                        />


                                        <AccountDetail
                                            icon={ShieldCheck}
                                            label="Status"
                                            value={
                                                account.status ||
                                                "ACTIVE"
                                            }
                                        />

                                    </div>


                                    {/* CARD FOOTER */}

                                    <div
                                        style={{
                                            marginTop: "22px",
                                            paddingTop: "16px",
                                            borderTop:
                                                "1px solid rgba(255,255,255,0.06)",
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent:
                                                "space-between",
                                        }}
                                    >

                                        <span
                                            style={{
                                                fontSize: "12px",
                                                opacity: 0.5,
                                            }}
                                        >
                                            Currency
                                        </span>

                                        <span
                                            style={{
                                                fontSize: "12px",
                                                fontWeight: 600,
                                            }}
                                        >

                                            {account.currency ||
                                                "INR"}

                                        </span>

                                    </div>

                                </div>

                            )
                        )}

                    </div>

                )}

            </section>


            {/* ==================================================
                ACCOUNT ACTIONS
            ================================================== */}

            <section className="quick-actions">

                <div className="section-heading">

                    <div>

                        <h2>
                            Account Actions
                        </h2>

                        <p>
                            Frequently used banking actions
                        </p>

                    </div>

                </div>


                <div className="quick-action-grid">

                    <button
                        type="button"
                        className="quick-action"
                        onClick={() =>
                            navigate("/transfer")
                        }
                    >

                        <div className="quick-action-icon">
                            <ArrowUpRight size={20} />
                        </div>

                        <div className="quick-action-content">

                            <strong>
                                Transfer Money
                            </strong>

                            <span>
                                Send money securely
                            </span>

                        </div>

                        <ArrowUpRight
                            size={17}
                            className="quick-action-arrow"
                        />

                    </button>


                    <button
                        type="button"
                        className="quick-action"
                        onClick={() =>
                            navigate("/transactions")
                        }
                    >

                        <div className="quick-action-icon">
                            <CreditCard size={20} />
                        </div>

                        <div className="quick-action-content">

                            <strong>
                                View Transactions
                            </strong>

                            <span>
                                Review account activity
                            </span>

                        </div>

                        <ArrowUpRight
                            size={17}
                            className="quick-action-arrow"
                        />

                    </button>

                </div>

            </section>


            {/* ==================================================
                CREATE ACCOUNT MODAL
            ================================================== */}

            {showCreateModal && (

                <div
                    className="bankcore-modal-overlay"
                    onMouseDown={(event) => {

                        if (
                            event.target ===
                            event.currentTarget &&
                            !creatingAccount
                        ) {

                            closeCreateModal();

                        }

                    }}
                >

                    <div
                        className="bankcore-modal"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="create-account-title"
                    >

                        {/* MODAL HEADER */}

                        <div
                            className="bankcore-modal-header"
                        >

                            <div
                                style={{
                                    display: "flex",
                                    alignItems: "center",
                                    gap: "12px",
                                }}
                            >

                                <div
                                    className="bankcore-modal-icon"
                                >

                                    <Building2
                                        size={20}
                                    />

                                </div>

                                <div>

                                    <h2
                                        id="create-account-title"
                                    >
                                        Open New Account
                                    </h2>

                                    <p>
                                        Add another account to your
                                        BankCore profile.
                                    </p>

                                </div>

                            </div>


                            <button
                                type="button"
                                className="bankcore-modal-close"
                                onClick={closeCreateModal}
                                disabled={creatingAccount}
                                aria-label="Close"
                            >

                                <X size={18} />

                            </button>

                        </div>


                        {/* MODAL BODY */}

                        <div
                            className="bankcore-modal-body"
                        >

                            <div
                                className="bankcore-form-group"
                            >

                                <label>
                                    Account Type
                                </label>


                                <div
                                    className="bankcore-account-type-options"
                                >

                                    {/* SAVINGS */}

                                    <button
                                        type="button"
                                        className={`bankcore-account-type-option ${
                                            accountType === "SAVINGS"
                                                ? "selected"
                                                : ""
                                        }`}
                                        onClick={() =>
                                            setAccountType(
                                                "SAVINGS"
                                            )
                                        }
                                        disabled={
                                            creatingAccount
                                        }
                                    >

                                        <div
                                            className="bankcore-account-type-icon"
                                        >

                                            <WalletCards
                                                size={19}
                                            />

                                        </div>


                                        <div
                                            className="bankcore-account-type-content"
                                        >

                                            <strong>
                                                Savings Account
                                            </strong>

                                            <span>
                                                For everyday saving
                                                and spending
                                            </span>

                                        </div>


                                        <span
                                            className="bankcore-radio"
                                        />

                                    </button>


                                    {/* CURRENT */}

                                    <button
                                        type="button"
                                        className={`bankcore-account-type-option ${
                                            accountType === "CURRENT"
                                                ? "selected"
                                                : ""
                                        }`}
                                        onClick={() =>
                                            setAccountType(
                                                "CURRENT"
                                            )
                                        }
                                        disabled={
                                            creatingAccount
                                        }
                                    >

                                        <div
                                            className="bankcore-account-type-icon"
                                        >

                                            <CreditCard
                                                size={19}
                                            />

                                        </div>


                                        <div
                                            className="bankcore-account-type-content"
                                        >

                                            <strong>
                                                Current Account
                                            </strong>

                                            <span>
                                                For business and
                                                frequent transactions
                                            </span>

                                        </div>


                                        <span
                                            className="bankcore-radio"
                                        />

                                    </button>

                                </div>

                            </div>


                            {/* ZERO BALANCE */}

                            <div
                                className="bankcore-zero-balance"
                            >

                                <div
                                    className="bankcore-zero-balance-icon"
                                >

                                    <WalletCards
                                        size={17}
                                    />

                                </div>


                                <div>

                                    <strong>
                                        Starting balance
                                    </strong>

                                    <span>
                                        ₹0.00
                                    </span>

                                    <p>
                                        New accounts start with a
                                        zero balance. You can fund
                                        the account through a transfer.
                                    </p>

                                </div>

                            </div>


                            {/* ERROR */}

                            {createError && (

                                <div
                                    className="bankcore-create-error"
                                >

                                    <strong>
                                        Account creation failed
                                    </strong>

                                    <span>
                                        {createError}
                                    </span>

                                </div>

                            )}


                            {/* SUCCESS */}

                            {createSuccess && (

                                <div
                                    className="bankcore-create-success"
                                >

                                    <CheckCircle2
                                        size={18}
                                    />

                                    <span>
                                        {createSuccess}
                                    </span>

                                </div>

                            )}

                        </div>


                        {/* MODAL FOOTER */}

                        <div
                            className="bankcore-modal-footer"
                        >

                            <button
                                type="button"
                                className="bankcore-modal-cancel"
                                onClick={closeCreateModal}
                                disabled={creatingAccount}
                            >
                                Cancel
                            </button>


                            <button
                                type="button"
                                className="bankcore-modal-create"
                                onClick={createAccount}
                                disabled={
                                    creatingAccount ||
                                    !!createSuccess
                                }
                            >

                                {creatingAccount ? (

                                    <>
                                        <Loader2
                                            size={17}
                                            className="bankcore-spin"
                                        />

                                        Creating...
                                    </>

                                ) : (

                                    <>
                                        <Plus size={17} />

                                        Create Account
                                    </>

                                )}

                            </button>

                        </div>

                    </div>

                </div>

            )}

        </div>
    );
}


// ============================================================
// ACCOUNT DETAIL
// ============================================================

function AccountDetail({
                           icon: Icon,
                           label,
                           value,
                       }) {

    return (
        <div
            style={{
                display: "flex",
                alignItems: "center",
                gap: "9px",
            }}
        >

            <Icon
                size={15}
                style={{
                    opacity: 0.55,
                }}
            />


            <div>

                <div
                    style={{
                        fontSize: "10px",
                        textTransform: "uppercase",
                        letterSpacing: "0.08em",
                        opacity: 0.45,
                        marginBottom: "3px",
                    }}
                >
                    {label}
                </div>


                <div
                    style={{
                        fontSize: "12px",
                        fontWeight: 600,
                    }}
                >
                    {value}
                </div>

            </div>

        </div>
    );
}


export default AccountsPage;