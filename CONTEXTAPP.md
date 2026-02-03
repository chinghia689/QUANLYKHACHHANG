# Customer Management System - Module 2: Account Management

## Overview
Module 2 adds bank account management capabilities to the system. It supports Checking and Savings accounts with different business rules.

## Architecture Decisions
- **Account Number Generation**: Auto-incrementing string starting from "1001000001".
- **Status Workflow**: ACTIVE -> FROZEN -> CLOSED.
- **Data Binding**: Uses JavaFX Properties in `Account` model for seamless UI updates.
- **Permissions**:
  - **Open**: All Staff
  - **Freeze**: All Staff (Active -> Frozen)
  - **Unfreeze**: Manager/Admin only (Frozen -> Active)
  - **Close**: Manager/Admin only (Active -> Closed), requires 0 balance.

## Business Rules

### Account Types
1. **CHECKING**
   - No term (0 months)
   - Default interest rate: 0.50%
   - Used for daily transactions (Module 3)

2. **SAVINGS**
   - Requires Term selection (3, 6, 12 months)
   - Interest Rates:
     - 3 Months: 4.00%
     - 6 Months: 5.00%
     - 12 Months: 6.00%

### Constraints
- Customer can have only ONE active account of each type.
- Cannot close account if balance > 0.
- Soft delete implementation for "Close" (status=CLOSED, closed_date set).

## Database Schema
Table `accounts`:
- `id`: PK, Auto-increment
- `customer_id`: FK to customers
- `account_number`: Unique
- `account_type`: Enum (CHECKING, SAVINGS)
- `balance`: Decimal(15,2)
- `interest_rate`: Decimal(5,2)
- `term_months`: Int
- `status`: Enum (ACTIVE, FROZEN, CLOSED)
- `created_date`, `closed_date`: Datetime

## UI Components
- **Account View**: Table with search/filter, status color coding.
- **Account Dialog**: Dynamic form that changes based on account type selection.
