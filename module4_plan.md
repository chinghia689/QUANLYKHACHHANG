# Module 4: Loan Management - Implementation Plan

## Tổng quan

Mục tiêu: Implement Module 4 (Loan Management) với các tính năng:
- ✅ Tạo đơn vay mới (Loan Application)
- ✅ Xem danh sách khoản vay với search/filter
- ✅ Quy trình duyệt khoản vay (MANAGER/ADMIN)
- ✅ Hiển thị lịch trả nợ chi tiết (Amortization Schedule)

**Giai đoạn này KHÔNG bao gồm**: Giải ngân thực tế, Thanh toán khoản vay (sẽ làm ở phase sau)

---

## Business Requirements

### Validation Rules
- **Số tiền vay**: 10,000,000 - 1,000,000,000 VND
- **Kỳ hạn**: 6 - 60 tháng
- **Lãi suất**: Cố định 12%/năm
- **Loan Number Format**: `LN` + year + 6-digit sequence (VD: `LN2026000001`)
- **Reference Number Format**: `LN` + yyyyMMddHHmmssSSS + random 3 digits

### Business Logic
- Tính monthly payment theo công thức PMT: `PMT = P * [r(1+r)^n] / [(1+r)^n - 1]`
  - P: principal amount (số tiền vay gốc)
  - r: monthly interest rate (lãi suất tháng = lãi năm / 12)
  - n: term in months
- Loan status workflow: `PENDING` → `APPROVED` / `REJECTED` → `DISBURSED` → `PAID` / `OVERDUE`
- Chỉ MANAGER và ADMIN có quyền duyệt khoản vay

### Giải ngân Requirements (Validation only - Phase sau mới implement)
- ✅ Kiểm tra khách hàng có ít nhất 1 CHECKING account ACTIVE
- ✅ Kiểm tra khách hàng không có khoản vay cũ chưa thanh toán (status = DISBURSED hoặc OVERDUE)
- ✅ Sẵn sàng ghi transaction log khi giải ngân
- ✅ In phiếu giải ngân (PDF)

---

## Database Schema

### Table: loans

```sql
CREATE TABLE IF NOT EXISTS loans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    loan_account_id BIGINT NULL,  -- FK to accounts (LOAN type) - set after disbursement
    loan_number VARCHAR(30) NOT NULL UNIQUE,
    principal_amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL DEFAULT 12.00,
    term_months INT NOT NULL,
    monthly_payment DECIMAL(15, 2) NOT NULL,
    total_paid DECIMAL(15, 2) NOT NULL DEFAULT 0,
    remaining_balance DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    purpose TEXT NULL,
    applied_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_date DATETIME NULL,
    approved_by BIGINT NULL,  -- FK to users
    approval_note TEXT NULL,  -- Ghi chú khi duyệt/từ chối
    start_date DATE NULL,  -- Ngày bắt đầu tính lãi (sau giải ngân)
    end_date DATE NULL,  -- Ngày đáo hạn
    created_by BIGINT NOT NULL,  -- FK to users (người tạo đơn)
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (loan_account_id) REFERENCES accounts(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Indexes
CREATE INDEX idx_loans_customer_id ON loans(customer_id);
CREATE INDEX idx_loans_loan_number ON loans(loan_number);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_approved_by ON loans(approved_by);
```

### Migration Script Location
- **File**: `src/main/resources/database/mysql-schema.sql`
- **Action**: Append table creation script

---

## Implementation Steps

### Phase 1: Models & Enums

#### 1.1 Create LoanStatus Enum
**File**: `src/main/java/com/customer/model/LoanStatus.java`

```java
public enum LoanStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Từ chối"),
    DISBURSED("Đã giải ngân"),
    PAID("Đã thanh toán"),
    OVERDUE("Quá hạn");

    private final String displayName;

    LoanStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
```

#### 1.2 Create Loan Model
**File**: `src/main/java/com/customer/model/Loan.java`

