import {
    LayoutDashboard,
    CreditCard,
    ArrowLeftRight,
    ReceiptText,
    PieChart,
    WalletCards,
    HeartPulse,
    Sparkles,
    LogOut,
    Bell,
    Search,
    ChevronDown,
    Menu,
    X,
    TrendingUp,
    ArrowUpRight,
    ArrowDownLeft,
    ShieldCheck,
    RefreshCw,
    Target,
} from "lucide-react";

import {
    NavLink,
    Navigate,
    Route,
    Routes,
    useLocation,
    useNavigate,
} from "react-router-dom";

import {
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    Login,
    Register,
} from "./pages/AuthPages";

import AccountsPage from "./pages/AccountsPage";
import TransferPage from "./pages/TransferPage";
import TransactionsPage from "./pages/TransactionsPage";
import SpendingPage from "./pages/SpendingPage";
import BudgetPage from "./pages/BudgetPage";
import FinancialHealthPage from "./pages/FinancialHealthPage";
import AiPage from "./pages/AiPage";
import SavingsGoalsPage from "./pages/SavingsGoalsPage";

import api from "./api";


// ============================================================
// NAVIGATION
// ============================================================

const navigation = [
    {
        label: "Overview",
        path: "/",
        icon: LayoutDashboard,
    },
    {
        label: "Accounts",
        path: "/accounts",
        icon: CreditCard,
    },
    {
        label: "Transfer",
        path: "/transfer",
        icon: ArrowLeftRight,
    },
    {
        label: "Transactions",
        path: "/transactions",
        icon: ReceiptText,
    },
    {
        label: "Spending",
        path: "/spending",
        icon: PieChart,
    },
    {
        label: "Budgets",
        path: "/budgets",
        icon: WalletCards,
    },
    {
        label: "Savings Goals",
        path: "/savings-goals",
        icon: Target,
    },
    {
        label: "Financial Health",
        path: "/financial-health",
        icon: HeartPulse,
    },
];


// ============================================================
// AUTH HELPERS
// ============================================================

function isAuthenticated() {

    return Boolean(
        localStorage.getItem(
            "bankcore_token"
        )
    );
}


// ============================================================
// PROTECTED ROUTE
// ============================================================

function ProtectedRoute({
                            children,
                        }) {

    if (!isAuthenticated()) {

        return (
            <Navigate
                to="/login"
                replace
            />
        );
    }


    return children;
}


// ============================================================
// APP
// ============================================================

function App() {

    const [sidebarOpen, setSidebarOpen] =
        useState(false);


    return (
        <Routes>

            {/* ==================================================
                PUBLIC AUTH ROUTES
            ================================================== */}

            <Route
                path="/login"
                element={<Login />}
            />

            <Route
                path="/register"
                element={<Register />}
            />


            {/* ==================================================
                PROTECTED APPLICATION
            ================================================== */}

            <Route
                path="*"
                element={
                    <ProtectedRoute>
                        <BankingLayout
                            sidebarOpen={
                                sidebarOpen
                            }
                            setSidebarOpen={
                                setSidebarOpen
                            }
                        />
                    </ProtectedRoute>
                }
            />

        </Routes>
    );
}


// ============================================================
// BANKING LAYOUT
// ============================================================

