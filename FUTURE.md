# FUTURE - Banking Customer Management System

> Tài liệu chi tiết các tính năng cần phát triển cho hệ thống quản lý khách hàng ngân hàng.

## Tổng quan

| Hạng mục | Chi tiết |
|----------|----------|
| **Loại ngân hàng** | Retail + Corporate (cá nhân & doanh nghiệp) |
| **Tài khoản** | Thanh toán, Tiết kiệm, Tài khoản vay |
| **Giao dịch** | Nạp/rút tiền, chuyển khoản nội bộ |
| **Khoản vay** | Tạo & theo dõi cơ bản |
| **Bảo mật** | Login + phân quyền (Admin, Manager, Staff) |
| **Báo cáo** | Thống kê, giao dịch, khoản vay, export Excel |
| **Mục đích** | Demo/Portfolio |

---

## Module 1: User & Authentication

### 1.1 Model

#### User
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| username | String | Tên đăng nhập (unique) |
| password_hash | String | Mật khẩu đã hash (BCrypt) |
| full_name | String | Họ tên nhân viên |
| email | String | Email |
| role | Role | Vai trò (enum) |
| status | UserStatus | ACTIVE, INACTIVE, LOCKED |
| created_date | LocalDateTime | Ngày tạo |
| last_login | LocalDateTime | Lần đăng nhập cuối |

#### Role (Enum)
| Value | Description | Permissions |
|-------|-------------|-------------|
| ADMIN | Quản trị viên | Full access, quản lý user |
| MANAGER | Quản lý | Duyệt khoản vay, xem báo cáo |
| STAFF | Nhân viên | CRUD customer, tạo giao dịch |

### 1.2 Chức năng
- [ ] Login screen với username/password
- [ ] Session management (lưu user đang đăng nhập)
- [ ] Logout
- [ ] Đổi mật khẩu
- [ ] Quản lý user (ADMIN only): thêm, sửa, khóa tài khoản
- [ ] Phân quyền menu/button theo role

### 1.3 Files cần tạo
```
src/main/java/com/customer/model/User.java
src/main/java/com/customer/model/Role.java
src/main/java/com/customer/model/UserStatus.java
src/main/java/com/customer/dao/UserDAO.java
src/main/java/com/customer/service/AuthService.java
src/main/java/com/customer/service/UserService.java
src/main/java/com/customer/controller/LoginController.java
src/main/java/com/customer/controller/UserController.java
src/main/java/com/customer/util/PasswordUtil.java
src/main/java/com/customer/util/SessionManager.java
src/main/resources/views/login-view.fxml
src/main/resources/views/user-view.fxml
```

---

## Module 2: Account Management (Quản lý tài khoản)

### 2.1 Model

#### Account
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| customer_id | Long | FK -> customers |
| account_number | String | Số tài khoản (unique, 10-12 digits) |
| account_type | AccountType | Loại tài khoản |
| balance | BigDecimal | Số dư hiện tại |
| interest_rate | BigDecimal | Lãi suất (%/năm) - cho SAVINGS |
| status | AccountStatus | ACTIVE, FROZEN, CLOSED |
| created_date | LocalDateTime | Ngày mở |
| closed_date | LocalDateTime | Ngày đóng (nullable) |

#### AccountType (Enum)
| Value | Description |
|-------|-------------|
| CHECKING | Tài khoản thanh toán - giao dịch hàng ngày |
| SAVINGS | Tài khoản tiết kiệm - có lãi suất |
| LOAN | Tài khoản vay - theo dõi khoản vay |

#### AccountStatus (Enum)
| Value | Description |
|-------|-------------|
| ACTIVE | Đang hoạt động |
| FROZEN | Tạm khóa |
| CLOSED | Đã đóng |

### 2.2 Chức năng
- [ ] Mở tài khoản mới cho customer
- [ ] Xem danh sách tài khoản của customer
- [ ] Xem chi tiết tài khoản (số dư, lịch sử)
- [ ] Đóng tài khoản
- [ ] Tạm khóa/mở khóa tài khoản
- [ ] Tự động generate account number

### 2.3 Business Rules
- Mỗi customer có thể có nhiều tài khoản
- Account number format: `1001XXXXXX` (10 digits)
- SAVINGS account: lãi suất mặc định 5%/năm
- Không thể đóng tài khoản có số dư > 0
- Không thể đóng LOAN account khi còn nợ

