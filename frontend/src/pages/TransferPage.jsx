import {
    ArrowLeftRight,
    ArrowUpRight,
    CheckCircle2,
    CircleAlert,
    Loader2,
    RefreshCw,
    Search,
    ShieldCheck,
    UserRound,
    WalletCards,
    XCircle,
} from "lucide-react";

import {
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";

import {
    useNavigate,
} from "react-router-dom";

import api from "../api";
import "./TransferPage.css";

// ============================================================
// HELPERS
// ============================================================

function generateIdempotencyKey() {

    if (
        typeof crypto !== "undefined" &&
        crypto.randomUUID
    ) {

        return crypto.randomUUID();
    }

    return (
        "bankcore-" +
        Date.now() +
        "-" +
        Math.random()
            .toString(36)
            .substring(2)
    );
}


function formatCurrency(
    amount,
    currency = "INR"
) {

    const numericAmount =
        Number(amount || 0);

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency,
            maximumFractionDigits: 2,
        }
    ).format(numericAmount);
}


function formatAccountNumber(
    accountNumber
) {

    if (!accountNumber) {
        return "";
    }

    const value =
        String(accountNumber);

    if (value.length <= 8) {
        return value;
    }

    return (
        value.substring(0, 4) +
        "••••" +
        value.substring(value.length - 4)
    );
}


function getErrorMessage(error) {

    if (
        error?.response?.data?.message
    ) {

        return error.response.data.message;
    }

    if (
        error?.response?.data?.error
    ) {

        return error.response.data.error;
    }

    if (error?.message) {

        return error.message;
    }

    return "An unexpected error occurred";
}


// ============================================================
// TRANSFER PAGE
// ============================================================