function BankingLayout({
                           sidebarOpen,
                           setSidebarOpen,
                       }) {

    const location =
        useLocation();

    const navigate =
        useNavigate();


    const handleLogout = () => {

        localStorage.removeItem(
            "bankcore_token"
        );

        localStorage.removeItem(
            "bankcore_user"
        );

        navigate("/login", {
            replace: true,
        });
    };


    return (
        <div className="app-shell">

            {/* ==================================================
                MOBILE OVERLAY
            ================================================== */}

            {sidebarOpen && (
                <div
                    className="sidebar-overlay"
                    onClick={() =>
                        setSidebarOpen(false)
                    }
                />
            )}


            {/* ==================================================
                SIDEBAR
            ================================================== */}

            <aside
                className={`sidebar ${
                    sidebarOpen
                        ? "sidebar-open"
                        : ""
                }`}
            >

                <div className="sidebar-header">

                    <div className="brand">

                        <div className="brand-mark">
                            B
                        </div>

                        <div>

                            <div className="brand-name">
                                BankCore
                            </div>

                            <div className="brand-subtitle">
                                Personal Banking
                            </div>

                        </div>

                    </div>


                    <button
                        className="mobile-close-button"
                        onClick={() =>
                            setSidebarOpen(false)
                        }
                    >
                        <X size={20} />
                    </button>

                </div>


                <nav className="sidebar-navigation">

                    <div className="navigation-section-title">
                        BANKING
                    </div>


                    {navigation.map((item) => {

                        const Icon =
                            item.icon;


                        return (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                end={
                                    item.path === "/"
                                }
                                onClick={() =>
                                    setSidebarOpen(
                                        false
                                    )
                                }
                                className={({
                                                isActive,
                                            }) =>
                                    `sidebar-link ${
                                        isActive
                                            ? "sidebar-link-active"
                                            : ""
                                    }`
                                }
                            >

                                <Icon size={19} />

                                <span>
                      {item.label}
                    </span>

                            </NavLink>
                        );

                    })}


                    <div className="navigation-section-title ai-section-title">
                        INTELLIGENCE
                    </div>


                    <NavLink
                        to="/ai"
                        onClick={() =>
                            setSidebarOpen(
                                false
                            )
                        }
                        className={({
                                        isActive,
                                    }) =>
                            `sidebar-link ai-link ${
                                isActive
                                    ? "sidebar-link-active"
                                    : ""
                            }`
                        }
                    >

                        <Sparkles size={19} />

                        <span>
                BankCore AI
              </span>

                        <span className="ai-badge">
                AI
              </span>

                    </NavLink>

                </nav>


                <div className="sidebar-bottom">

                    <div className="security-card">

                        <div className="security-icon">
                            <ShieldCheck size={18} />
                        </div>

                        <div>

                            <div className="security-title">
                                Secure Banking
                            </div>

                            <div className="security-text">
                                Your data is protected
                            </div>

                        </div>

                    </div>


                    <button
                        className="sidebar-logout"
                        onClick={
                            handleLogout
                        }
                    >

                        <LogOut size={18} />

                        <span>
                Logout
              </span>

                    </button>

                </div>

            </aside>


            {/* ==================================================
                MAIN AREA
            ================================================== */}

            <main className="main-area">

                <header className="topbar">

                    <div className="topbar-left">

                        <button
                            className="mobile-menu-button"
                            onClick={() =>
                                setSidebarOpen(
                                    true
                                )
                            }
                        >
                            <Menu size={21} />
                        </button>


                        <div>

                            <div className="breadcrumb">

                                BankCore

                                <span>
                    /
                  </span>

                                {
                                    getPageName(
                                        location.pathname
                                    )
                                }

                            </div>

                        </div>

                    </div>


                    <div className="topbar-right">

                        <button className="topbar-icon-button">
                            <Search size={19} />
                        </button>

                        <button className="topbar-icon-button notification-button">

                            <Bell size={19} />

                            <span className="notification-dot" />

                        </button>


                        <div className="topbar-divider" />


                        <ProfileButton />

                    </div>

                </header>


                <div className="page-content">

                    <Routes>

                        <Route
                            path="/"
                            element={<Dashboard />}
                        />

                        <Route
                            path="/accounts"
                            element={<AccountsPage />}
                        />

                        <Route
                            path="/transfer"
                            element={<TransferPage />}
                        />

                        <Route
                            path="/transactions"
                            element={
                                <ProtectedRoute>
                                    <TransactionsPage />
                                </ProtectedRoute>
                            }
                        />

                        <Route
                            path="/spending"
                            element={
                                <ProtectedRoute>
                                    <SpendingPage />
                                </ProtectedRoute>
                            }
                        />

                        <Route
                            path="/budgets"
                            element={
                                <ProtectedRoute>
                                    <BudgetPage />
                                </ProtectedRoute>
                            }
                        />

                        <Route
                            path="/savings-goals"
                            element={
                                <ProtectedRoute>
                                    <SavingsGoalsPage />
                                </ProtectedRoute>
                            }
                        />

                        <Route
                            path="/financial-health"
                            element={
                                <ProtectedRoute>
                                    <FinancialHealthPage />
                                </ProtectedRoute>
                            }
                        />

                        <Route
                            path="/ai"
                            element={
                                <ProtectedRoute>
                                    <AiPage />
                                </ProtectedRoute>
                            }
                        />

                        <Route
                            path="*"
                            element={
                                <Navigate
                                    to="/"
                                    replace
                                />
                            }
                        />

                    </Routes>

                </div>

            </main>

        </div>
    );
}


