```markdown
# 🏦 BankCore — Personal Banking Platform

BankCore is a full-stack personal banking platform built to simulate modern digital banking workflows with secure authentication, account management, money transfers, transaction tracking, spending analytics, budgeting, savings goals, financial health insights, and an AI-powered banking assistant.

The application combines a React frontend with a Spring Boot backend, PostgreSQL database, JWT authentication, Spring Security, Spring AI, Google Gemini, and PGVector.

---

## 🌐 Live Demo

**🚀 Frontend**

https://bank-core-personal-banking-platform.vercel.app/

**⚙️ Backend API**

https://bankcore-backend-izco.onrender.com/

**💚 Backend Health Check**

https://bankcore-backend-izco.onrender.com/health

**📂 GitHub Repository**

https://github.com/Aayush30020/BankCore-Personal_Banking_Platform

---

## 📌 Overview

BankCore is designed as a production-style personal banking platform that demonstrates how modern banking systems can be structured using a secure backend and an interactive frontend.

Users can:

- 🔐 Register and securely log in
- 🏦 Create and manage bank accounts
- 💰 View account balances
- 💸 Transfer money between accounts
- 📜 View transaction history
- 📊 Analyze monthly spending
- 🎯 Create and manage budgets
- 🪙 Create and track savings goals
- ❤️ Monitor financial health
- 🧪 Generate demo banking data
- ➕ Add simulated money to accounts
- 🤖 Interact with an AI-powered banking assistant

The project also demonstrates important backend engineering concepts such as transaction management, idempotency, database locking, ledger entries, JWT authentication, customer data isolation, and controlled AI tool calling.

---

# ✨ Features

## 🔐 Authentication & Security

- Secure user registration and login
- JWT-based authentication
- Spring Security
- BCrypt password hashing
- Protected REST APIs
- Customer-specific data access
- Authenticated AI requests
- Unauthorized request protection

---

## 🏦 Account Management

Users can create and manage multiple bank accounts.

Features include:

- Savings accounts
- Current accounts
- Account numbers
- Account balances
- Account status
- Currency information
- Account creation timestamps
- Customer-specific account access

---

## 💸 Money Transfers

BankCore provides secure account-to-account transfers.

The transfer workflow validates:

- Source account
- Destination account
- Account ownership
- Account status
- Available balance
- Transfer amount

Each successful transfer creates the required transaction and ledger records while updating account balances atomically.

---

## 🔁 Idempotent Transactions

BankCore implements idempotency for money transfers.

Each transfer request uses an idempotency key to prevent accidental duplicate transactions caused by:

- Network retries
- Duplicate requests
- Client retries
- Request timeouts

If the same idempotency key is submitted again, the existing transaction can be returned instead of creating a duplicate transaction.

---

## 📒 Ledger System

BankCore maintains a separate ledger for financial transactions.

A successful transfer creates:

- `DEBIT` entry for the sender
- `CREDIT` entry for the receiver

This provides an additional financial record for tracking account activity.

---

## 📊 Spending Analytics

BankCore analyzes completed outgoing transactions and categorizes spending.

Supported categories include:

- 🍔 Food
- 🛍️ Shopping
- 🧾 Bills
- 🚗 Transport
- 🎬 Entertainment
- 🏥 Health
- 📚 Education
- 💸 Transfer
- 📦 Other

The spending section provides:

- Total spending
- Category-wise spending
- Monthly spending
- Recent transactions
- Spending distribution

---

## 💰 Budget Management

Users can create monthly budgets for different spending categories.

BankCore tracks:

- Monthly budget limit
- Current month spending
- Remaining budget
- Budget utilization
- Budget status

Example:

```text
Food           ₹8,000
Shopping       ₹10,000
Bills          ₹6,000
Transport      ₹5,000
Health         ₹3,000
Education      ₹2,000
Entertainment  ₹3,000
```

---

## 🎯 Savings Goals

Users can create and track personal savings goals.

Each goal contains:

- Goal name
- Target amount
- Current saved amount
- Target date
- Progress percentage

BankCore can also calculate:

- Remaining amount
- Required monthly savings
- Required daily savings
- Goal progress
- Goal status
- Remaining time

Example:

```text
Goal: Bike
Target Amount: ₹90,000
Current Savings: ₹0
Remaining Amount: ₹90,000
Required Monthly Saving: ₹30,000
```

---

## ❤️ Financial Health

The Financial Health section provides a consolidated view of the user's finances.

It combines information from:

- Bank accounts
- Spending
- Budgets
- Savings goals
- Financial alerts

This provides a centralized overview of the customer's financial activity.

---

# 🤖 BankCore AI

One of the main features of BankCore is its AI-powered banking assistant.

The assistant is built using:

- Spring AI
- Google Gemini
- Spring AI Tool Calling
- PGVector
- Retrieval-Augmented Generation

The AI assistant can answer general BankCore questions as well as authenticated customer-specific financial questions.

---

## 🧠 AI Tool Calling

The AI does not directly access the database.

Instead, BankCore exposes controlled backend tools that allow the AI to retrieve specific information.

Examples include:

```text
getMyAccounts
getMyTransactions
getMySpending
getMyBudgets
getMySavingsGoals
getMySavingsGoalAnalysis
getFinancialAlerts
```

For example, a user can ask:

```text
How much do I need to save every month for my Bike goal?
```

The AI retrieves the actual savings goal information from the backend and generates a response based on the retrieved data.

---

## 🔗 Multi-Tool AI Queries

BankCore can combine multiple backend tools when a question requires information from different areas of the application.

For example:

```text
How much money do I have across my accounts,
and how much could I allocate toward my Bike goal?
```

The AI can retrieve:

1. Account balances
2. Savings goal information

and combine both results into a single response.

---

## 📚 Retrieval-Augmented Generation

BankCore uses PostgreSQL with PGVector for vector-based knowledge retrieval.

The RAG system is used for BankCore-specific information such as:

- Platform functionality
- Banking workflows
- Application knowledge
- Feature information
- Product-specific documentation

Customer-specific financial information is retrieved through authenticated backend tools rather than relying on RAG.

---

## 🛡️ AI Safety

The AI assistant follows controlled access rules.

### 👤 Customer Data Isolation

Customer-specific financial information is retrieved only for the authenticated customer.

### 🎯 Backend as Source of Truth

The backend remains the source of truth for:

- Account balances
- Transactions
- Spending
- Budgets
- Savings goals
- Financial alerts

### 🔒 Controlled Tool Access

The AI can only use explicitly exposed backend tools.

### 🚫 No Unrestricted Money Movement

The AI does not have unrestricted access to transfer money.

Financial operations remain controlled by dedicated banking APIs.

---

# 🏗️ Architecture

```text
                         ┌──────────────────┐
                         │      User        │
                         └────────┬─────────┘
                                  │
                                  ▼
                         ┌──────────────────┐
                         │ React + Vite     │
                         │    Frontend      │
                         └────────┬─────────┘
                                  │
                               REST API
                                  │
                                  ▼
                      ┌────────────────────────┐
                      │     Spring Boot        │
                      │      Backend           │
                      └───────────┬────────────┘
                                  │
               ┌──────────────────┼──────────────────┐
               │                  │                  │
               ▼                  ▼                  ▼
        ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
        │ PostgreSQL  │    │   Spring AI │    │  Security   │
        │    Neon     │    │             │    │ JWT / BCrypt│
        └─────────────┘    └──────┬──────┘    └─────────────┘
                                  │
                          ┌───────┴────────┐
                          │                │
                          ▼                ▼
                   ┌────────────┐   ┌────────────┐
                   │   Gemini   │   │  PGVector  │
                   │     AI     │   │    RAG     │
                   └────────────┘   └────────────┘