function TransferPage() {

    const navigate =
        useNavigate();


    // ==========================================================
    // STATE
    // ==========================================================

    const [
        accounts,
        setAccounts
    ] = useState([]);


    const [
        selectedAccountId,
        setSelectedAccountId
    ] = useState("");


    const [
        recipientAccountNumber,
        setRecipientAccountNumber
    ] = useState("");


    const [
        recipient,
        setRecipient
    ] = useState(null);


    const [
        amount,
        setAmount
    ] = useState("");


    const [
        description,
        setDescription
    ] = useState("");


    const [
        loadingAccounts,
        setLoadingAccounts
    ] = useState(true);


    const [
        loadingRecipient,
        setLoadingRecipient
    ] = useState(false);


    const [
        submitting,
        setSubmitting
    ] = useState(false);


    const [
        accountError,
        setAccountError
    ] = useState("");


    const [
        recipientError,
        setRecipientError
    ] = useState("");


    const [
        transferError,
        setTransferError
    ] = useState("");


    const [
        successResponse,
        setSuccessResponse
    ] = useState(null);


    // ==========================================================
    // LOAD MY ACCOUNTS
    // ==========================================================

    const loadAccounts =
        useCallback(
            async () => {

                setLoadingAccounts(true);

                setAccountError("");

                try {

                    const response =
                        await api.get(
                            "/accounts"
                        );

                    const loadedAccounts =
                        Array.isArray(
                            response.data
                        )
                            ? response.data
                            : response.data?.accounts || [];

                    setAccounts(
                        loadedAccounts
                    );

                    const activeAccounts =
                        loadedAccounts.filter(
                            (account) =>
                                account.status ===
                                "ACTIVE"
                        );

                    if (
                        activeAccounts.length > 0
                    ) {

                        setSelectedAccountId(
                            String(
                                activeAccounts[0].id
                            )
                        );
                    }

                } catch (error) {

                    console.error(
                        "Failed to load BankCore accounts:",
                        error
                    );

                    setAccounts([]);

                    setAccountError(
                        getErrorMessage(error)
                    );

                } finally {

                    setLoadingAccounts(false);
                }

            },
            []
        );


    useEffect(
        () => {

            loadAccounts();

        },
        [loadAccounts]
    );


    // ==========================================================
    // SELECTED SOURCE ACCOUNT
    // ==========================================================

    const selectedAccount =
        useMemo(
            () => {

                return accounts.find(
                    (account) =>
                        String(account.id) ===
                        String(selectedAccountId)
                );

            },
            [
                accounts,
                selectedAccountId,
            ]
        );


    // ==========================================================
    // RECIPIENT LOOKUP
    // ==========================================================

    const verifyRecipient =
        async () => {

            setRecipientError("");

            setRecipient(null);

            setTransferError("");

            setSuccessResponse(null);


            const normalizedAccountNumber =
                recipientAccountNumber
                    .trim()
                    .toUpperCase();


            if (!normalizedAccountNumber) {

                setRecipientError(
                    "Enter the recipient account number."
                );

                return;
            }


            if (
                normalizedAccountNumber.length < 6
            ) {

                setRecipientError(
                    "Please enter a valid BankCore account number."
                );

                return;
            }


            if (
                selectedAccount &&
                normalizedAccountNumber ===
                String(
                    selectedAccount.accountNumber
                ).toUpperCase()
            ) {

                setRecipientError(
                    "You cannot transfer money to your own account."
                );

                return;
            }


            setLoadingRecipient(true);


            try {

                const response =
                    await api.get(
                        "/accounts/lookup",
                        {
                            params: {
                                accountNumber:
                                normalizedAccountNumber,
                            },
                        }
                    );


                const recipientData =
                    response.data;


                if (
                    !recipientData ||
                    !recipientData.accountId
                ) {

                    throw new Error(
                        "Recipient account could not be verified."
                    );
                }


                if (
                    selectedAccount &&
                    String(
                        recipientData.accountId
                    ) ===
                    String(
                        selectedAccount.id
                    )
                ) {

                    setRecipientError(
                        "You cannot transfer money to your own account."
                    );

                    setRecipient(null);

                    return;
                }


                setRecipient(
                    recipientData
                );


            } catch (error) {

                console.error(
                    "Recipient verification failed:",
                    error
                );

                setRecipient(null);

                setRecipientError(
                    getErrorMessage(error)
                );

            } finally {

                setLoadingRecipient(false);
            }
        };


    // ==========================================================
    // CHANGE RECIPIENT NUMBER
    // ==========================================================

    const handleRecipientChange =
        (event) => {

            setRecipientAccountNumber(
                event.target.value
            );

            setRecipient(null);

            setRecipientError("");

            setTransferError("");

            setSuccessResponse(null);
        };


    // ==========================================================
    // CHANGE SOURCE ACCOUNT
    // ==========================================================

    const handleSourceAccountChange =
        (event) => {

            setSelectedAccountId(
                event.target.value
            );

            setRecipient(null);

            setRecipientError("");

            setTransferError("");

            setSuccessResponse(null);
        };


    // ==========================================================
    // AMOUNT CHANGE
    // ==========================================================

    const handleAmountChange =
        (event) => {

            const value =
                event.target.value;

            if (value === "") {

                setAmount("");

                return;
            }

            if (
                !/^\d*\.?\d{0,2}$/.test(
                    value
                )
            ) {

                return;
            }

            setAmount(value);

            setTransferError("");

            setSuccessResponse(null);
        };


    // ==========================================================
    // TRANSFER VALIDATION
    // ==========================================================

    const validateTransfer =
        () => {

            if (!selectedAccount) {

                return "Please select a source account.";
            }


            if (
                selectedAccount.status !==
                "ACTIVE"
            ) {

                return "The selected source account is not active.";
            }


            if (!recipient) {

                return "Please verify the recipient account first.";
            }


            const numericAmount =
                Number(amount);


            if (
                !amount ||
                !Number.isFinite(
                    numericAmount
                ) ||
                numericAmount <= 0
            ) {

                return "Enter a valid transfer amount.";
            }


            if (
                Number(
                    selectedAccount.balance
                ) < numericAmount
            ) {

                return "Insufficient account balance.";
            }


            if (
                Number(
                    selectedAccount.balance
                ) === numericAmount
            ) {

                return "The transfer amount must leave enough balance in the account.";
            }


            return null;
        };


    // ==========================================================
    // SUBMIT TRANSFER
    // ==========================================================

    const handleTransfer =
        async (event) => {

            event.preventDefault();

            setTransferError("");

            setSuccessResponse(null);


            const validationError =
                validateTransfer();


            if (validationError) {

                setTransferError(
                    validationError
                );

                return;
            }


            setSubmitting(true);


            try {

                const idempotencyKey =
                    generateIdempotencyKey();


                const response =
                    await api.post(
                        "/transfers",
                        {
                            fromAccountId:
                                Number(
                                    selectedAccount.id
                                ),

                            toAccountId:
                                Number(
                                    recipient.accountId
                                ),

                            amount:
                                Number(amount),

                            description:
                                description.trim() ||
                                null,
                        },
                        {
                            headers: {
                                "Idempotency-Key":
                                idempotencyKey,
                            },
                        }
                    );


                setSuccessResponse(
                    response.data
                );


                setAmount("");

                setDescription("");

                setRecipientAccountNumber("");

                setRecipient(null);


                // Refresh the source balance.
                await loadAccounts();


            } catch (error) {

                console.error(
                    "Transfer failed:",
                    error
                );

                setTransferError(
                    getErrorMessage(error)
                );

            } finally {

                setSubmitting(false);
            }
        };


    // ==========================================================
    // LOADING ACCOUNTS
    // ==========================================================

    if (loadingAccounts) {

        return (
            <div className="transfer-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            MONEY TRANSFER
                        </p>

                        <h1>
                            Transfer Money
                        </h1>

                        <p className="page-description">
                            Send money securely between BankCore accounts.
                        </p>

                    </div>

                    <div className="transfer-security-label">

                        <ShieldCheck size={18} />

                        Secure transfer

                    </div>

                </section>


                <div
                    style={{
                        padding: "48px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        gap: "12px",
                    }}
                >

                    <Loader2
                        size={22}
                        className="spin"
                    />

                    Loading your accounts...

                </div>

            </div>
        );
    }


    // ==========================================================
    // ACCOUNT LOAD ERROR
    // ==========================================================

    if (accountError) {

        return (
            <div className="transfer-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            MONEY TRANSFER
                        </p>

                        <h1>
                            Transfer Money
                        </h1>

                        <p className="page-description">
                            Send money securely between BankCore accounts.
                        </p>

                    </div>

                    <div className="transfer-security-label">

                        <ShieldCheck size={18} />

                        Secure transfer

                    </div>

                </section>


                <div
                    style={{
                        maxWidth: "760px",
                        padding: "32px",
                    }}
                >

                    <CircleAlert
                        size={28}
                    />

                    <h2>
                        Transfer unavailable
                    </h2>

                    <p>
                        {accountError}
                    </p>

                    <button
                        className="primary-button"
                        onClick={loadAccounts}
                    >

                        <RefreshCw size={17} />

                        Retry

                    </button>

                </div>

            </div>
        );
    }


    // ==========================================================
    // NO ACTIVE ACCOUNT
    // ==========================================================

    if (accounts.length === 0) {

        return (
            <div className="transfer-page">

                <section className="page-header">

                    <div>

                        <p className="eyebrow">
                            MONEY TRANSFER
                        </p>

                        <h1>
                            Transfer Money
                        </h1>

                        <p className="page-description">
                            Send money securely between BankCore accounts.
                        </p>

                    </div>

                    <div className="transfer-security-label">

                        <ShieldCheck size={18} />

                        Secure transfer

                    </div>

                </section>


                <div
                    style={{
                        maxWidth: "760px",
                        padding: "32px",
                    }}
                >

                    <WalletCards
                        size={30}
                    />

                    <h2>
                        No accounts available
                    </h2>

                    <p>
                        You need an active BankCore account before
                        you can make a transfer.
                    </p>

                    <button
                        className="primary-button"
                        onClick={() =>
                            navigate("/accounts")
                        }
                    >
                        View Accounts
                    </button>

                </div>

            </div>
        );
    }


    // ==========================================================
    // MAIN PAGE
    // ==========================================================

    return (
        <div className="transfer-page">

            {/* ==================================================
                PAGE HEADER
            ================================================== */}

            <section className="page-header">

                <div>

                    <p className="eyebrow">
                        MONEY TRANSFER
                    </p>

                    <h1>
                        Transfer Money
                    </h1>

                    <p className="page-description">
                        Send money securely between BankCore accounts.
                    </p>

                </div>


                <div className="transfer-security-label">

                    <ShieldCheck size={18} />

                    Secure transfer

                </div>

            </section>


            {/* ==================================================
                TRANSFER LAYOUT
            ================================================== */}

            <div className="transfer-layout">


                {/* ==================================================
                  FORM CARD
              ================================================== */}

                <section className="transfer-form-card">

                    <div className="transfer-card-header">

                        <div>

                            <p className="eyebrow">
                                SEND MONEY
                            </p>

                            <h2>
                                Make a transfer
                            </h2>

                            <p>
                                Transfer funds securely to another
                                BankCore account.
                            </p>

                        </div>

                        <div className="transfer-card-icon">

                            <ArrowLeftRight
                                size={22}
                            />

                        </div>

                    </div>


                    <form
                        onSubmit={
                            handleTransfer
                        }
                    >


                        {/* ==================================================
                      FROM ACCOUNT
                  ================================================== */}

                        <div className="form-group">

                            <label>
                                From account
                            </label>

                            <div className="input-with-icon">

                                <WalletCards
                                    size={18}
                                />

                                <select
                                    value={
                                        selectedAccountId
                                    }
                                    onChange={
                                        handleSourceAccountChange
                                    }
                                >

                                    {accounts
                                        .filter(
                                            (account) =>
                                                account.status ===
                                                "ACTIVE"
                                        )
                                        .map(
                                            (account) => (

                                                <option
                                                    key={
                                                        account.id
                                                    }
                                                    value={
                                                        account.id
                                                    }
                                                >

                                                    {account.type}
                                                    {" — "}
                                                    {formatAccountNumber(
                                                        account.accountNumber
                                                    )}
                                                    {" — "}
                                                    {formatCurrency(
                                                        account.balance,
                                                        account.currency
                                                    )}

                                                </option>

                                            )
                                        )}

                                </select>

                            </div>


                            {selectedAccount && (

                                <div className="available-balance">

                      <span>
                        Available balance
                      </span>

                                    <strong>
                                        {formatCurrency(
                                            selectedAccount.balance,
                                            selectedAccount.currency
                                        )}
                                    </strong>

                                </div>

                            )}

                        </div>


                        {/* ==================================================
                      RECIPIENT
                  ================================================== */}

                        <div className="form-group">

                            <label>
                                To recipient
                            </label>

                            <div
                                style={{
                                    display: "flex",
                                    gap: "10px",
                                    alignItems: "stretch",
                                }}
                            >

                                <div
                                    className="input-with-icon"
                                    style={{
                                        flex: 1,
                                    }}
                                >

                                    <UserRound
                                        size={18}
                                    />

                                    <input
                                        type="text"
                                        value={
                                            recipientAccountNumber
                                        }
                                        onChange={
                                            handleRecipientChange
                                        }
                                        placeholder="Enter recipient account number"
                                        autoComplete="off"
                                    />

                                </div>


                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={
                                        verifyRecipient
                                    }
                                    disabled={
                                        loadingRecipient
                                    }
                                >

                                    {loadingRecipient ? (

                                        <Loader2
                                            size={17}
                                            className="spin"
                                        />

                                    ) : (

                                        <Search
                                            size={17}
                                        />

                                    )}

                                    {loadingRecipient
                                        ? "Checking..."
                                        : "Verify"}

                                </button>

                            </div>


                            {/* ==================================================
                        RECIPIENT ERROR
                    ================================================== */}

                            {recipientError && (

                                <div
                                    style={{
                                        marginTop: "10px",
                                        display: "flex",
                                        alignItems: "center",
                                        gap: "8px",
                                    }}
                                >

                                    <XCircle
                                        size={17}
                                    />

                                    <span>
                        {recipientError}
                      </span>

                                </div>

                            )}


                            {/* ==================================================
                        VERIFIED RECIPIENT
                    ================================================== */}

                            {recipient && (

                                <div
                                    style={{
                                        marginTop: "14px",
                                        padding: "16px",
                                        border: "1px solid rgba(90, 220, 140, 0.25)",
                                        borderRadius: "12px",
                                        background:
                                            "rgba(60, 180, 100, 0.06)",
                                        display: "flex",
                                        alignItems: "center",
                                        gap: "14px",
                                    }}
                                >

                                    <div
                                        style={{
                                            width: "42px",
                                            height: "42px",
                                            borderRadius: "50%",
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            background:
                                                "rgba(90, 220, 140, 0.12)",
                                        }}
                                    >

                                        <CheckCircle2
                                            size={21}
                                        />

                                    </div>


                                    <div>

                                        <strong>
                                            {recipient.accountHolderName}
                                        </strong>

                                        <div
                                            style={{
                                                marginTop: "4px",
                                                fontSize: "13px",
                                                opacity: 0.65,
                                            }}
                                        >

                                            Account{" "}
                                            {formatAccountNumber(
                                                recipient.accountNumber
                                            )}

                                        </div>

                                    </div>

                                </div>

                            )}

                        </div>


                        {/* ==================================================
                      AMOUNT
                  ================================================== */}

                        <div className="form-group">

                            <label>
                                Amount
                            </label>

                            <div className="input-with-icon">

                  <span
                      style={{
                          fontSize: "18px",
                          fontWeight: 600,
                      }}
                  >
                    ₹
                  </span>

                                <input
                                    type="text"
                                    inputMode="decimal"
                                    value={amount}
                                    onChange={
                                        handleAmountChange
                                    }
                                    placeholder="0.00"
                                />

                            </div>

                        </div>


                        {/* ==================================================
                      DESCRIPTION
                  ================================================== */}

                        <div className="form-group">

                            <label>
                                Description
                                <span
                                    style={{
                                        opacity: 0.5,
                                        marginLeft: "6px",
                                    }}
                                >
                    Optional
                  </span>
                            </label>

                            <input
                                type="text"
                                value={description}
                                onChange={(event) =>
                                    setDescription(
                                        event.target.value
                                    )
                                }
                                placeholder="e.g. Monthly expenses"
                                maxLength={255}
                            />

                        </div>


                        {/* ==================================================
                      TRANSFER ERROR
                  ================================================== */}

                        {transferError && (

                            <div
                                style={{
                                    marginBottom: "18px",
                                    padding: "14px 16px",
                                    borderRadius: "10px",
                                    border:
                                        "1px solid rgba(255, 90, 90, 0.25)",
                                    background:
                                        "rgba(255, 70, 70, 0.06)",
                                    display: "flex",
                                    gap: "10px",
                                    alignItems: "flex-start",
                                }}
                            >

                                <CircleAlert
                                    size={19}
                                />

                                <span>
                      {transferError}
                    </span>

                            </div>

                        )}


                        {/* ==================================================
                      SUCCESS
                  ================================================== */}

                        {successResponse && (

                            <div
                                style={{
                                    marginBottom: "18px",
                                    padding: "18px",
                                    borderRadius: "10px",
                                    border:
                                        "1px solid rgba(90, 220, 140, 0.25)",
                                    background:
                                        "rgba(60, 180, 100, 0.06)",
                                }}
                            >

                                <div
                                    style={{
                                        display: "flex",
                                        alignItems: "center",
                                        gap: "10px",
                                        marginBottom: "8px",
                                    }}
                                >

                                    <CheckCircle2
                                        size={20}
                                    />

                                    <strong>
                                        Transfer successful
                                    </strong>

                                </div>


                                {successResponse.transactionReference && (

                                    <div
                                        style={{
                                            fontSize: "13px",
                                            opacity: 0.7,
                                        }}
                                    >

                                        Reference:{" "}
                                        {
                                            successResponse.transactionReference
                                        }

                                    </div>

                                )}

                            </div>

                        )}


                        {/* ==================================================
                      SUBMIT
                  ================================================== */}

                        <button
                            type="submit"
                            className="primary-button"
                            disabled={
                                submitting ||
                                loadingRecipient ||
                                !recipient
                            }
                            style={{
                                width: "100%",
                                justifyContent: "center",
                            }}
                        >

                            {submitting ? (

                                <>

                                    <Loader2
                                        size={18}
                                        className="spin"
                                    />

                                    Processing transfer...

                                </>

                            ) : (

                                <>

                                    <ArrowUpRight
                                        size={18}
                                    />

                                    Transfer Money

                                </>

                            )}

                        </button>


                        <div
                            style={{
                                marginTop: "16px",
                                display: "flex",
                                alignItems: "center",
                                gap: "8px",
                                fontSize: "13px",
                                opacity: 0.65,
                            }}
                        >

                            <ShieldCheck
                                size={16}
                            />

                            Your transfer is protected by
                            BankCore's authenticated and atomic
                            transaction system.

                        </div>

                    </form>

                </section>


                {/* ==================================================
                  INFORMATION CARD
              ================================================== */}

                <aside className="transfer-info-card">

                    <div className="transfer-info-header">

                        <ShieldCheck
                            size={22}
                        />

                        <h2>
                            Secure transfer
                        </h2>

                    </div>


                    <p>
                        BankCore processes transfers atomically.
                        The source account is debited and the
                        destination account is credited as one
                        transaction.
                    </p>


                    <div className="transfer-info-list">

                        <div>

                            <CheckCircle2
                                size={17}
                            />

                            <span>
                  Authenticated transfers
                </span>

                        </div>


                        <div>

                            <CheckCircle2
                                size={17}
                            />

                            <span>
                  Account ownership validation
                </span>

                        </div>


                        <div>

                            <CheckCircle2
                                size={17}
                            />

                            <span>
                  Balance validation
                </span>

                        </div>


                        <div>

                            <CheckCircle2
                                size={17}
                            />

                            <span>
                  Idempotent transaction processing
                </span>

                        </div>

                    </div>


                    <div
                        style={{
                            marginTop: "28px",
                            paddingTop: "22px",
                            borderTop:
                                "1px solid rgba(255,255,255,0.08)",
                        }}
                    >

                        <p
                            style={{
                                margin: 0,
                                fontSize: "13px",
                                opacity: 0.6,
                                lineHeight: 1.6,
                            }}
                        >

                            Recipient verification only displays
                            the account holder's name and masked
                            account number. Account balances remain
                            private.

                        </p>

                    </div>

                </aside>

            </div>

        </div>
    );
}


export default TransferPage;