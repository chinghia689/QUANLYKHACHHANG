# Customer Management Application - User Manual

## 1. Getting Started

### Login
- Launch the application
- Enter your username and password
- Click **Login** button
- Default roles:
    - **ADMIN/MANAGER**: Full access, including reports and approval
    - **STAFF**: Managing customers and transactions

---

## 2. Dashboard
The landing page after login showing system overview.
- **Summary Cards**: Total Customers, Total Balance, Total Loans, Today's Transactions
- **Charts**: Visual distribution of customers, accounts, and financial trends
- **Quick Stats**: Month-over-month growth indicators

---

## 3. Customer Management (Module 2)
Navigate to **Customers** menu (👥 icon).

### Add Customer
1. Click **"Add Customer"** button
2. Fill in required details:
    - Full Name
    - Email (must be unique)
    - Phone Number
    - Address
    - Customer Type (INDIVIDUAL/CORPORATE)
3. Click **Save**

### Edit Customer
1. Select a customer from the list
2. Click **"Edit"** button
3. Update details and click **Save**

### Delete Customer
1. Select a customer
2. Click **"Delete"** button
3. Confirm the action

---

## 4. Account Management (Module 2)
Navigate to **Accounts** menu (🏦 icon).

### Create Account
1. Go to **Customers** view
2. Select a customer
3. Click **"View Accounts"** (or similar context action)
4. Click **"Add Account"**
5. Select Account Type (SAVINGS/CHECKING) and Initial Balance
6. Click **Create**

### Account Actions
- **Lock/Unlock**: Change account status
- **View Details**: See balance and transaction history

---

## 5. Transaction Management (Module 3)
Navigate to **Accounts** menu and select an account to perform transactions.

### Deposit
1. Click **"Deposit"** button
2. Enter Amount and Description
3. Confirm
4. **PDF Receipt** is automatically generated

### Withdraw
1. Click **"Withdraw"** button
2. Enter Amount
3. Confirm (Must have sufficient balance)
4. **PDF Receipt** is automatically generated

### Transfer
1. Click **"Transfer"** button
2. Enter Target Account Number
3. Enter Amount
4. Confirm transfer
5. **PDF Receipt** is automatically generated

---

## 6. Loan Management (Module 4)
Navigate to **Loans** menu (💰 icon).

### Apply for Loan
1. Click **"New Loan Application"**
2. Select Customer
3. Enter Principal Amount
4. Enter Term (Months) and Interest Rate
5. Click **Submit Application**
6. Status becomes **PENDING**

### Approve/Reject Loan (Manager Only)
1. Select a **PENDING** loan
2. Click **"Approve"** or **"Reject"**
3. If Approved, status becomes **APPROVED**

### Disburse Loan
1. Select an **APPROVED** loan
2. Click **"Disburse"**
3. Funds are deposited into customer's account
4. Status becomes **DISBURSED**

### Repay Loan
1. Select a **DISBURSED** loan
2. Click **"Make Payment"**
3. Enter Payment Amount
4. Funds deducted from customer's account

---

## 7. Reports & Export (Module 5)
Navigate to **Reports** menu (📈 icon).

### Dashboard Overview
- View real-time charts and top accounts
- **Export PDF**: Click button to save snapshot

### Transaction Report
1. Select Date Range (From/To)
2. Filter by Transaction Type (optional)
3. Click **"Generate Report"**
4. **Export PDF**: Save list of transactions

### Loan Portfolio Report
1. Select Date Range and Loan Status
2. Click **"Generate Report"**
3. View outstanding balance and loan list
4. **Export PDF**: Save loan portfolio details

### Account Statement
1. Select specific Account
2. Select Period
3. Click **"Generate Statement"**
4. View Opening Balance, Total In/Out, Closing Balance
5. **Export PDF**: Official account statement generation

---

## 8. Settings & Profile
- **Theme**: Toggle Dark/Light mode (Top-left button)
- **Change Password**: Click "Change Password" in sidebar
- **Logout**: Click "Logout" to end session
