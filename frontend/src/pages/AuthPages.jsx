import "../auth.css";

import { useState } from "react";
import {
    ArrowRight,
    Eye,
    EyeOff,
    LockKeyhole,
    Mail,
    User,
    ShieldCheck,
    Sparkles,
} from "lucide-react";

import {
    Link,
    useLocation,
    useNavigate,
} from "react-router-dom";

import api from "../api.js";


// ============================================================
// AUTH RESPONSE HELPERS
// ============================================================

function extractToken(data) {

    return (
        data?.token ||
        data?.jwt ||
        data?.accessToken ||
        data?.access_token ||
        null
    );
}


function extractUser(data, fallback = {}) {

    const user =
        data?.user ||
        data?.customer ||
        data?.profile ||
        {};

    return {

        id:
            user.id ??
            data?.userId ??
            data?.customerId ??
            null,

        name:
            user.name ??
            data?.name ??
            fallback.name ??
            "",

        email:
            user.email ??
            data?.email ??
            fallback.email ??
            "",

    };
}


// ============================================================
// STORE AUTHENTICATION
// ============================================================

function storeAuthentication(data, fallback = {}) {

    const token =
        extractToken(data);

    if (!token) {

        throw new Error(
            "Authentication succeeded but no JWT token was returned by the server."
        );
    }


    const user =
        extractUser(
            data,
            fallback
        );


    localStorage.setItem(
        "bankcore_token",
        token
    );

    localStorage.setItem(
        "bankcore_user",
        JSON.stringify(user)
    );


    return {
        token,
        user,
    };
}


// ============================================================
// AUTH LAYOUT
// ============================================================

function AuthLayout({
                        children,
                        title,
                        subtitle,
                        mode,
                    }) {

    return (
        <div className="auth-page">

            {/* ==================================================
                LEFT BRAND PANEL
            ================================================== */}

            <section className="auth-brand-panel">

                <div className="auth-brand-content">

                    <div className="auth-brand">

                        <div className="auth-brand-mark">
                            B
                        </div>

                        <div>

                            <div className="auth-brand-name">
                                BankCore
                            </div>

                            <div className="auth-brand-subtitle">
                                Personal Banking
                            </div>

                        </div>

                    </div>


                    <div className="auth-hero">

                        <div className="auth-eyebrow">
                            SMARTER BANKING
                        </div>

                        <h1>
                            Your money.
                            <br />
                            <span>
                                Your control.
                            </span>
                        </h1>

                        <p>
                            A secure banking workspace for
                            understanding your money, managing
                            transactions, and making better
                            financial decisions.
                        </p>

                    </div>


                    <div className="auth-features">

                        <div className="auth-feature">

                            <div className="auth-feature-icon">
                                <ShieldCheck size={17} />
                            </div>

                            <div>

                                <strong>
                                    Secure by design
                                </strong>

                                <span>
                                    JWT authentication and
                                    protected banking APIs
                                </span>

                            </div>

                        </div>


                        <div className="auth-feature">

                            <div className="auth-feature-icon ai-feature-icon">
                                <Sparkles size={17} />
                            </div>

                            <div>

                                <strong>
                                    Intelligent insights
                                </strong>

                                <span>
                                    Understand spending,
                                    budgets and financial activity
                                </span>

                            </div>

                        </div>

                    </div>

                </div>


                <div className="auth-brand-footer">
                    BankCore · Secure Personal Banking
                </div>

            </section>


            {/* ==================================================
                FORM PANEL
            ================================================== */}

            <section className="auth-form-panel">

                <div className="auth-form-container">

                    <div className="auth-mobile-brand">

                        <div className="auth-brand-mark">
                            B
                        </div>

                        <span>
                            BankCore
                        </span>

                    </div>


                    <div className="auth-form-header">

                        <div className="auth-form-eyebrow">
                            {mode === "login"
                                ? "WELCOME BACK"
                                : "GET STARTED"}
                        </div>

                        <h2>
                            {title}
                        </h2>

                        <p>
                            {subtitle}
                        </p>

                    </div>


                    {children}


                    <div className="auth-security-note">

                        <ShieldCheck size={14} />

                        <span>
                            Your credentials are securely
                            transmitted to BankCore.
                        </span>

                    </div>

                </div>

            </section>

        </div>
    );
}