### 2.4 Files cần tạo
```
src/main/java/com/customer/model/Account.java
src/main/java/com/customer/model/AccountType.java
src/main/java/com/customer/model/AccountStatus.java
src/main/java/com/customer/dao/AccountDAO.java
src/main/java/com/customer/service/AccountService.java
src/main/java/com/customer/controller/AccountController.java
src/main/resources/views/account-view.fxml
src/main/resources/views/account-dialog.fxml
```

---

## Module 3: Transaction Management (Giao dịch)

### 3.1 Model

#### Transaction
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| account_id | Long | FK -> accounts (tài khoản nguồn) |
| transaction_type | TransactionType | Loại giao dịch |
| amount | BigDecimal | Số tiền |
| target_account_id | Long | FK -> accounts (tài khoản đích - cho TRANSFER) |
| balance_after | BigDecimal | Số dư sau giao dịch |
| description | String | Nội dung giao dịch |
| reference_number | String | Mã giao dịch (unique) |
| created_by | Long | FK -> users (nhân viên thực hiện) |
| created_date | LocalDateTime | Thời gian giao dịch |

#### TransactionType (Enum)
| Value | Description |
|-------|-------------|
| DEPOSIT | Nạp tiền |
| WITHDRAW | Rút tiền |
| TRANSFER | Chuyển khoản nội bộ |

### 3.2 Chức năng
- [ ] Nạp tiền vào tài khoản
- [ ] Rút tiền từ tài khoản
- [ ] Chuyển khoản giữa 2 tài khoản nội bộ
- [ ] Xem lịch sử giao dịch theo tài khoản
- [ ] Tìm kiếm giao dịch theo ngày, số tiền, mã GD
- [ ] In phiếu giao dịch

### 3.3 Business Rules
- Không rút/chuyển quá số dư hiện có
- Số tiền giao dịch phải > 0
- Reference number format: `TXN` + timestamp + random (VD: TXN20240115143022001)
- Ghi log người thực hiện giao dịch
- Chuyển khoản: trừ nguồn, cộng đích trong 1 transaction (atomic)

### 3.4 Files cần tạo
```
src/main/java/com/customer/model/Transaction.java
src/main/java/com/customer/model/TransactionType.java
src/main/java/com/customer/dao/TransactionDAO.java
src/main/java/com/customer/service/TransactionService.java
src/main/java/com/customer/controller/TransactionController.java
src/main/resources/views/transaction-view.fxml
src/main/resources/views/deposit-dialog.fxml
src/main/resources/views/withdraw-dialog.fxml
src/main/resources/views/transfer-dialog.fxml
```

---

## Module 4: Loan Management (Quản lý khoản vay)

### 4.1 Model

#### Loan
| Field | Type | Description |
|-------|------|-------------|
| id | Long | Primary key |
| customer_id | Long | FK -> customers |
| loan_account_id | Long | FK -> accounts (LOAN type) |
| loan_number | String | Mã khoản vay (unique) |
| principal_amount | BigDecimal | Số tiền vay gốc |
| interest_rate | BigDecimal | Lãi suất (%/năm) |
| term_months | Integer | Kỳ hạn (tháng) |
| monthly_payment | BigDecimal | Số tiền trả hàng tháng |
| total_paid | BigDecimal | Tổng đã trả |
| remaining_balance | BigDecimal | Số dư còn lại |
| status | LoanStatus | Trạng thái |
| purpose | String | Mục đích vay |
| applied_date | LocalDateTime | Ngày nộp đơn |
| approved_date | LocalDateTime | Ngày duyệt |
| approved_by | Long | FK -> users (người duyệt) |
| start_date | LocalDate | Ngày bắt đầu tính lãi |
| end_date | LocalDate | Ngày đáo hạn |

#### LoanStatus (Enum)
| Value | Description |
|-------|-------------|
| PENDING | Chờ duyệt |
| APPROVED | Đã duyệt |
| REJECTED | Từ chối |
| DISBURSED | Đã giải ngân |
| PAID | Đã thanh toán hết |
| OVERDUE | Quá hạn |