**Key points**:
- Dùng JavaFX Properties cho all fields (LongProperty, ObjectProperty<BigDecimal>, StringProperty, etc.)
- Transient fields: `customerName` (String), `approverName` (String), `creatorName` (String)
- Constructor: default + full constructor
- Property methods pattern: `getId()`, `setId()`, `idProperty()`

**Fields to include**:
```java
private final LongProperty id;
private final LongProperty customerId;
private final LongProperty loanAccountId;
private final StringProperty loanNumber;
private final ObjectProperty<BigDecimal> principalAmount;
private final ObjectProperty<BigDecimal> interestRate;
private final IntegerProperty termMonths;
private final ObjectProperty<BigDecimal> monthlyPayment;
private final ObjectProperty<BigDecimal> totalPaid;
private final ObjectProperty<BigDecimal> remainingBalance;
private final ObjectProperty<LoanStatus> status;
private final StringProperty purpose;
private final ObjectProperty<LocalDateTime> appliedDate;
private final ObjectProperty<LocalDateTime> approvedDate;
private final LongProperty approvedBy;
private final StringProperty approvalNote;
private final ObjectProperty<LocalDate> startDate;
private final ObjectProperty<LocalDate> endDate;
private final LongProperty createdBy;
private final ObjectProperty<LocalDateTime> createdDate;

// Transient fields (không lưu DB)
private final StringProperty customerName;
private final StringProperty approverName;
private final StringProperty creatorName;
```

**Helper method**:
```java
// Calculate total amount to pay back
public BigDecimal getTotalAmount() {
    if (monthlyPayment != null && termMonths > 0) {
        return monthlyPayment.multiply(new BigDecimal(termMonths));
    }
    return BigDecimal.ZERO;
}
```

---

### Phase 2: Data Access Layer (DAO)

#### 2.1 Create LoanDAO
**File**: `src/main/java/com/customer/dao/LoanDAO.java`

**Methods to implement**:

1. **Constructor**:
   ```java
   private final Connection connection;

   public LoanDAO() {
       this.connection = DatabaseManager.getInstance().getConnection();
   }
   ```

2. **generateLoanNumber()**: `String`
   - Query: `SELECT MAX(loan_number) FROM loans WHERE loan_number LIKE 'LN{year}%'`
   - Logic: Parse sequence, increment +1
   - Default: `LN2026000001`

3. **save(Loan loan)**: `void`
   - Insert loan vào database
   - Capture generated ID với `Statement.RETURN_GENERATED_KEYS`
   - Set ID vào loan object

4. **update(Loan loan)**: `void`
   - Update loan (dùng cho approval, status change)

5. **findById(long id)**: `Loan`
   - Query with JOIN:
     ```sql
     SELECT l.*,
            c.full_name as customer_name,
            u1.full_name as approver_name,
            u2.full_name as creator_name
     FROM loans l
     JOIN customers c ON l.customer_id = c.id
     LEFT JOIN users u1 ON l.approved_by = u1.id
     JOIN users u2 ON l.created_by = u2.id
     WHERE l.id = ?
     ```

6. **findByCustomerId(long customerId)**: `List<Loan>`
   - Tất cả khoản vay của 1 khách hàng

7. **search(String keyword, LoanStatus status, LocalDate fromDate, LocalDate toDate)**: `List<Loan>`
   - Dynamic WHERE clause
   - Parameters:
     - keyword: loan_number LIKE or customer_name LIKE
     - status: filter theo status
     - fromDate/toDate: applied_date BETWEEN
   - Join với customers và users
   - ORDER BY applied_date DESC

8. **findByStatus(LoanStatus status)**: `List<Loan>`
   - Helper method cho approval workflow

9. **hasActiveLoan(long customerId)**: `boolean`
   - Check nếu customer có khoản vay DISBURSED hoặc OVERDUE
   - Dùng cho validation giải ngân

10. **extractLoanFromResultSet(ResultSet rs)**: `Loan` (private helper)
    - Map ResultSet sang Loan object
    - Handle NULL values (approved_date, approved_by, etc.)
    - Set transient fields từ JOIN columns