// ============================================================
// LOGIN
// ============================================================

export function Login() {

    const navigate =
        useNavigate();

    const location =
        useLocation();


    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [showPassword, setShowPassword] =
        useState(false);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");

    const [success, setSuccess] =
        useState(
            location.state?.message || ""
        );


    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");

        setSuccess("");

        setLoading(true);


        try {

            const response =
                await api.post(
                    "/auth/login",
                    {
                        email: email.trim(),
                        password,
                    }
                );


            storeAuthentication(
                response.data,
                {
                    email: email.trim(),
                }
            );


            navigate("/", {
                replace: true,
            });


        } catch (err) {

            console.error(
                "BankCore login error:",
                err
            );


            const backendMessage =
                err.response?.data?.message ||
                err.response?.data?.error ||
                err.response?.data?.detail;


            setError(
                backendMessage ||
                "Unable to sign in. Please check your email and password."
            );

        } finally {

            setLoading(false);

        }

    };


    return (
        <AuthLayout
            mode="login"
            title="Welcome back"
            subtitle="Sign in to access your BankCore dashboard."
        >

            {success && (
                <div className="auth-success">
                    {success}
                </div>
            )}


            {error && (
                <div className="auth-error">
                    {error}
                </div>
            )}


            <form
                className="auth-form"
                onSubmit={handleSubmit}
            >

                {/* EMAIL */}

                <div className="auth-field">

                    <label htmlFor="login-email">
                        Email address
                    </label>

                    <div className="auth-input-wrapper">

                        <Mail
                            size={17}
                            className="auth-input-icon"
                        />

                        <input
                            id="login-email"
                            type="email"
                            value={email}
                            onChange={(event) =>
                                setEmail(
                                    event.target.value
                                )
                            }
                            placeholder="you@example.com"
                            autoComplete="email"
                            required
                        />

                    </div>

                </div>


                {/* PASSWORD */}

                <div className="auth-field">

                    <label htmlFor="login-password">
                        Password
                    </label>

                    <div className="auth-input-wrapper">

                        <LockKeyhole
                            size={17}
                            className="auth-input-icon"
                        />

                        <input
                            id="login-password"
                            type={
                                showPassword
                                    ? "text"
                                    : "password"
                            }
                            value={password}
                            onChange={(event) =>
                                setPassword(
                                    event.target.value
                                )
                            }
                            placeholder="Enter your password"
                            autoComplete="current-password"
                            required
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() =>
                                setShowPassword(
                                    (value) => !value
                                )
                            }
                        >

                            {showPassword ? (
                                <EyeOff size={17} />
                            ) : (
                                <Eye size={17} />
                            )}

                        </button>

                    </div>

                </div>


                {/* SUBMIT */}

                <button
                    type="submit"
                    className="auth-submit"
                    disabled={loading}
                >

                    {loading ? (
                        <>
                            <span className="auth-spinner" />
                            Signing in...
                        </>
                    ) : (
                        <>
                            Sign in
                            <ArrowRight size={17} />
                        </>
                    )}

                </button>

            </form>


            <div className="auth-switch">

                <span>
                    Don't have a BankCore account?
                </span>

                <Link to="/register">
                    Create account
                </Link>

            </div>

        </AuthLayout>
    );
}


// ============================================================
// REGISTER
// ============================================================