```

---

# 🛠️ Technology Stack

## 🎨 Frontend

| Technology | Purpose |
|---|---|
| React | Frontend framework |
| Vite | Frontend build tool |
| Axios | API communication |
| CSS | UI styling |

## ⚙️ Backend

| Technology | Purpose |
|---|---|
| Java | Backend programming language |
| Spring Boot | Backend framework |
| Spring Security | Authentication and authorization |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| JWT | Authentication |
| BCrypt | Password hashing |
| Maven | Dependency management |

## 🗄️ Database

| Technology | Purpose |
|---|---|
| PostgreSQL | Primary relational database |
| Neon | Production PostgreSQL hosting |
| PGVector | Vector storage and similarity search |

## 🤖 AI

| Technology | Purpose |
|---|---|
| Spring AI | AI integration framework |
| Google Gemini | Large language model |
| PGVector | RAG vector storage |
| Tool Calling | Customer-specific banking data retrieval |

## ☁️ Deployment

| Platform | Purpose |
|---|---|
| Vercel | React frontend |
| Render | Spring Boot backend |
| Neon | PostgreSQL database |
| GitHub | Source code and version control |

---

# 📂 Project Structure

```text
BankCore-Personal_Banking_Platform/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── bankcore/
│       │           ├── config/
│       │           ├── controller/
│       │           ├── dto/
│       │           ├── entity/
│       │           ├── repository/
│       │           ├── security/
│       │           ├── service/
│       │           └── ...
│       │
│       └── resources/
│           └── application.properties
│
├── frontend/
│   ├── src/
│   │   ├── pages/
│   │   ├── api.js
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── auth.css
│   │   └── main.jsx
│   │
│   ├── package.json
│   └── vite.config.js
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── .gitignore
└── README.md
```

---

# 🚀 REST API

## 🔐 Authentication

```http
POST /api/auth/register
POST /api/auth/login
```

## 🏦 Accounts

```http
GET  /api/accounts
GET  /api/accounts/{accountId}
POST /api/accounts
GET  /api/accounts/lookup?accountNumber={accountNumber}
```

## 💸 Transfers

```http
POST /api/transfers
```

## 📜 Transactions

```http
GET /api/transactions
```

## 📊 Spending

```http
GET /api/spending
```

## 💰 Budgets

```http
GET    /api/budgets
PUT    /api/budgets
DELETE /api/budgets/{category}
```

## 🎯 Savings Goals

```http
GET    /api/savings-goals
PUT    /api/savings-goals
DELETE /api/savings-goals/{name}
```

## 🤖 AI Assistant

```http
POST /api/ai/chat
```

Example:

```json
{
  "message": "How much do I need to save every month for my Bike goal?"
}
```

## 💚 Health

```http
GET /health
```

Response:

```json
{
  "status": "UP"
}
```

---

# 💻 Local Setup

## 📋 Prerequisites

Install the following:

- Java 21+
- Node.js
- npm
- PostgreSQL
- Git

Recommended:

- IntelliJ IDEA
- VS Code
- Docker Desktop

---

## 📥 Clone Repository

```bash
git clone https://github.com/Aayush30020/BankCore-Personal_Banking_Platform.git
```

```bash
cd BankCore-Personal_Banking_Platform
```

---

# ⚙️ Backend Setup

Configure the required environment variables:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/bankcore
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

JWT_SECRET=your_jwt_secret
GEMINI_API_KEY=your_gemini_api_key
```