---

### Phase 3: Service Layer

#### 3.1 Create LoanService
**File**: `src/main/java/com/customer/service/LoanService.java`

**Constants**:
```java
public static final BigDecimal MIN_LOAN_AMOUNT = new BigDecimal("10000000");  // 10M
public static final BigDecimal MAX_LOAN_AMOUNT = new BigDecimal("1000000000"); // 1B
public static final int MIN_TERM_MONTHS = 6;
public static final int MAX_TERM_MONTHS = 60;
public static final BigDecimal INTEREST_RATE = new BigDecimal("12.00");  // 12% per year
```

**Methods to implement**:

1. **applyLoan(long customerId, BigDecimal amount, int termMonths, String purpose, long createdBy)**: `Loan`
   - Validate: amount, term
   - Calculate monthly payment (PMT formula)
   - Generate loan_number
   - Create Loan object with status = PENDING
   - Save to database
   - Return Loan

2. **calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, int months)**: `BigDecimal`
   - Formula: `PMT = P * [r(1+r)^n] / [(1+r)^n - 1]`
   - r = annualRate / 12 / 100 (monthly rate as decimal)
   - Return rounded to 2 decimal places

3. **approveLoan(long loanId, long approvedBy, String note)**: `void`
   - Load loan
   - Check status = PENDING
   - Update: status = APPROVED, approved_by, approved_date = now, approval_note
   - Save

4. **rejectLoan(long loanId, long approvedBy, String reason)**: `void`
   - Similar to approveLoan but status = REJECTED

5. **searchLoans(String keyword, LoanStatus status, LocalDate from, LocalDate to)**: `List<Loan>`
   - Delegate to LoanDAO.search()

6. **canApproveLoan()**: `boolean`
   - Return SessionManager.hasRole(Role.MANAGER, Role.ADMIN)

7. **validateForDisbursement(long customerId)**: `void` throws ValidationException
   - Check customer có CHECKING account ACTIVE (gọi AccountDAO)
   - Check customer không có active loan (gọi LoanDAO.hasActiveLoan())
   - Throw ValidationException nếu fail

8. **generateAmortizationSchedule(Loan loan)**: `List<AmortizationEntry>`
   - Tính lịch trả nợ theo tháng
   - Return list of AmortizationEntry (inner class)

**Inner Classes**:
```java
public static class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}

public static class AmortizationEntry {
    private final int paymentNumber;
    private final LocalDate dueDate;
    private final BigDecimal principalPortion;
    private final BigDecimal interestPortion;
    private final BigDecimal totalPayment;
    private final BigDecimal remainingBalance;

    // Constructor + getters
}
```

**Amortization calculation logic**:
```java
public List<AmortizationEntry> generateAmortizationSchedule(Loan loan) {
    List<AmortizationEntry> schedule = new ArrayList<>();

    BigDecimal monthlyRate = loan.getInterestRate()
        .divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP)
        .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

    BigDecimal remainingBalance = loan.getPrincipalAmount();
    LocalDate currentDate = loan.getStartDate() != null
        ? loan.getStartDate()
        : LocalDate.now();

    for (int i = 1; i <= loan.getTermMonths(); i++) {
        BigDecimal interestPayment = remainingBalance.multiply(monthlyRate)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal principalPayment = loan.getMonthlyPayment().subtract(interestPayment);
        remainingBalance = remainingBalance.subtract(principalPayment);

        LocalDate dueDate = currentDate.plusMonths(i);

        schedule.add(new AmortizationEntry(
            i,
            dueDate,
            principalPayment,
            interestPayment,
            loan.getMonthlyPayment(),
            remainingBalance.max(BigDecimal.ZERO)
        ));
    }

    return schedule;
}
```

---

### Phase 4: UI Layer - Controllers

#### 4.1 Create LoanController
**File**: `src/main/java/com/customer/controller/LoanController.java`

