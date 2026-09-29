import { useEffect, useMemo, useState } from "react";

import {
    ArrowDown,
    ArrowUp,
    BarChart3,
    CreditCard,
    RefreshCw,
    Receipt,
    TrendingDown,
    TrendingUp,
    Wallet,
} from "lucide-react";

import {
    Bar,
    BarChart,
    CartesianGrid,
    Cell,
    Legend,
    Pie,
    PieChart,
    ResponsiveContainer,
    Tooltip,
    XAxis,
    YAxis,
} from "recharts";

import api from "../api";

import "./SpendingPage.css";


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


const formatNumber = (number) => {

    return new Intl.NumberFormat(
        "en-IN"
    ).format(
        Number(number || 0)
    );
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


// ============================================================
// COMPONENT
// ============================================================

export default function SpendingPage() {

    const [analysis, setAnalysis] =
        useState(null);

    const [categoryData, setCategoryData] =
        useState(null);

    const [loading, setLoading] =
        useState(true);

    const [refreshing, setRefreshing] =
        useState(false);

    const [error, setError] =
        useState("");


    // ========================================================
    // FETCH DATA
    // ========================================================

    const fetchSpending = async (
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
                analysisResponse,
                categoryResponse,
            ] = await Promise.all([

                api.get(
                    "/spending/analysis"
                ),

                api.get(
                    "/spending/categories"
                ),
            ]);


            setAnalysis(
                analysisResponse.data
            );

            setCategoryData(
                categoryResponse.data
            );

        } catch (err) {

            console.error(
                "Failed to load spending data:",
                err
            );

            setError(
                err.response?.data?.message ||
                "Unable to load spending data."
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

        fetchSpending();

    }, []);


    // ========================================================
    // CHART DATA
    // ========================================================

    const chartData = useMemo(() => {

        if (
            !categoryData ||
            !Array.isArray(
                categoryData.categories
            )
        ) {
            return [];
        }

        return categoryData.categories
            .map((item) => ({
                category:
                    getCategoryLabel(
                        item.category
                    ),

                categoryCode:
                item.category,

                amount:
                    Number(
                        item.amount || 0
                    ),

                transactions:
                    Number(
                        item.transactionCount || 0
                    ),
            }))
            .sort(
                (a, b) =>
                    b.amount - a.amount
            );

    }, [categoryData]);


    // ========================================================
    // TOTAL SPENDING
    // ========================================================

    const totalSpending =
        Number(
            analysis?.totalSpent ||
            categoryData?.totalSpending ||
            0
        );


    // ========================================================
    // TOP CATEGORY
    // ========================================================

    const topCategory =
        chartData.length > 0
            ? chartData[0]
            : null;


    // ========================================================
    // AVERAGE / LARGEST / SMALLEST
    // ========================================================

    const average =
        Number(
            analysis?.averageTransactionAmount ||
            0
        );

    const largest =
        Number(
            analysis?.largestTransactionAmount ||
            0
        );

    const smallest =
        Number(
            analysis?.smallestTransactionAmount ||
            0
        );


    // ========================================================
    // CHART COLORS
    // ========================================================

    const chartColors = [
        "#6F7CFF",
        "#38BDF8",
        "#34D399",
        "#A78BFA",
        "#FBBF24",
        "#FB7185",
        "#22D3EE",
        "#818CF8",
        "#94A3B8",
    ];


    // ========================================================
    // LOADING
    // ========================================================

    if (loading) {

        return (
            <div className="spending-page">

                <div className="spending-loading">

                    <div className="spending-spinner" />

                    <p>
                        Loading spending data...
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
            <div className="spending-page">

                <div className="spending-error">

                    <div className="spending-error-icon">
                        <CreditCard size={34} />
                    </div>

                    <h2>
                        Unable to load spending
                    </h2>

                    <p>
                        {error}
                    </p>

                    <button
                        onClick={() =>
                            fetchSpending()
                        }
                    >
                        <RefreshCw size={15} />

                        Try Again
                    </button>

                </div>

            </div>
        );
    }


    // ========================================================
    // MAIN
    // ========================================================

    return (
        <div className="spending-page">


            {/* ==================================================
                HEADER
            ================================================== */}

            <div className="spending-header">

                <div>

                    <div className="spending-eyebrow">
                        SPENDING ANALYTICS
                    </div>

                    <h1>
                        Your Spending
                    </h1>

                    <p>
                        Understand where your money
                        is going across your BankCore
                        transactions.
                    </p>

                </div>


                <button
                    className="spending-refresh-button"
                    onClick={() =>
                        fetchSpending(true)
                    }
                    disabled={refreshing}
                >

                    <RefreshCw
                        size={17}
                        className={
                            refreshing
                                ? "spending-refresh-spin"
                                : ""
                        }
                    />

                    {refreshing
                        ? "Refreshing..."
                        : "Refresh"}

                </button>

            </div>


            {/* ==================================================
                TOP SUMMARY
            ================================================== */}

            <div className="spending-summary-grid">


                {/* TOTAL SPENDING */}

                <div className="spending-summary-card primary">

                    <div className="spending-summary-icon">

                        <Wallet
                            size={20}
                        />

                    </div>

                    <div>

                        <span>
                            Total Spending
                        </span>

                        <strong>
                            {formatCurrency(
                                totalSpending
                            )}
                        </strong>

                        <small>
                            {formatNumber(
                                analysis?.transactionCount
                            )}{" "}
                            completed transactions
                        </small>

                    </div>

                </div>


                {/* AVERAGE */}

                <div className="spending-summary-card">

                    <div className="spending-summary-icon blue">

                        <BarChart3
                            size={20}
                        />

                    </div>

                    <div>

                        <span>
                            Average Transaction
                        </span>

                        <strong>
                            {formatCurrency(
                                average
                            )}
                        </strong>

                        <small>
                            Per transaction
                        </small>

                    </div>

                </div>


                {/* TOP CATEGORY */}

                <div className="spending-summary-card">

                    <div className="spending-summary-icon purple">

                        <TrendingUp
                            size={20}
                        />

                    </div>

                    <div>

                        <span>
                            Highest Category
                        </span>

                        <strong>
                            {topCategory
                                ? topCategory.category
                                : "—"}
                        </strong>

                        <small>
                            {topCategory
                                ? formatCurrency(
                                    topCategory.amount
                                )
                                : "No spending yet"}
                        </small>

                    </div>

                </div>

            </div>


            {/* ==================================================
                ANALYSIS CARDS
            ================================================== */}

            <div className="spending-stat-grid">


                <div className="spending-stat-card">

                    <div className="spending-stat-top">

                        <span>
                            Largest Transaction
                        </span>

                        <div className="stat-icon red">

                            <ArrowUp
                                size={17}
                            />

                        </div>

                    </div>

                    <strong>
                        {formatCurrency(
                            largest
                        )}
                    </strong>

                    <small>
                        Highest outgoing transaction
                    </small>

                </div>


                <div className="spending-stat-card">

                    <div className="spending-stat-top">

                        <span>
                            Smallest Transaction
                        </span>

                        <div className="stat-icon green">

                            <ArrowDown
                                size={17}
                            />

                        </div>

                    </div>

                    <strong>
                        {formatCurrency(
                            smallest
                        )}
                    </strong>

                    <small>
                        Lowest outgoing transaction
                    </small>

                </div>


                <div className="spending-stat-card">

                    <div className="spending-stat-top">

                        <span>
                            Net Spending
                        </span>

                        <div className="stat-icon orange">

                            <TrendingDown
                                size={17}
                            />

                        </div>

                    </div>

                    <strong>
                        {formatCurrency(
                            Math.abs(
                                Number(
                                    analysis?.netSpending ||
                                    0
                                )
                            )
                        )}
                    </strong>

                    <small>
                        Outgoing money flow
                    </small>

                </div>


                <div className="spending-stat-card">

                    <div className="spending-stat-top">

                        <span>
                            Categories
                        </span>

                        <div className="stat-icon blue">

                            <Receipt
                                size={17}
                            />

                        </div>

                    </div>

                    <strong>
                        {chartData.length}
                    </strong>

                    <small>
                        Categories with spending
                    </small>

                </div>

            </div>


            {/* ==================================================
                CHARTS
            ================================================== */}

            <div className="spending-chart-grid">


                {/* CATEGORY BAR CHART */}

                <div className="spending-chart-card">

                    <div className="spending-chart-header">

                        <div>

                            <div className="section-eyebrow">
                                BREAKDOWN
                            </div>

                            <h2>
                                Spending by Category
                            </h2>

                            <p>
                                Amount spent across
                                each category.
                            </p>

                        </div>

                    </div>


                    {chartData.length === 0 ? (

                        <div className="spending-empty">

                            <BarChart3
                                size={32}
                            />

                            <p>
                                No categorized
                                spending yet.
                            </p>

                        </div>

                    ) : (

                        <div className="spending-bar-chart">

                            <ResponsiveContainer
                                width="100%"
                                height={330}
                            >

                                <BarChart
                                    data={chartData}
                                    margin={{
                                        top: 10,
                                        right: 10,
                                        left: 0,
                                        bottom: 10,
                                    }}
                                >

                                    <CartesianGrid
                                        stroke="#26334E"
                                        strokeDasharray="3 3"
                                        vertical={false}
                                    />

                                    <XAxis
                                        dataKey="category"
                                        tick={{
                                            fontSize: 11,
                                            fill: "#91A0BC",
                                        }}
                                        axisLine={{
                                            stroke: "#26334E",
                                        }}
                                        tickLine={false}
                                    />

                                    <YAxis
                                        tick={{
                                            fontSize: 11,
                                            fill: "#91A0BC",
                                        }}
                                        axisLine={false}
                                        tickLine={false}
                                        tickFormatter={
                                            (value) =>
                                                `₹${value}`
                                        }
                                    />

                                    <Tooltip
                                        cursor={{
                                            fill: "rgba(89, 108, 240, 0.08)",
                                        }}
                                        contentStyle={{
                                            background:
                                                "#111927",
                                            border:
                                                "1px solid #26334E",
                                            borderRadius:
                                                "10px",
                                            color:
                                                "#EDF2FF",
                                            boxShadow:
                                                "0 10px 30px rgba(0,0,0,0.35)",
                                        }}
                                        labelStyle={{
                                            color:
                                                "#EDF2FF",
                                            fontWeight:
                                                600,
                                        }}
                                        itemStyle={{
                                            color:
                                                "#91A0BC",
                                        }}
                                        formatter={(
                                            value
                                        ) =>
                                            formatCurrency(
                                                value
                                            )
                                        }
                                    />

                                    <Bar
                                        dataKey="amount"
                                        radius={[
                                            5,
                                            5,
                                            0,
                                            0,
                                        ]}
                                    >

                                        {chartData.map(
                                            (
                                                entry,
                                                index
                                            ) => (

                                                <Cell
                                                    key={
                                                        entry.categoryCode
                                                    }
                                                    fill={
                                                        chartColors[
                                                        index %
                                                        chartColors.length
                                                            ]
                                                    }
                                                />

                                            )
                                        )}

                                    </Bar>

                                </BarChart>

                            </ResponsiveContainer>

                        </div>

                    )}

                </div>


                {/* PIE CHART */}

                <div className="spending-chart-card">

                    <div className="spending-chart-header">

                        <div>

                            <div className="section-eyebrow">
                                DISTRIBUTION
                            </div>

                            <h2>
                                Spending Distribution
                            </h2>

                            <p>
                                How your spending is
                                distributed.
                            </p>

                        </div>

                    </div>


                    {chartData.length === 0 ? (

                        <div className="spending-empty">

                            <CreditCard
                                size={32}
                            />

                            <p>
                                No spending data available.
                            </p>

                        </div>

                    ) : (

                        <div className="spending-pie-wrapper">

                            <ResponsiveContainer
                                width="100%"
                                height={260}
                            >

                                <PieChart>

                                    <Pie
                                        data={chartData}
                                        dataKey="amount"
                                        nameKey="category"
                                        cx="50%"
                                        cy="50%"
                                        outerRadius={92}
                                        innerRadius={52}
                                        paddingAngle={2}
                                    >

                                        {chartData.map(
                                            (
                                                entry,
                                                index
                                            ) => (

                                                <Cell
                                                    key={
                                                        entry.categoryCode
                                                    }
                                                    fill={
                                                        chartColors[
                                                        index %
                                                        chartColors.length
                                                            ]
                                                    }
                                                />

                                            )
                                        )}

                                    </Pie>

                                    <Tooltip
                                        contentStyle={{
                                            background:
                                                "#111927",
                                            border:
                                                "1px solid #26334E",
                                            borderRadius:
                                                "10px",
                                            color:
                                                "#EDF2FF",
                                            boxShadow:
                                                "0 10px 30px rgba(0,0,0,0.35)",
                                        }}
                                        labelStyle={{
                                            color:
                                                "#EDF2FF",
                                        }}
                                        itemStyle={{
                                            color:
                                                "#91A0BC",
                                        }}
                                        formatter={(
                                            value
                                        ) =>
                                            formatCurrency(
                                                value
                                            )
                                        }
                                    />

                                </PieChart>

                            </ResponsiveContainer>


                            <div className="spending-legend">

                                {chartData.map(
                                    (
                                        item,
                                        index
                                    ) => {

                                        const percentage =
                                            totalSpending > 0
                                                ? (
                                                item.amount /
                                                totalSpending
                                            ) * 100
                                                : 0;

                                        return (

                                            <div
                                                className="spending-legend-item"
                                                key={
                                                    item.categoryCode
                                                }
                                            >

                                                <span>

                                                    <i
                                                        style={{
                                                            background:
                                                                chartColors[
                                                                index %
                                                                chartColors.length
                                                                    ],
                                                        }}
                                                    />

                                                    {item.category}

                                                </span>

                                                <strong>
                                                    {percentage.toFixed(
                                                        1
                                                    )}
                                                    %
                                                </strong>

                                            </div>

                                        );

                                    }
                                )}

                            </div>

                        </div>

                    )}

                </div>

            </div>


            {/* ==================================================
                CATEGORY TABLE
            ================================================== */}

            <div className="spending-category-card">

                <div className="spending-chart-header">

                    <div>

                        <div className="section-eyebrow">
                            DETAILS
                        </div>

                        <h2>
                            Category Breakdown
                        </h2>

                        <p>
                            Detailed view of your
                            categorized spending.
                        </p>

                    </div>

                </div>


                {chartData.length === 0 ? (

                    <div className="spending-empty">

                        <p>
                            No category data available.
                        </p>

                    </div>

                ) : (

                    <div className="category-table-wrapper">

                        <table className="category-table">

                            <thead>

                            <tr>

                                <th>
                                    Category
                                </th>

                                <th>
                                    Transactions
                                </th>

                                <th>
                                    Amount
                                </th>

                                <th>
                                    Share
                                </th>

                            </tr>

                            </thead>

                            <tbody>

                            {chartData.map(
                                (item) => {

                                    const percentage =
                                        totalSpending > 0
                                            ? (
                                            item.amount /
                                            totalSpending
                                        ) * 100
                                            : 0;

                                    return (

                                        <tr
                                            key={
                                                item.categoryCode
                                            }
                                        >

                                            <td>

                                                <div className="category-name">

                                                        <span
                                                            className="category-dot"
                                                            style={{
                                                                background:
                                                                    chartColors[
                                                                    chartData.indexOf(
                                                                        item
                                                                    ) %
                                                                    chartColors.length
                                                                        ],
                                                            }}
                                                        />

                                                    <strong>
                                                        {
                                                            item.category
                                                        }
                                                    </strong>

                                                </div>

                                            </td>

                                            <td>
                                                {
                                                    item.transactions
                                                }
                                            </td>

                                            <td>

                                                <strong>
                                                    {formatCurrency(
                                                        item.amount
                                                    )}
                                                </strong>

                                            </td>

                                            <td>

                                                <div className="category-share">

                                                    <div className="share-bar">

                                                            <span
                                                                style={{
                                                                    width:
                                                                        `${Math.min(
                                                                            percentage,
                                                                            100
                                                                        )}%`,
                                                                }}
                                                            />

                                                    </div>

                                                    <small>
                                                        {percentage.toFixed(
                                                            1
                                                        )}
                                                        %
                                                    </small>

                                                </div>

                                            </td>

                                        </tr>
                                    );
                                }
                            )}

                            </tbody>

                        </table>

                    </div>

                )}

            </div>

        </div>
    );
}