export function Register() {

    const navigate =
        useNavigate();


    const [name, setName] =
        useState("");

    const [email, setEmail] =
        useState("");

    const [password, setPassword] =
        useState("");

    const [confirmPassword, setConfirmPassword] =
        useState("");

    const [showPassword, setShowPassword] =
        useState(false);

    const [loading, setLoading] =
        useState(false);

    const [error, setError] =
        useState("");


    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");


        if (
            password !==
            confirmPassword
        ) {

            setError(
                "Passwords do not match."
            );

            return;
        }


        if (password.length < 6) {

            setError(
                "Password must contain at least 6 characters."
            );

            return;
        }


        setLoading(true);


        try {

            const response =
                await api.post(
                    "/auth/register",
                    {
                        name: name.trim(),
                        email: email.trim(),
                        password,
                    }
                );


            /*
             * If registration returns a JWT,
             * log the customer in immediately.
             *
             * Otherwise redirect to login.
             */

            const token =
                extractToken(
                    response.data
                );


            if (token) {

                storeAuthentication(
                    response.data,
                    {
                        name: name.trim(),
                        email: email.trim(),
                    }
                );


                navigate("/", {
                    replace: true,
                });

            } else {

                navigate("/login", {
                    replace: true,
                    state: {
                        message:
                            "Account created successfully. Please sign in.",
                    },
                });

            }


        } catch (err) {

            console.error(
                "BankCore registration error:",
                err
            );


            const backendMessage =
                err.response?.data?.message ||
                err.response?.data?.error ||
                err.response?.data?.detail;


            setError(
                backendMessage ||
                "Unable to create your account. Please try again."
            );

        } finally {

            setLoading(false);

        }

    };


    return (
        <AuthLayout
            mode="register"
            title="Create your account"
            subtitle="Set up your secure BankCore personal banking account."
        >

            {error && (
                <div className="auth-error">
                    {error}
                </div>
            )}


            <form
                className="auth-form"
                onSubmit={handleSubmit}
            >

                {/* NAME */}

                <div className="auth-field">

                    <label htmlFor="register-name">
                        Full name
                    </label>

                    <div className="auth-input-wrapper">

                        <User
                            size={17}
                            className="auth-input-icon"
                        />

                        <input
                            id="register-name"
                            type="text"
                            value={name}
                            onChange={(event) =>
                                setName(
                                    event.target.value
                                )
                            }
                            placeholder="Your full name"
                            autoComplete="name"
                            required
                        />

                    </div>

                </div>


                {/* EMAIL */}

                <div className="auth-field">

                    <label htmlFor="register-email">
                        Email address
                    </label>

                    <div className="auth-input-wrapper">

                        <Mail
                            size={17}
                            className="auth-input-icon"
                        />

                        <input
                            id="register-email"
                            type="email"
                            value={email}
                            onChange={(event) =>
                                setEmail(
                                    event.target.value
                                )
                            }
                            placeholder="you@example.com"
                            autoComplete="email"
                            required
                        />

                    </div>

                </div>


                {/* PASSWORD */}

                <div className="auth-field">

                    <label htmlFor="register-password">
                        Password
                    </label>

                    <div className="auth-input-wrapper">

                        <LockKeyhole
                            size={17}
                            className="auth-input-icon"
                        />

                        <input
                            id="register-password"
                            type={
                                showPassword
                                    ? "text"
                                    : "password"
                            }
                            value={password}
                            onChange={(event) =>
                                setPassword(
                                    event.target.value
                                )
                            }
                            placeholder="Create a password"
                            autoComplete="new-password"
                            required
                        />

                        <button
                            type="button"
                            className="password-toggle"
                            onClick={() =>
                                setShowPassword(
                                    (value) => !value
                                )
                            }
                        >

                            {showPassword ? (
                                <EyeOff size={17} />
                            ) : (
                                <Eye size={17} />
                            )}

                        </button>

                    </div>

                </div>


                {/* CONFIRM PASSWORD */}

                <div className="auth-field">

                    <label htmlFor="register-confirm-password">
                        Confirm password
                    </label>

                    <div className="auth-input-wrapper">

                        <LockKeyhole
                            size={17}
                            className="auth-input-icon"
                        />

                        <input
                            id="register-confirm-password"
                            type={
                                showPassword
                                    ? "text"
                                    : "password"
                            }
                            value={confirmPassword}
                            onChange={(event) =>
                                setConfirmPassword(
                                    event.target.value
                                )
                            }
                            placeholder="Confirm your password"
                            autoComplete="new-password"
                            required
                        />

                    </div>

                </div>


                {/* SUBMIT */}

                <button
                    type="submit"
                    className="auth-submit"
                    disabled={loading}
                >

                    {loading ? (
                        <>
                            <span className="auth-spinner" />
                            Creating account...
                        </>
                    ) : (
                        <>
                            Create account
                            <ArrowRight size={17} />
                        </>
                    )}

                </button>

            </form>


            <div className="auth-switch">

                <span>
                    Already have an account?
                </span>

                <Link to="/login">
                    Sign in
                </Link>

            </div>

        </AuthLayout>
    );
}