**FXML Components**:
```java
@FXML private TextField searchField;
@FXML private ComboBox<LoanStatus> statusFilter;
@FXML private DatePicker fromDatePicker;
@FXML private DatePicker toDatePicker;
@FXML private Button newLoanButton;
@FXML private Button refreshButton;

// Main table
@FXML private TableView<Loan> loanTable;
@FXML private TableColumn<Loan, String> loanNumberColumn;
@FXML private TableColumn<Loan, String> customerNameColumn;
@FXML private TableColumn<Loan, BigDecimal> principalAmountColumn;
@FXML private TableColumn<Loan, Integer> termMonthsColumn;
@FXML private TableColumn<Loan, BigDecimal> monthlyPaymentColumn;
@FXML private TableColumn<Loan, LoanStatus> statusColumn;
@FXML private TableColumn<Loan, LocalDateTime> appliedDateColumn;

// Detail section buttons
@FXML private Button viewDetailButton;
@FXML private Button approveButton;
@FXML private Button rejectButton;
@FXML private Button disburseButton;

@FXML private StackPane loadingOverlay;
```

**Fields**:
```java
private final LoanService loanService;
private final ObservableList<Loan> loanList;
```

**Methods**:

1. **initialize()**:
   - Setup table columns với PropertyValueFactory
   - Custom cell factories:
     - principalAmountColumn: format currency
     - monthlyPaymentColumn: format currency
     - statusColumn: colored text based on status
     - appliedDateColumn: format datetime
   - Populate statusFilter ComboBox (all values + "Tất cả")
   - Setup selection listener:
     - Enable/disable buttons based on status và role
     - PENDING → approve/reject visible (only MANAGER/ADMIN)
     - APPROVED → disburse visible (only MANAGER/ADMIN)
   - Setup search listener (searchField.textProperty())
   - Setup filter listeners (statusFilter, fromDate, toDate)
   - Call loadLoans()

2. **loadLoans()**: `void`
   - Show loading overlay
   - Create Task<List<Loan>>:
     - Call loanService.searchLoans() với filters
   - On success: update loanList, restore selection
   - On fail: show error alert
   - Hide loading

3. **handleNewLoan()**: `void` [@FXML]
   - Open LoanApplicationDialog
   - If success: reload loans

4. **handleViewDetail()**: `void` [@FXML]
   - Get selected loan
   - Open LoanDetailDialog (show full info + amortization schedule)

5. **handleApprove()**: `void` [@FXML]
   - Get selected loan
   - Open LoanApprovalDialog (mode = APPROVE)
   - If confirmed: reload loans

6. **handleReject()**: `void` [@FXML]
   - Get selected loan
   - Open LoanApprovalDialog (mode = REJECT)
   - If confirmed: reload loans

7. **handleDisburse()**: `void` [@FXML]
   - Placeholder for Phase 2 (giải ngân thực tế)
   - Currently: show info "Chức năng giải ngân sẽ được implement trong phase tiếp theo"

8. **handleRefresh()**: `void` [@FXML]
   - Reload loans

**Permission checks trong initialize()**:
```java
// Hide approve/reject/disburse buttons if not MANAGER/ADMIN
if (!loanService.canApproveLoan()) {
    approveButton.setVisible(false);
    rejectButton.setVisible(false);
    disburseButton.setVisible(false);
}
```

---

#### 4.2 Create LoanApplicationDialog
**File**: `src/main/java/com/customer/ui/LoanApplicationDialog.java`

**Extends**: `Stage`

**Fields**:
```java
private final LoanService loanService;
private final AccountService accountService;
private boolean success = false;

private ComboBox<Customer> customerComboBox;
private TextField amountField;
private ComboBox<Integer> termComboBox;
private TextArea purposeArea;
private Label monthlyPaymentLabel;
private Label totalAmountLabel;
private Label interestRateLabel;
```

**Constructor**:
- Initialize services
- Setup modal
- Call setupUI()

