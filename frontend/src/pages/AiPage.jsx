import { useEffect, useRef, useState } from "react";
import {
    Bot,
    BrainCircuit,
    CheckCircle2,
    ChevronRight,
    Clock3,
    Copy,
    Loader2,
    MessageCircle,
    RefreshCw,
    Send,
    Sparkles,
    Trash2,
    User,
    Wallet,
} from "lucide-react";

import api from "../api";
import "./AiPage.css";


// ============================================================
// QUICK PROMPTS
// ============================================================

const QUICK_PROMPTS = [
    {
        title: "Financial health",
        message: "Give me a summary of my financial health.",
        icon: Wallet,
    },
    {
        title: "Recent transactions",
        message: "Show me my recent transactions.",
        icon: Clock3,
    },
    {
        title: "Spending analysis",
        message: "Analyze my overall spending.",
        icon: BrainCircuit,
    },
    {
        title: "Budget status",
        message: "How are my budgets doing?",
        icon: CheckCircle2,
    },
];


// ============================================================
// HELPERS
// ============================================================

const createMessage = (
    role,
    content
) => ({
    id:
        Date.now() +
        Math.random(),

    role,

    content,

    timestamp:
        new Date(),
});


// ============================================================
// INLINE MARKDOWN FORMATTER
// ============================================================

const formatInlineMarkdown = (
    text
) => {

    if (!text) {
        return null;
    }

    const normalizedText =
        text
            .replace(/\bON_TRACK\b/g, "On Track")
            .replace(/\bNEAR_LIMIT\b/g, "Near Limit")
            .replace(/\bAT_LIMIT\b/g, "At Limit")
            .replace(/\bEXCEEDED\b/g, "Exceeded");

    text = normalizedText;

    const parts = [];

    /*
     * Supports:
     *
     * **bold**
     * *italic*
     * `code`
     *
     * The parser intentionally keeps this lightweight
     * because BankCore AI only needs basic Markdown
     * formatting for financial responses.
     */

    const pattern =
        /(\*\*[^*]+\*\*|\*[^*]+\*|`[^`]+`)/g;

    let lastIndex = 0;

    let match;

    while (
        (match = pattern.exec(text)) !== null
        ) {

        // ----------------------------------------------------
        // NORMAL TEXT BEFORE MATCH
        // ----------------------------------------------------

        if (
            match.index > lastIndex
        ) {

            parts.push(
                text.substring(
                    lastIndex,
                    match.index
                )
            );
        }


        const token =
            match[0];


        // ----------------------------------------------------
        // BOLD
        // ----------------------------------------------------

        if (
            token.startsWith("**") &&
            token.endsWith("**")
        ) {

            parts.push(
                <strong
                    key={
                        `bold-${match.index}`
                    }
                >
                    {token.substring(
                        2,
                        token.length - 2
                    )}
                </strong>
            );

        }

            // ----------------------------------------------------
            // ITALIC
        // ----------------------------------------------------

        else if (
            token.startsWith("*") &&
            token.endsWith("*")
        ) {

            parts.push(
                <em
                    key={
                        `italic-${match.index}`
                    }
                >
                    {token.substring(
                        1,
                        token.length - 1
                    )}
                </em>
            );

        }

            // ----------------------------------------------------
            // INLINE CODE
        // ----------------------------------------------------

        else if (
            token.startsWith("`") &&
            token.endsWith("`")
        ) {

            parts.push(
                <code
                    key={
                        `code-${match.index}`
                    }
                >
                    {token.substring(
                        1,
                        token.length - 1
                    )}
                </code>
            );
        }


        lastIndex =
            match.index +
            token.length;
    }


    // --------------------------------------------------------
    // REMAINING NORMAL TEXT
    // --------------------------------------------------------

    if (
        lastIndex < text.length
    ) {

        parts.push(
            text.substring(
                lastIndex
            )
        );
    }


    return parts;
};


// ============================================================
// AI PAGE
// ============================================================