### 4.2 Chức năng
- [ ] Tạo đơn vay mới
- [ ] Xem danh sách khoản vay
- [ ] Duyệt/Từ chối khoản vay (MANAGER, ADMIN)
- [ ] Giải ngân (tạo LOAN account, nạp tiền)
- [ ] Thanh toán khoản vay (trả góp)
- [ ] Xem lịch trả nợ
- [ ] Cảnh báo khoản vay sắp đến hạn

### 4.3 Business Rules
- Chỉ MANAGER và ADMIN được duyệt khoản vay
- Lãi suất mặc định: 12%/năm
- Công thức tính trả góp hàng tháng (PMT):
  ```
  PMT = P * [r(1+r)^n] / [(1+r)^n - 1]
  Trong đó:
  - P: số tiền vay gốc
  - r: lãi suất tháng (lãi năm / 12)
  - n: số tháng vay
  ```
- Loan number format: `LN` + year + sequence (VD: LN2024000001)

### 4.4 Files cần tạo
```
src/main/java/com/customer/model/Loan.java
src/main/java/com/customer/model/LoanStatus.java
src/main/java/com/customer/dao/LoanDAO.java
src/main/java/com/customer/service/LoanService.java
src/main/java/com/customer/controller/LoanController.java
src/main/resources/views/loan-view.fxml
src/main/resources/views/loan-application-dialog.fxml
src/main/resources/views/loan-approval-dialog.fxml
```

---

## Module 5: Reporting & Export

### 5.1 Báo cáo cần có

#### Dashboard (Thống kê tổng quan)
- Tổng số khách hàng (cá nhân / doanh nghiệp)
- Tổng số tài khoản theo loại
- Tổng số dư toàn hệ thống
- Tổng dư nợ cho vay
- Số giao dịch hôm nay / tuần này / tháng này
- Biểu đồ: phân bố khách hàng, xu hướng giao dịch

#### Báo cáo giao dịch
- Lọc theo: khoảng thời gian, loại giao dịch, tài khoản
- Tổng nạp, tổng rút, tổng chuyển
- Chi tiết từng giao dịch

#### Báo cáo khoản vay
- Tổng dư nợ
- Số khoản vay theo trạng thái
- Danh sách khoản vay sắp đến hạn
- Danh sách nợ quá hạn

### 5.2 Export
- [ ] Export danh sách khách hàng ra Excel
- [ ] Export lịch sử giao dịch ra Excel
- [ ] Export báo cáo khoản vay ra Excel
- [ ] Sao kê tài khoản (PDF - optional)