**setupUI()**:
- GridPane layout
- Row 0: Header "TẠO ĐƠN VAY MỚI"
- Row 1: Customer selection (ComboBox với custom cell factory hiển thị ID + Name)
- Row 2: Loan amount (TextField + real-time formatting label)
- Row 3: Term (ComboBox: 6, 12, 18, 24, 36, 48, 60 months)
- Row 4: Interest rate (Label - read-only: "12.00% / năm")
- Row 5: Monthly payment (Label - calculated)
- Row 6: Total amount to pay (Label - calculated)
- Row 7: Purpose (TextArea)
- Row 8: Buttons (Confirm, Cancel)

**Real-time calculation**:
- Listener on amountField + termComboBox
- When both have values:
  - Calculate monthly payment via loanService.calculateMonthlyPayment()
  - Display in monthlyPaymentLabel
  - Calculate total = monthly * term
  - Display in totalAmountLabel

**handleConfirm()**:
- Validate inputs
- Show loading cursor
- Create Task:
  - Call loanService.applyLoan()
- On success:
  - Show success alert với loan_number
  - Set success = true
  - Close dialog
- On fail: show error alert

**Load customers**:
- Task để load customers từ database
- Populate customerComboBox

---

#### 4.3 Create LoanApprovalDialog
**File**: `src/main/java/com/customer/ui/LoanApprovalDialog.java`

**Extends**: `Stage`

**Mode enum**:
```java
public enum ApprovalMode {
    APPROVE, REJECT
}
```

**Fields**:
```java
private final Loan loan;
private final ApprovalMode mode;
private final LoanService loanService;
private boolean success = false;
```

**Constructor**:
```java
public LoanApprovalDialog(Loan loan, ApprovalMode mode) {
    this.loan = loan;
    this.mode = mode;
    this.loanService = new LoanService();
    setupUI();
}
```

**setupUI()**:
- VBox layout
- Header: "DUYỆT KHOẢN VAY" / "TỪ CHỐI KHOẢN VAY" (based on mode)
- Display loan info (read-only):
  - Loan number
  - Customer name
  - Amount
  - Term
  - Monthly payment
- TextArea: Approval note / Rejection reason (required)
- Buttons: Confirm (green/red based on mode), Cancel

**handleConfirm()**:
- Validate note is not empty
- Show loading
- Create Task:
  - If APPROVE: loanService.approveLoan()
  - If REJECT: loanService.rejectLoan()
- On success:
  - Show success alert
  - Set success = true
  - Close
- On fail: show error

---

#### 4.4 Create LoanDetailDialog
**File**: `src/main/java/com/customer/ui/LoanDetailDialog.java`

**Extends**: `Stage`

**Fields**:
```java
private final Loan loan;
private final LoanService loanService;
```

**setupUI()**:
- BorderPane layout
- **Top**: Loan summary card
  - GridPane với loan info (read-only labels)
  - Fields: loan_number, customer_name, principal_amount, interest_rate, term, monthly_payment, total_amount, status, applied_date, approved_date, approver_name, approval_note
- **Center**: Amortization Schedule TableView
  - Columns:
    - Payment # (int)
    - Due Date (LocalDate)
    - Principal (BigDecimal - formatted)
    - Interest (BigDecimal - formatted)
    - Total Payment (BigDecimal - formatted)
    - Remaining Balance (BigDecimal - formatted)
- **Bottom**: Close button

**loadAmortizationSchedule()**:
- Get schedule from loanService.generateAmortizationSchedule(loan)
- Populate TableView
- If loan.startDate is null → show message "Chưa giải ngân - lịch trả nợ chỉ mang tính tham khảo"

---

### Phase 5: FXML Views

#### 5.1 Create loan-view.fxml
**File**: `src/main/resources/views/loan-view.fxml`