export default function AiPage() {

    const [messages, setMessages] =
        useState([
            createMessage(
                "assistant",
                "Hi Aayush. I'm BankCore AI. I can help you understand your accounts, transactions, spending, budgets, savings goals, and overall financial activity."
            ),
        ]);

    const [input, setInput] =
        useState("");

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    const [copiedId, setCopiedId] =
        useState(null);

    const messagesEndRef =
        useRef(null);

    const textareaRef =
        useRef(null);


    // ========================================================
    // AUTO SCROLL
    // ========================================================

    useEffect(() => {

        messagesEndRef.current?.scrollIntoView({
            behavior: "smooth",
        });

    }, [messages, loading]);


    // ========================================================
    // FOCUS INPUT
    // ========================================================

    useEffect(() => {

        textareaRef.current?.focus();

    }, []);


    // ========================================================
    // SEND MESSAGE
    // ========================================================

    const sendMessage = async (
        messageOverride = null
    ) => {

        const message =
            (
                messageOverride !== null
                    ? messageOverride
                    : input
            ).trim();

        if (!message || loading) {
            return;
        }

        setError("");

        setInput("");

        const userMessage =
            createMessage(
                "user",
                message
            );

        setMessages((previous) => [
            ...previous,
            userMessage,
        ]);

        setLoading(true);

        try {

            const response =
                await api.post(
                    "/ai/chat",
                    {
                        message,
                    },
                    {
                        timeout: 60000,
                    }
                );

            const aiResponse =
                response.data?.response;

            if (!aiResponse) {

                throw new Error(
                    "BankCore AI returned an empty response."
                );
            }

            const assistantMessage =
                createMessage(
                    "assistant",
                    aiResponse
                );

            setMessages((previous) => [
                ...previous,
                assistantMessage,
            ]);

        } catch (err) {

            console.error(
                "BankCore AI error:",
                err
            );

            const backendMessage =
                err.response?.data?.message;

            const backendError =
                err.response?.data?.error;

            setError(
                backendMessage ||
                backendError ||
                err.message ||
                "Unable to connect to BankCore AI."
            );

        } finally {

            setLoading(false);

            setTimeout(() => {
                textareaRef.current?.focus();
            }, 50);
        }
    };


    // ========================================================
    // ENTER KEY
    // ========================================================

    const handleKeyDown = (event) => {

        if (
            event.key === "Enter" &&
            !event.shiftKey
        ) {

            event.preventDefault();

            sendMessage();
        }
    };


    // ========================================================
    // CLEAR CHAT
    // ========================================================

    const clearChat = () => {

        if (loading) {
            return;
        }

        setMessages([
            createMessage(
                "assistant",
                "Chat cleared. What would you like to know about your BankCore account?"
            ),
        ]);

        setError("");

        setTimeout(() => {
            textareaRef.current?.focus();
        }, 50);
    };


    // ========================================================
    // COPY MESSAGE
    // ========================================================

    const copyMessage = async (
        messageId,
        content
    ) => {

        try {

            await navigator.clipboard.writeText(
                content
            );

            setCopiedId(messageId);

            setTimeout(() => {
                setCopiedId(null);
            }, 1500);

        } catch (err) {

            console.error(
                "Failed to copy message:",
                err
            );
        }
    };


    // ========================================================
    // FORMAT AI MESSAGE
    // ========================================================

    const formatMessage = (
        content
    ) => {

        if (!content) {
            return null;
        }

        const lines =
            content.split("\n");

        return lines.map(
            (line, index) => {

                const trimmed =
                    line.trim();


                // =================================================
                // EMPTY LINE
                // =================================================

                if (
                    trimmed === ""
                ) {

                    return (
                        <div
                            key={index}
                            className="ai-message-spacer"
                        />
                    );
                }


                // =================================================
                // H3
                // =================================================

                if (
                    trimmed.startsWith("### ")
                ) {

                    return (
                        <h4
                            key={index}
                            className="ai-message-heading"
                        >
                            {formatInlineMarkdown(
                                trimmed.substring(4)
                            )}
                        </h4>
                    );
                }


                // =================================================
                // H2
                // =================================================

                if (
                    trimmed.startsWith("## ")
                ) {

                    return (
                        <h3
                            key={index}
                            className="ai-message-heading"
                        >
                            {formatInlineMarkdown(
                                trimmed.substring(3)
                            )}
                        </h3>
                    );
                }


                // =================================================
                // BULLET LIST
                //
                // Supports:
                // - item
                // • item
                // * item
                // =================================================

                const isDashBullet =
                    trimmed.startsWith("- ");

                const isDotBullet =
                    trimmed.startsWith("• ");

                const isAsteriskBullet =
                    trimmed.startsWith("* ") &&
                    !trimmed.startsWith("**");


                if (
                    isDashBullet ||
                    isDotBullet ||
                    isAsteriskBullet
                ) {

                    let bulletText =
                        trimmed.substring(2);


                    return (
                        <div
                            key={index}
                            className="ai-message-list-item"
                        >

                            <span className="ai-list-dot">
                                •
                            </span>

                            <span>
                                {formatInlineMarkdown(
                                    bulletText
                                )}
                            </span>

                        </div>
                    );
                }


                // =================================================
                // NUMBERED LIST
                //
                // Supports:
                // 1. Item
                // 2. Item
                // =================================================

                const numberedMatch =
                    trimmed.match(
                        /^(\d+)\.\s+(.*)$/
                    );


                if (
                    numberedMatch
                ) {

                    return (
                        <div
                            key={index}
                            className="ai-message-list-item"
                        >

                            <span className="ai-list-number">
                                {numberedMatch[1]}.
                            </span>

                            <span>
                                {formatInlineMarkdown(
                                    numberedMatch[2]
                                )}
                            </span>

                        </div>
                    );
                }


                // =================================================
                // NORMAL PARAGRAPH
                // =================================================

                return (
                    <p
                        key={index}
                        className="ai-message-paragraph"
                    >
                        {formatInlineMarkdown(
                            trimmed
                        )}
                    </p>
                );
            }
        );
    };


    // ========================================================
    // RENDER
    // ========================================================

    return (
        <div className="ai-page">

            {/* ==================================================
                HEADER
            ================================================== */}

            <div className="ai-page-header">

                <div className="ai-header-main">

                    <div className="ai-header-icon">
                        <Sparkles
                            size={25}
                            strokeWidth={2}
                        />
                    </div>

                    <div>

                        <div className="ai-eyebrow">
                            INTELLIGENCE
                        </div>

                        <h1>
                            BankCore AI
                        </h1>

                        <p>
                            Your intelligent financial
                            assistant for your BankCore account.
                        </p>

                    </div>

                </div>


                <button
                    className="ai-clear-button"
                    onClick={clearChat}
                    disabled={loading}
                >

                    <Trash2 size={16} />

                    Clear chat

                </button>

            </div>


            {/* ==================================================
                AI STATUS
            ================================================== */}

            <div className="ai-status-bar">

                <div className="ai-status-left">

                    <div className="ai-status-indicator">
                        <span />
                    </div>

                    <div>

                        <strong>
                            BankCore AI is ready
                        </strong>

                        <span>
                            Connected to your authenticated
                            banking data
                        </span>

                    </div>

                </div>


                <div className="ai-status-secure">

                    <CheckCircle2 size={16} />

                    Secure session

                </div>

            </div>


            {/* ==================================================
                MAIN CHAT AREA
            ================================================== */}

            <div className="ai-chat-card">

                {/* ==============================================
                    CHAT HEADER
                ============================================== */}

                <div className="ai-chat-header">

                    <div className="ai-chat-title">

                        <div className="ai-chat-avatar">
                            <Bot size={20} />
                        </div>

                        <div>

                            <strong>
                                BankCore Assistant
                            </strong>

                            <span>
                                Ask about your finances
                            </span>

                        </div>

                    </div>


                    <div className="ai-powered-badge">

                        <Sparkles size={13} />

                        AI powered

                    </div>

                </div>


                {/* ==============================================
                    MESSAGES
                ============================================== */}

                <div className="ai-messages">

                    {messages.map(
                        (message) => {

                            const isUser =
                                message.role ===
                                "user";

                            return (
                                <div
                                    key={message.id}
                                    className={`ai-message-row ${
                                        isUser
                                            ? "user"
                                            : "assistant"
                                    }`}
                                >

                                    <div
                                        className={`ai-message-avatar ${
                                            isUser
                                                ? "user"
                                                : "assistant"
                                        }`}
                                    >

                                        {isUser ? (
                                            <User
                                                size={17}
                                            />
                                        ) : (
                                            <Bot
                                                size={17}
                                            />
                                        )}

                                    </div>


                                    <div className="ai-message-wrapper">

                                        <div className="ai-message-name">

                                            {isUser
                                                ? "You"
                                                : "BankCore AI"}

                                        </div>


                                        <div
                                            className={`ai-message-bubble ${
                                                isUser
                                                    ? "user"
                                                    : "assistant"
                                            }`}
                                        >

                                            {isUser ? (

                                                <p className="ai-message-paragraph">
                                                    {message.content}
                                                </p>

                                            ) : (

                                                formatMessage(
                                                    message.content
                                                )

                                            )}

                                        </div>


                                        <div className="ai-message-actions">

                                            <span>

                                                {message.timestamp.toLocaleTimeString(
                                                    "en-IN",
                                                    {
                                                        hour: "2-digit",
                                                        minute: "2-digit",
                                                    }
                                                )}

                                            </span>


                                            {!isUser && (

                                                <button
                                                    onClick={() =>
                                                        copyMessage(
                                                            message.id,
                                                            message.content
                                                        )
                                                    }
                                                    title="Copy response"
                                                >

                                                    {copiedId ===
                                                    message.id ? (

                                                        <>
                                                            <CheckCircle2
                                                                size={13}
                                                            />

                                                            Copied
                                                        </>

                                                    ) : (

                                                        <>
                                                            <Copy
                                                                size={13}
                                                            />

                                                            Copy
                                                        </>

                                                    )}

                                                </button>

                                            )}

                                        </div>

                                    </div>

                                </div>
                            );
                        }
                    )}


                    {/* ==========================================
                        LOADING
                    ========================================== */}

                    {loading && (

                        <div className="ai-message-row assistant">

                            <div className="ai-message-avatar assistant">
                                <Bot size={17} />
                            </div>

                            <div className="ai-message-wrapper">

                                <div className="ai-message-name">
                                    BankCore AI
                                </div>

                                <div className="ai-message-bubble assistant ai-thinking">

                                    <div className="thinking-icon">

                                        <Loader2
                                            size={17}
                                            className="spin"
                                        />

                                    </div>

                                    <span>
                                        Analyzing your
                                        financial data...
                                    </span>

                                </div>

                            </div>

                        </div>
                    )}


                    <div
                        ref={messagesEndRef}
                    />

                </div>


                {/* ==============================================
                    ERROR
                ============================================== */}

                {error && (

                    <div className="ai-error">

                        <div className="ai-error-icon">
                            <RefreshCw size={16} />
                        </div>

                        <div>

                            <strong>
                                BankCore AI couldn't
                                complete that request.
                            </strong>

                            <span>
                                {error}
                            </span>

                        </div>

                    </div>
                )}


                {/* ==============================================
                    QUICK PROMPTS
                ============================================== */}

                {messages.length <= 1 && !loading && (

                    <div className="ai-quick-section">

                        <div className="ai-quick-header">

                            <div>

                                <strong>
                                    Try asking
                                </strong>

                                <span>
                                    Quick questions about
                                    your finances
                                </span>

                            </div>

                            <MessageCircle
                                size={17}
                            />

                        </div>


                        <div className="ai-quick-grid">

                            {QUICK_PROMPTS.map(
                                (prompt) => {

                                    const Icon =
                                        prompt.icon;

                                    return (
                                        <button
                                            key={
                                                prompt.title
                                            }
                                            className="ai-quick-card"
                                            onClick={() =>
                                                sendMessage(
                                                    prompt.message
                                                )
                                            }
                                        >

                                            <div className="ai-quick-icon">

                                                <Icon
                                                    size={18}
                                                />

                                            </div>

                                            <div>

                                                <strong>
                                                    {
                                                        prompt.title
                                                    }
                                                </strong>

                                                <span>
                                                    {
                                                        prompt.message
                                                    }
                                                </span>

                                            </div>

                                            <ChevronRight
                                                size={16}
                                                className="ai-quick-arrow"
                                            />

                                        </button>
                                    );
                                }
                            )}

                        </div>

                    </div>
                )}


                {/* ==============================================
                    INPUT
                ============================================== */}

                <div className="ai-input-area">

                    <div className="ai-input-wrapper">

                        <textarea
                            ref={textareaRef}
                            value={input}
                            onChange={(event) =>
                                setInput(
                                    event.target.value
                                )
                            }
                            onKeyDown={
                                handleKeyDown
                            }
                            placeholder="Ask BankCore AI about your finances..."
                            maxLength={2000}
                            rows={1}
                            disabled={loading}
                        />

                        <div className="ai-input-footer">

                            <span>
                                {input.length}/2000
                            </span>

                            <span className="ai-input-hint">
                                Press Enter to send
                            </span>

                        </div>

                    </div>


                    <button
                        className="ai-send-button"
                        onClick={() =>
                            sendMessage()
                        }
                        disabled={
                            loading ||
                            !input.trim()
                        }
                        title="Send message"
                    >

                        {loading ? (

                            <Loader2
                                size={20}
                                className="spin"
                            />

                        ) : (

                            <Send
                                size={20}
                            />

                        )}

                    </button>

                </div>


                {/* ==============================================
                    DISCLAIMER
                ============================================== */}

                <div className="ai-disclaimer">

                    <Sparkles size={13} />

                    <span>
                        BankCore AI provides information
                        and analysis based on your banking
                        data. It does not provide regulated
                        financial advice.
                    </span>

                </div>

            </div>

        </div>
    );
}