// ============================================================
// PROFILE BUTTON
// ============================================================

function ProfileButton() {

    const storedUser =
        localStorage.getItem(
            "bankcore_user"
        );


    let user = {};

    try {

        user =
            storedUser
                ? JSON.parse(storedUser)
                : {};

    } catch {

        user = {};

    }


    const name =
        user.name ||
        "Aayush";


    const email =
        user.email ||
        "";


    const initial =
        name
            .trim()
            .charAt(0)
            .toUpperCase() ||
        "A";


    return (
        <button className="profile-button">

            <div className="profile-avatar">
                {initial}
            </div>

            <div className="profile-info">

          <span className="profile-name">
            {name}
          </span>

                <span className="profile-role">
            {email ||
                "Personal Account"}
          </span>

            </div>

            <ChevronDown size={16} />

        </button>
    );
}


// ============================================================
// DASHBOARD
// ============================================================

function Dashboard() {

    const navigate =
        useNavigate();


    // ==========================================================
    // DASHBOARD STATE
    // ==========================================================

    const [dashboardData, setDashboardData] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    // ==========================================================
    // LOAD DASHBOARD
    // ==========================================================

    const loadDashboard = async () => {

        try {

            setLoading(true);

            setError("");


            const response =
                await api.get(
                    "/dashboard"
                );


            setDashboardData(
                response.data
            );

        } catch (error) {

            console.error(
                "Failed to load dashboard:",
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
                "Unable to load your banking data."
            );

        } finally {

            setLoading(false);

        }
    };


    // ==========================================================
    // INITIAL LOAD
    // ==========================================================

    useEffect(() => {

        loadDashboard();

    }, []);


    // ==========================================================
    // DATA
    // ==========================================================

    const totalBalance =
        dashboardData?.totalBalance ??
        0;


    const financialInsights =
        dashboardData?.financialInsights ??
        {};


    const moneySent =
        financialInsights.totalSent ??
        0;


    const moneyReceived =
        financialInsights.totalReceived ??
        0;


    const currentMonthSpending =
        dashboardData?.currentMonthSpending ??
        {};


    /*
     * The backend's monthly spending DTO has been
     * kept flexible here so the frontend remains
     * compatible with the existing response.
     */
    const monthlySpending =
        currentMonthSpending.totalSpent ??
        currentMonthSpending.totalSpending ??
        currentMonthSpending.amount ??
        0;


    const categorySpending =
        dashboardData?.categorySpending ??
        {};


    const categories =
        categorySpending.categories ??
        [];


    const recentTransactions =
        dashboardData?.recentTransactions ??
        [];


    // ==========================================================
    // CATEGORY DATA
    // ==========================================================

    const categoryData =
        useMemo(() => {

            return categories
                .filter(
                    (item) =>
                        Number(
                            item.amount
                        ) > 0
                )
                .sort(
                    (a, b) =>
                        Number(b.amount) -
                        Number(a.amount)
                );

        }, [categories]);


    const categoryTotal =
        categoryData.reduce(
            (total, item) =>
                total +
                Number(item.amount || 0),
            0
        );


    // ==========================================================
    // DONUT GRADIENT
    // ==========================================================

    const donutGradient =
        useMemo(() => {

            if (
                categoryTotal <= 0 ||
                categoryData.length === 0
            ) {

                return undefined;
            }


            const categoryColors = [
                "#8b5cf6",
                "#60a5fa",
                "#f59e0b",
                "#22c55e",
                "#ec4899",
                "#14b8a6",
                "#f97316",
                "#a78bfa",
                "#94a3b8",
            ];


            let currentPercentage = 0;


            const stops =
                categoryData.map(
                    (item, index) => {

                        const percentage =
                            (
                                Number(
                                    item.amount
                                ) /
                                categoryTotal
                            ) *
                            100;


                        const start =
                            currentPercentage;

                        const end =
                            currentPercentage +
                            percentage;


                        currentPercentage =
                            end;


                        const color =
                            categoryColors[
                            index %
                            categoryColors.length
                                ];


                        return `${color} ${start}% ${end}%`;

                    }
                );


            return `conic-gradient(${stops.join(", ")})`;

        }, [
            categoryData,
            categoryTotal,
        ]);


    // ==========================================================
    // FORMAT HELPERS
    // ==========================================================

    const formatCurrency =
        (value) => {

            const numericValue =
                Number(value || 0);


            return new Intl.NumberFormat(
                "en-IN",
                {
                    style: "currency",
                    currency: "INR",
                    maximumFractionDigits: 0,
                }
            ).format(
                numericValue
            );
        };


    const formatDate =
        (value) => {

            if (!value) {
                return "";
            }


            const date =
                new Date(value);


            if (
                Number.isNaN(
                    date.getTime()
                )
            ) {

                return String(value);

            }


            return date.toLocaleDateString(
                "en-IN",
                {
                    day: "2-digit",
                    month: "short",
                    year: "numeric",
                }
            );
        };


    const getTransactionDescription =
        (transaction) => {

            return (
                transaction.description ||
                transaction.transactionReference ||
                "Banking transaction"
            );

        };


    const getTransactionDirection =
        (transaction) => {

            if (
                transaction.direction
            ) {

                return String(
                    transaction.direction
                ).toUpperCase();

            }


            return "SENT";

        };


    // ==========================================================
    // LOADING STATE
    // ==========================================================

    if (loading) {

        return (
            <div className="dashboard-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            PERSONAL BANKING
                        </p>

                        <h1>
                            Good morning, Aayush
                        </h1>

                        <p className="page-description">
                            Loading your financial overview...
                        </p>

                    </div>

                </section>


                <section className="dashboard-grid">

                    <DashboardLoadingCard />

                    <DashboardLoadingCard />

                    <DashboardLoadingCard />

                    <DashboardLoadingCard />

                </section>


                <section className="dashboard-main-grid">

                    <div className="dashboard-panel transactions-panel">

                        <div className="panel-header">

                            <div>

                                <h2>
                                    Recent Transactions
                                </h2>

                                <p>
                                    Your latest banking activity
                                </p>

                            </div>

                        </div>

                        <div className="empty-state">

                            <RefreshCw
                                size={24}
                                className="loading-spin"
                            />

                            <h3>
                                Loading transaction data
                            </h3>

                            <p>
                                Connecting securely to BankCore.
                            </p>

                        </div>

                    </div>


                    <div className="dashboard-panel">

                        <div className="panel-header">

                            <div>

                                <h2>
                                    Spending Overview
                                </h2>

                                <p>
                                    Current month
                                </p>

                            </div>

                        </div>

                        <div className="empty-state">

                            <RefreshCw
                                size={24}
                                className="loading-spin"
                            />

                            <p>
                                Loading spending data...
                            </p>

                        </div>

                    </div>

                </section>

            </div>
        );
    }


    // ==========================================================
    // ERROR STATE
    // ==========================================================

    if (error) {

        return (
            <div className="dashboard-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            PERSONAL BANKING
                        </p>

                        <h1>
                            Good morning, Aayush
                        </h1>

                        <p className="page-description">
                            Here's an overview of your finances.
                        </p>

                    </div>

                </section>


                <div className="dashboard-panel">

                    <div className="empty-state">

                        <div className="empty-icon">
                            <ShieldCheck size={22} />
                        </div>

                        <h3>
                            Unable to load dashboard
                        </h3>

                        <p>
                            {error}
                        </p>


                        <button
                            className="primary-button"
                            onClick={
                                loadDashboard
                            }
                        >

                            <RefreshCw size={17} />

                            Try again

                        </button>

                    </div>

                </div>

            </div>
        );
    }


    // ==========================================================
    // DASHBOARD
    // ==========================================================

    return (
        <div className="dashboard-page">

            <section className="page-header">

                <div>

                    <p className="eyebrow">
                        PERSONAL BANKING
                    </p>

                    <h1>
                        Good morning, Aayush
                    </h1>

                    <p className="page-description">
                        Here's an overview of your finances.
                    </p>

                </div>


                <button
                    className="primary-button"
                    onClick={() =>
                        navigate("/transfer")
                    }
                >

                    <ArrowLeftRight size={17} />

                    Transfer Money

                </button>

            </section>


            {/* ==================================================
                METRIC CARDS
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
                                Across your active accounts
                            </p>

                        </div>

                        <div className="card-icon balance-icon">
                            <WalletCards size={21} />
                        </div>

                    </div>


                    <div className="balance-footer">

                        <div className="balance-status">

                            <span className="status-dot" />

                            Account connected

                        </div>

                        <span>
                INR
              </span>

                    </div>

                </div>


                <DashboardMetricCard
                    title="MONEY SENT"
                    value={
                        formatCurrency(
                            moneySent
                        )
                    }
                    subtitle={`${financialInsights.sentTransactionCount || 0} transactions`}
                    icon={ArrowUpRight}
                    iconClass="sent-icon"
                />


                <DashboardMetricCard
                    title="MONEY RECEIVED"
                    value={
                        formatCurrency(
                            moneyReceived
                        )
                    }
                    subtitle={`${financialInsights.receivedTransactionCount || 0} transactions`}
                    icon={ArrowDownLeft}
                    iconClass="received-icon"
                />


                <DashboardMetricCard
                    title="MONTHLY SPENDING"
                    value={
                        formatCurrency(
                            monthlySpending
                        )
                    }
                    subtitle="Current month"
                    icon={TrendingUp}
                    iconClass="spending-icon"
                />

            </section>


            {/* ==================================================
                MAIN DASHBOARD PANELS
            ================================================== */}

            <section className="dashboard-main-grid">

                {/* ==================================================
                  RECENT TRANSACTIONS
              ================================================== */}

                <div className="dashboard-panel transactions-panel">

                    <div className="panel-header">

                        <div>

                            <h2>
                                Recent Transactions
                            </h2>

                            <p>
                                Your latest banking activity
                            </p>

                        </div>


                        <button
                            className="text-button"
                            onClick={() =>
                                navigate(
                                    "/transactions"
                                )
                            }
                        >
                            View all
                        </button>

                    </div>


                    {recentTransactions.length === 0 ? (

                        <div className="empty-state">

                            <div className="empty-icon">
                                <ReceiptText size={22} />
                            </div>

                            <h3>
                                No transactions yet
                            </h3>

                            <p>
                                Your completed transactions
                                will appear here.
                            </p>

                        </div>

                    ) : (

                        <div
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                gap: "4px",
                            }}
                        >

                            {recentTransactions.map(
                                (transaction, index) => {

                                    const direction =
                                        getTransactionDirection(
                                            transaction
                                        );


                                    const isReceived =
                                        direction ===
                                        "RECEIVED";


                                    return (
                                        <div
                                            key={
                                                transaction.id ||
                                                transaction.transactionReference ||
                                                index
                                            }
                                            style={{
                                                display: "flex",
                                                alignItems: "center",
                                                justifyContent: "space-between",
                                                padding: "14px 4px",
                                                borderBottom:
                                                    index <
                                                    recentTransactions.length - 1
                                                        ? "1px solid rgba(255,255,255,0.06)"
                                                        : "none",
                                            }}
                                        >

                                            <div
                                                style={{
                                                    display: "flex",
                                                    alignItems: "center",
                                                    gap: "12px",
                                                    minWidth: 0,
                                                }}
                                            >

                                                <div
                                                    style={{
                                                        width: "38px",
                                                        height: "38px",
                                                        borderRadius: "10px",
                                                        display: "flex",
                                                        alignItems: "center",
                                                        justifyContent: "center",
                                                        background:
                                                            isReceived
                                                                ? "rgba(34,197,94,0.12)"
                                                                : "rgba(245,158,11,0.12)",
                                                        color:
                                                            isReceived
                                                                ? "#22c55e"
                                                                : "#f59e0b",
                                                        flexShrink: 0,
                                                    }}
                                                >

                                                    {isReceived ? (
                                                        <ArrowDownLeft
                                                            size={18}
                                                        />
                                                    ) : (
                                                        <ArrowUpRight
                                                            size={18}
                                                        />
                                                    )}

                                                </div>


                                                <div
                                                    style={{
                                                        minWidth: 0,
                                                    }}
                                                >

                                                    <div
                                                        style={{
                                                            fontWeight: 600,
                                                            whiteSpace: "nowrap",
                                                            overflow: "hidden",
                                                            textOverflow: "ellipsis",
                                                        }}
                                                    >
                                                        {
                                                            getTransactionDescription(
                                                                transaction
                                                            )
                                                        }
                                                    </div>


                                                    <div
                                                        style={{
                                                            fontSize: "12px",
                                                            opacity: 0.55,
                                                            marginTop: "3px",
                                                        }}
                                                    >

                                                        {
                                                            formatDate(
                                                                transaction.createdAt
                                                            )
                                                        }

                                                        {transaction.status
                                                            ? ` • ${transaction.status}`
                                                            : ""}

                                                    </div>

                                                </div>

                                            </div>


                                            <div
                                                style={{
                                                    textAlign: "right",
                                                    marginLeft: "12px",
                                                    flexShrink: 0,
                                                }}
                                            >

                                                <div
                                                    style={{
                                                        fontWeight: 700,
                                                    }}
                                                >

                                                    {isReceived
                                                        ? "+"
                                                        : "-"}

                                                    {formatCurrency(
                                                        transaction.amount
                                                    )}

                                                </div>


                                                <div
                                                    style={{
                                                        fontSize: "11px",
                                                        opacity: 0.5,
                                                        marginTop: "3px",
                                                    }}
                                                >

                                                    {transaction.currency ||
                                                        "INR"}

                                                </div>

                                            </div>

                                        </div>
                                    );

                                }
                            )}

                        </div>

                    )}

                </div>


                {/* ==================================================
                  SPENDING OVERVIEW
              ================================================== */}

                <div className="dashboard-panel">

                    <div className="panel-header">

                        <div>

                            <h2>
                                Spending Overview
                            </h2>

                            <p>
                                Current month
                            </p>

                        </div>

                        <PieChart
                            size={20}
                            className="panel-icon"
                        />

                    </div>


                    {categoryData.length === 0 ? (

                        <div className="empty-state">

                            <div className="empty-icon">
                                <PieChart size={22} />
                            </div>

                            <h3>
                                No categorized spending
                            </h3>

                            <p>
                                Categorized spending will appear
                                here as transaction data becomes
                                available.
                            </p>

                        </div>

                    ) : (

                        <div className="spending-placeholder">

                            <div
                                className="donut-placeholder"
                                style={
                                    donutGradient
                                        ? {
                                            background:
                                            donutGradient,
                                        }
                                        : undefined
                                }
                            >

                                <div>

                      <span>
                        {formatCurrency(
                            categoryTotal
                        )}
                      </span>

                                    <small>
                                        INR
                                    </small>

                                </div>

                            </div>


                            <div className="spending-legend">

                                {categoryData
                                    .slice(0, 5)
                                    .map(
                                        (
                                            category,
                                            index
                                        ) => {

                                            const categoryColors = [
                                                "#8b5cf6",
                                                "#60a5fa",
                                                "#f59e0b",
                                                "#22c55e",
                                                "#ec4899",
                                            ];


                                            return (
                                                <div
                                                    className="legend-item"
                                                    key={
                                                        category.category
                                                    }
                                                >

                                    <span
                                        className="legend-dot"
                                        style={{
                                            background:
                                                categoryColors[
                                                index %
                                                categoryColors.length
                                                    ],
                                        }}
                                    />


                                                    <span>
                                      {formatCategoryName(
                                          category.category
                                      )}
                                    </span>


                                                    <strong>
                                                        {formatCurrency(
                                                            category.amount
                                                        )}
                                                    </strong>

                                                </div>
                                            );

                                        }
                                    )}

                            </div>

                        </div>

                    )}

                </div>

            </section>


            {/* ==================================================
                QUICK ACTIONS
            ================================================== */}

            <section className="quick-actions">

                <div className="section-heading">

                    <div>

                        <h2>
                            Quick Actions
                        </h2>

                        <p>
                            Frequently used banking tools
                        </p>

                    </div>

                </div>


                <div className="quick-action-grid">

                    <QuickAction
                        icon={ArrowLeftRight}
                        title="Transfer Money"
                        description="Send money securely"
                        path="/transfer"
                    />

                    <QuickAction
                        icon={ReceiptText}
                        title="Transactions"
                        description="View your activity"
                        path="/transactions"
                    />

                    <QuickAction
                        icon={WalletCards}
                        title="Manage Budget"
                        description="Track spending limits"
                        path="/budgets"
                    />

                    <QuickAction
                        icon={Sparkles}
                        title="Ask BankCore AI"
                        description="Get financial insights"
                        path="/ai"
                        highlight
                    />

                </div>

            </section>

        </div>
    );
}