**Structure** (similar to account-view.fxml):
```xml
<VBox styleClass="content-area" xmlns:fx="http://javafx.com/fxml">
    <!-- Header -->
    <HBox styleClass="page-header">
        <Label text="💰 Quản lý Khoản vay" styleClass="page-title"/>
    </HBox>

    <!-- Toolbar -->
    <HBox spacing="15" styleClass="toolbar">
        <TextField fx:id="searchField" promptText="Tìm theo mã khoản vay hoặc tên KH..." prefWidth="250"/>

        <Label text="Trạng thái:"/>
        <ComboBox fx:id="statusFilter" prefWidth="130"/>

        <Label text="Từ ngày:"/>
        <DatePicker fx:id="fromDatePicker" prefWidth="130"/>

        <Label text="Đến ngày:"/>
        <DatePicker fx:id="toDatePicker" prefWidth="130"/>

        <Region HBox.hgrow="ALWAYS"/>

        <Button fx:id="newLoanButton" text="+ Tạo đơn vay" onAction="#handleNewLoan"
                style="-fx-background-color: #3498db; -fx-text-fill: white;"/>
        <Button fx:id="refreshButton" text="🔄" onAction="#handleRefresh"/>
    </HBox>

    <!-- Main content: Loan table -->
    <VBox VBox.vgrow="ALWAYS">
        <StackPane VBox.vgrow="ALWAYS">
            <TableView fx:id="loanTable">
                <columns>
                    <TableColumn fx:id="loanNumberColumn" text="Mã khoản vay" prefWidth="120"/>
                    <TableColumn fx:id="customerNameColumn" text="Khách hàng" prefWidth="150"/>
                    <TableColumn fx:id="principalAmountColumn" text="Số tiền vay" prefWidth="130"/>
                    <TableColumn fx:id="termMonthsColumn" text="Kỳ hạn (tháng)" prefWidth="100"/>
                    <TableColumn fx:id="monthlyPaymentColumn" text="Trả hàng tháng" prefWidth="130"/>
                    <TableColumn fx:id="statusColumn" text="Trạng thái" prefWidth="120"/>
                    <TableColumn fx:id="appliedDateColumn" text="Ngày nộp đơn" prefWidth="150"/>
                </columns>
            </TableView>

            <!-- Loading overlay -->
            <StackPane fx:id="loadingOverlay" styleClass="loading-overlay"
                       visible="false" managed="false">
                <ProgressIndicator maxWidth="50" maxHeight="50"/>
            </StackPane>
        </StackPane>

        <!-- Action buttons -->
        <HBox spacing="15" styleClass="toolbar">
            <Button fx:id="viewDetailButton" text="📋 Xem chi tiết" onAction="#handleViewDetail"/>
            <Button fx:id="approveButton" text="✅ Duyệt" onAction="#handleApprove"
                    style="-fx-background-color: #27ae60; -fx-text-fill: white;"/>
            <Button fx:id="rejectButton" text="❌ Từ chối" onAction="#handleReject"
                    style="-fx-background-color: #e74c3c; -fx-text-fill: white;"/>
            <Button fx:id="disburseButton" text="💵 Giải ngân" onAction="#handleDisburse"
                    style="-fx-background-color: #f39c12; -fx-text-fill: white;"/>
        </HBox>
    </VBox>
</VBox>
```

---

### Phase 6: Integration

#### 6.1 Add Menu Item to Dashboard
**File**: `src/main/java/com/customer/controller/DashboardController.java` (hoặc file chính của app)

**Action needed**:
- Add menu item "Quản lý Khoản vay" (icon: 💰)
- Load loan-view.fxml when clicked
- Permission check: All roles có thể xem, chỉ MANAGER/ADMIN mới approve/reject

#### 6.2 Update DatabaseManager
**File**: `src/main/java/com/customer/dao/DatabaseManager.java`

**Action needed** (nếu chưa có):
- Ensure database schema được tạo khi app start
- Run migration script nếu table `loans` chưa tồn tại

---

## Testing Plan

### Unit Tests (Optional - nếu có thời gian)
- LoanService.calculateMonthlyPayment() - test với different amounts/terms
- LoanService.generateAmortizationSchedule() - verify tổng = principal + interest
- LoanDAO.generateLoanNumber() - test sequence increment

### Manual Testing Scenarios