### 5.3 Dependencies cần thêm (pom.xml)
```xml
<!-- Apache POI for Excel export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 5.4 Files cần tạo
```
src/main/java/com/customer/service/ReportService.java
src/main/java/com/customer/service/ExportService.java
src/main/java/com/customer/controller/DashboardController.java (update)
src/main/java/com/customer/controller/ReportController.java
src/main/resources/views/report-view.fxml
src/main/resources/views/dashboard-view.fxml (update)
```

---

## Module 6: Cập nhật Customer

### 6.1 Thêm fields mới

#### Customer (cập nhật)
| Field | Type | Description |
|-------|------|-------------|
| customer_category | CustomerCategory | INDIVIDUAL / CORPORATE |
| company_name | String | Tên công ty (cho CORPORATE) |
| tax_code | String | Mã số thuế (cho CORPORATE) |
| id_number | String | CMND/CCCD (cho INDIVIDUAL) |
| id_issue_date | LocalDate | Ngày cấp |
| id_issue_place | String | Nơi cấp |

#### CustomerCategory (Enum)
| Value | Description |
|-------|-------------|
| INDIVIDUAL | Khách hàng cá nhân |
| CORPORATE | Khách hàng doanh nghiệp |

### 6.2 Chức năng bổ sung
- [ ] Form đăng ký khác nhau cho cá nhân/doanh nghiệp
- [ ] Lọc khách hàng theo category
- [ ] Validation riêng: tax_code cho doanh nghiệp, id_number cho cá nhân

---

## Database Schema

### Bảng mới cần tạo

```sql
-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT,
    role TEXT NOT NULL DEFAULT 'STAFF',
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_date TEXT NOT NULL,
    last_login TEXT
);

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    account_number TEXT NOT NULL UNIQUE,
    account_type TEXT NOT NULL,
    balance REAL NOT NULL DEFAULT 0,
    interest_rate REAL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    created_date TEXT NOT NULL,
    closed_date TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    transaction_type TEXT NOT NULL,
    amount REAL NOT NULL,
    target_account_id INTEGER,
    balance_after REAL NOT NULL,
    description TEXT,
    reference_number TEXT NOT NULL UNIQUE,
    created_by INTEGER NOT NULL,
    created_date TEXT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (target_account_id) REFERENCES accounts(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Loans table
CREATE TABLE IF NOT EXISTS loans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    loan_account_id INTEGER,
    loan_number TEXT NOT NULL UNIQUE,
    principal_amount REAL NOT NULL,
    interest_rate REAL NOT NULL,
    term_months INTEGER NOT NULL,
    monthly_payment REAL,
    total_paid REAL DEFAULT 0,
    remaining_balance REAL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    purpose TEXT,
    applied_date TEXT NOT NULL,
    approved_date TEXT,
    approved_by INTEGER,
    start_date TEXT,
    end_date TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (loan_account_id) REFERENCES accounts(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Update customers table
ALTER TABLE customers ADD COLUMN customer_category TEXT DEFAULT 'INDIVIDUAL';
ALTER TABLE customers ADD COLUMN company_name TEXT;
ALTER TABLE customers ADD COLUMN tax_code TEXT;
ALTER TABLE customers ADD COLUMN id_number TEXT;
ALTER TABLE customers ADD COLUMN id_issue_date TEXT;
ALTER TABLE customers ADD COLUMN id_issue_place TEXT;

-- Indexes
CREATE INDEX idx_accounts_customer ON accounts(customer_id);
CREATE INDEX idx_accounts_number ON accounts(account_number);
CREATE INDEX idx_transactions_account ON transactions(account_id);
CREATE INDEX idx_transactions_date ON transactions(created_date);
CREATE INDEX idx_loans_customer ON loans(customer_id);
CREATE INDEX idx_loans_status ON loans(status);
```

### Sample Data (users)
```sql
-- Default admin user (password: admin123)
INSERT INTO users (username, password_hash, full_name, email, role, status, created_date)
VALUES ('admin', '$2a$10$...', 'Administrator', 'admin@bank.com', 'ADMIN', 'ACTIVE', datetime('now'));

-- Sample staff
INSERT INTO users (username, password_hash, full_name, email, role, status, created_date)
VALUES ('staff01', '$2a$10$...', 'Nguyen Van A', 'nva@bank.com', 'STAFF', 'ACTIVE', datetime('now'));
```

---

## Thứ tự triển khai (Priority)

### Phase 1: Foundation
1. [ ] User & Authentication module
2. [ ] Cập nhật Customer model (thêm category, company fields)
3. [ ] Database migration script

### Phase 2: Core Banking
4. [ ] Account Management module
5. [ ] Transaction module (deposit, withdraw, transfer)

### Phase 3: Lending
6. [ ] Loan Management module

### Phase 4: Reporting
7. [ ] Dashboard updates
8. [ ] Report screens
9. [ ] Excel export

---

## Tech Notes

### Password Hashing
Sử dụng BCrypt với strength = 10:
```java
// Hash
String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));

// Verify
boolean match = BCrypt.checkpw(password, hash);
```

Thêm dependency:
```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

### BigDecimal for Money
Luôn dùng `BigDecimal` cho tiền, không dùng `double`:
```java
BigDecimal balance = new BigDecimal("1000000.00");
balance = balance.add(new BigDecimal("50000"));
```

### Date/Time
- Dùng `LocalDateTime` cho timestamps
- Dùng `LocalDate` cho ngày (không cần giờ)
- Format: `DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")`

---

## Checklist tổng hợp

- [ ] **Module 1**: User & Authentication
- [ ] **Module 2**: Account Management
- [ ] **Module 3**: Transaction Management
- [ ] **Module 4**: Loan Management
- [ ] **Module 5**: Reporting & Export
- [ ] **Module 6**: Customer Updates
- [ ] **Database**: Migration scripts
- [ ] **Testing**: Unit tests cho services
- [ ] **Documentation**: API docs, user guide