// ============================================================
// DASHBOARD LOADING CARD
// ============================================================

function DashboardLoadingCard() {

    return (
        <div
            className="metric-card"
            style={{
                minHeight: "160px",
            }}
        >

            <div
                style={{
                    width: "42%",
                    height: "10px",
                    borderRadius: "6px",
                    background:
                        "rgba(255,255,255,0.07)",
                    marginBottom: "18px",
                }}
            />

            <div
                style={{
                    width: "65%",
                    height: "30px",
                    borderRadius: "8px",
                    background:
                        "rgba(255,255,255,0.08)",
                    marginBottom: "14px",
                }}
            />

            <div
                style={{
                    width: "50%",
                    height: "9px",
                    borderRadius: "6px",
                    background:
                        "rgba(255,255,255,0.05)",
                }}
            />

        </div>
    );
}


// ============================================================
// METRIC CARD
// ============================================================

function DashboardMetricCard({
                                 title,
                                 value,
                                 subtitle,
                                 icon: Icon,
                                 iconClass,
                             }) {

    return (
        <div className="metric-card">

            <div className="metric-card-top">

                <div>

                    <p className="card-label">
                        {title}
                    </p>

                    <div className="metric-value">
                        {value}
                    </div>

                </div>


                <div
                    className={`card-icon ${iconClass}`}
                >
                    <Icon size={20} />
                </div>

            </div>


            <p className="metric-subtitle">
                {subtitle}
            </p>

        </div>
    );
}