1. **Create Loan Application**:
   - Open dialog, chọn customer
   - Input: 50,000,000 VND, 12 tháng
   - Verify monthly payment calculated correctly (~4,440,000 VND)
   - Submit → check loan saved với status = PENDING

2. **Search & Filter**:
   - Filter by status: PENDING
   - Search by loan_number
   - Date range filter

3. **Approve Loan**:
   - Login as MANAGER/ADMIN
   - Select PENDING loan
   - Click Approve, enter note
   - Verify status changed to APPROVED, approved_date set

4. **Reject Loan**:
   - Select PENDING loan
   - Click Reject, enter reason
   - Verify status = REJECTED

5. **View Detail & Amortization**:
   - Click on any loan
   - Verify detail dialog shows correct info
   - Check amortization table has correct number of rows (= term_months)
   - Verify last row remaining balance = 0 (or very close)

6. **Permission Checks**:
   - Login as STAFF
   - Verify approve/reject/disburse buttons are hidden or disabled
   - Login as MANAGER → buttons visible

7. **Validation**:
   - Try create loan với amount < 10M → should fail
   - Try create loan với amount > 1B → should fail
   - Try create loan với term < 6 months → should fail

---

## File Structure Summary

```
src/main/java/com/customer/
├── model/
│   ├── Loan.java                    [NEW]
│   └── LoanStatus.java              [NEW]
├── dao/
│   └── LoanDAO.java                 [NEW]
├── service/
│   └── LoanService.java             [NEW]
├── controller/
│   └── LoanController.java          [NEW]
└── ui/
    ├── LoanApplicationDialog.java   [NEW]
    ├── LoanApprovalDialog.java      [NEW]
    └── LoanDetailDialog.java        [NEW]

src/main/resources/
├── database/
│   └── mysql-schema.sql             [UPDATE - append loans table]
└── views/
    └── loan-view.fxml               [NEW]
```

---

## Critical Files to Modify

1. **src/main/resources/database/mysql-schema.sql**
   - Line ~420 (end of file)
   - Append: loans table creation + indexes

2. **Dashboard/Menu Controller** (TBD - cần check tên file)
   - Add "Quản lý Khoản vay" menu item
   - Load loan-view.fxml

3. **DatabaseManager.java** (nếu cần)
   - Ensure migration runs

---

## Dependencies Check

All required dependencies already exist in pom.xml:
- ✅ JavaFX (for UI)
- ✅ MySQL Connector (for database)
- ✅ BCrypt (for auth - already have)

No new dependencies needed for this module.

---

## Verification Steps

After implementation, verify:

1. ✅ Database table `loans` created successfully
2. ✅ Can create new loan application (status = PENDING)
3. ✅ Loan number auto-generated correctly (format: LN2026XXXXXX)
4. ✅ Monthly payment calculated accurately
5. ✅ MANAGER/ADMIN can approve/reject loans
6. ✅ STAFF cannot see approve/reject buttons
7. ✅ Amortization schedule displays correctly (60 rows for 60-month loan)
8. ✅ Search and filters work properly
9. ✅ All validations enforce business rules
10. ✅ UI follows existing design patterns (colors, fonts, spacing)

---

## Notes

- **Giải ngân thực tế** (disbursement) sẽ làm ở Phase sau:
  - Tạo LOAN account
  - Transfer tiền vào CHECKING account
  - Update loan status = DISBURSED
  - Set start_date, end_date
  - Ghi transaction log
  - In phiếu giải ngân PDF

- **Thanh toán khoản vay** (payment) cũng sẽ làm ở Phase sau:
  - Payment dialog
  - Update total_paid, remaining_balance
  - Check OVERDUE status
  - Payment history table

---

## Estimated Complexity

- **Model & Enum**: Simple
- **DAO**: Medium (nhiều methods, dynamic queries)
- **Service**: Medium (business logic, calculation)
- **Controller**: Medium (TableView setup, filters)
- **Dialogs**: Medium (3 dialogs with different purposes)
- **FXML**: Simple

**Overall**: Medium complexity module, consistent with existing patterns.