Run the Spring Boot backend:

```bash
./mvnw spring-boot:run
```

For Windows:

```bash
mvnw.cmd spring-boot:run
```

Backend:

```text
http://localhost:8080
```

---

# 🎨 Frontend Setup

Navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Create a `.env` file:

```env
VITE_API_URL=http://localhost:8080/api
```

Run the frontend:

```bash
npm run dev
```

Frontend:

```text
http://localhost:5173
```

---

# 🔑 Environment Variables

## Backend

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
GEMINI_API_KEY
```

## Frontend

```text
VITE_API_URL
```

Never commit real passwords, API keys, JWT secrets, database credentials, or authentication tokens to GitHub.

---

# 🌍 Production Deployment

BankCore is deployed using the following architecture:

```text
Frontend
   ↓
Vercel
   ↓
Spring Boot REST API
   ↓
Render
   ↓
PostgreSQL
   ↓
Neon
```

### 🚀 Frontend

https://bank-core-personal-banking-platform.vercel.app/

### ⚙️ Backend

https://bankcore-backend-izco.onrender.com/

### 🗄️ Database

Neon PostgreSQL

### 💚 Health Monitoring

https://bankcore-backend-izco.onrender.com/health

The backend health endpoint is publicly accessible and can be monitored by uptime monitoring services.

---

# 🧪 Demo Data

BankCore includes a demo data generation workflow.

It can create sample:

- Bank accounts
- Account balances
- Transfers
- Transactions
- Spending categories
- Budgets
- Savings goals

This makes it possible to demonstrate the complete banking platform without manually creating every transaction.

---

# 🔄 Demo Workflow

A typical demonstration can follow this flow:

```text
Register
   ↓