// ============================================================
// QUICK ACTION
// ============================================================

function QuickAction({
                         icon: Icon,
                         title,
                         description,
                         path,
                         highlight = false,
                     }) {

    const navigate =
        useNavigate();


    return (
        <button
            className={`quick-action ${
                highlight
                    ? "quick-action-highlight"
                    : ""
            }`}
            onClick={() =>
                navigate(path)
            }
        >

            <div className="quick-action-icon">
                <Icon size={20} />
            </div>

            <div className="quick-action-content">

                <strong>
                    {title}
                </strong>

                <span>
            {description}
          </span>

            </div>

            <ArrowUpRight
                size={17}
                className="quick-action-arrow"
            />

        </button>
    );
}


// ============================================================
// PLACEHOLDER PAGE
// ============================================================

function PlaceholderPage({
                             title,
                             description,
                             icon: Icon,
                             ai = false,
                         }) {

    return (
        <div className="placeholder-page">

            <div
                className={`placeholder-icon ${
                    ai
                        ? "placeholder-ai"
                        : ""
                }`}
            >
                <Icon size={30} />
            </div>

            <p className="eyebrow">
                {ai
                    ? "INTELLIGENCE"
                    : "BANKING"}
            </p>

            <h1>
                {title}
            </h1>

            <p>
                {description}
            </p>

            <div className="coming-soon">

                <span />

                Building this section next

            </div>

        </div>
    );
}


// ============================================================
// CATEGORY NAME
// ============================================================

function formatCategoryName(
    category
) {

    if (!category) {
        return "Other";
    }


    const value =
        String(category)
            .toLowerCase()
            .replaceAll(
                "_",
                " "
            );


    return value
        .replace(
            /\b\w/g,
            (letter) =>
                letter.toUpperCase()
        );
}


// ============================================================
// PAGE NAME
// ============================================================

function getPageName(path) {

    const names = {

        "/":
            "Overview",

        "/accounts":
            "Accounts",

        "/transfer":
            "Transfer",

        "/transactions":
            "Transactions",

        "/spending":
            "Spending",

        "/budgets":
            "Budgets",

        "/savings-goals":
            "Savings Goals",

        "/financial-health":
            "Financial Health",

        "/ai":
            "BankCore AI",

    };


    return (
        names[path] ||
        "Banking"
    );
}


export default App;