Login
   ↓
Generate Demo Data
   ↓
View Accounts
   ↓
View Transactions
   ↓
Analyze Spending
   ↓
Review Budgets
   ↓
Create Savings Goals
   ↓
Check Financial Health
   ↓
Ask BankCore AI
```

Example questions for the AI assistant:

```text
How much money do I have across my accounts?

How much did I spend this month?

What category did I spend the most on?

How am I doing with my Bike goal?

How much do I need to save every month for my Bike goal?

What are my current financial alerts?

What is my remaining budget for food?
```

---

# 🧩 Engineering Highlights

### 🔐 Secure Authentication

JWT-based authentication using Spring Security.

### 👤 Customer Data Isolation

Banking information is retrieved using the authenticated customer's identity.

### 🔄 Transaction Management

Financial operations are processed using database transactions.

### 🔒 Concurrency Control

Database locking is used where required to prevent inconsistent account updates.

### 🔁 Idempotency

Transfer requests use idempotency keys to prevent duplicate transactions.

### 📒 Ledger

Transfers create corresponding debit and credit ledger entries.

### 🌐 REST Architecture

Banking operations are exposed through structured REST APIs.

### 🤖 AI Tool Calling

Spring AI connects Google Gemini with controlled backend banking tools.

### 📚 RAG

PGVector provides vector-based retrieval for BankCore-specific knowledge.

### ☁️ Production Deployment

The application is deployed using Vercel, Render, and Neon PostgreSQL.

---

# 📡 Monitoring

The backend exposes a public health endpoint:

```http
GET /health
```

Production health check:

https://bankcore-backend-izco.onrender.com/health

This endpoint can be used by uptime monitoring services to verify backend availability.

---

# 🧭 Application Modules

The BankCore frontend contains dedicated sections for:

| Module | Purpose |
|---|---|
| 🏠 Dashboard | Overall financial overview |
| 🏦 Accounts | Manage bank accounts |
| 💸 Transfer | Send money between accounts |
| 📜 Transactions | Review banking activity |
| 📊 Spending | Analyze spending |
| 💰 Budgets | Manage monthly budgets |
| 🎯 Savings Goals | Track financial goals |
| ❤️ Financial Health | View overall financial health |
| 🤖 BankCore AI | AI-powered banking assistant |

---

# 🔮 Future Enhancements

Possible future improvements include:

- Scheduled payments
- Recurring transactions
- Advanced financial analytics
- Investment tracking
- Expense forecasting
- AI-powered financial insights
- Improved notification system
- Advanced fraud detection
- Transaction search and filtering
- Exportable financial reports
- Mobile application
- More comprehensive automated testing
- Advanced audit logging
- Role-based banking administration

---

# ⚠️ Disclaimer

BankCore is a software engineering and portfolio project designed to simulate personal banking functionality.

It is not connected to a real banking institution and should not be used to process real financial transactions.

All demonstration balances and transactions are simulated.

---

# 👨‍💻 Author

## Aayush Verma

B.Tech Computer Science & Engineering

### 🔗 GitHub

https://github.com/Aayush30020

### 💼 LinkedIn

https://www.linkedin.com/in/aayush-verma-97995a288/

### 📂 Project Repository

https://github.com/Aayush30020/BankCore-Personal_Banking_Platform

### 🚀 Live Application

https://bank-core-personal-banking-platform.vercel.app/

---

<p align="center">
  Built with Java, Spring Boot, React, PostgreSQL, Spring AI, and Google Gemini.
</p>
```