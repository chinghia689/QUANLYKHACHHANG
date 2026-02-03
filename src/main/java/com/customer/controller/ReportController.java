package com.customer.controller;

import com.customer.dao.AccountDAO;
import com.customer.model.*;
import com.customer.model.dto.*;
import com.customer.service.ExportService;
import com.customer.service.ReportService;
import com.customer.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportController {

    @FXML private TabPane reportTabPane;
    @FXML private StackPane loadingOverlay;

    // --- Dashboard Tab ---
    @FXML private Label lblTotalCustomers;
    @FXML private Label lblTotalBalance;
    @FXML private Label lblTotalLoans;
    @FXML private Label lblTodayTransactions;
    @FXML private Label lblCustomerChange;
    @FXML private Label lblBalanceChange;

    @FXML private PieChart customerDistChart;
    @FXML private PieChart accountTypeChart;
    @FXML private BarChart<String, Number> transactionBarChart;
    @FXML private BarChart<String, Number> loanStatusChart;
    @FXML private LineChart<String, Number> balanceTrendChart;

    @FXML private TableView<Account> topAccountsTable;
    @FXML private TableColumn<Account, String> colAccNumber;
    @FXML private TableColumn<Account, String> colAccCustomer;
    @FXML private TableColumn<Account, String> colAccType;
    @FXML private TableColumn<Account, BigDecimal> colAccBalance;
    @FXML private TableColumn<Account, String> colAccStatus;

    @FXML private Button exportDashboardBtn;

    // --- Transaction Report Tab ---
    @FXML private DatePicker txnFromDate;
    @FXML private DatePicker txnToDate;
    @FXML private ComboBox<TransactionType> txnTypeFilter;
    @FXML private ComboBox<Account> txnAccountFilter;
    @FXML private Button generateTxnReportBtn;
    @FXML private Label lblTxnCount;
    @FXML private Label lblTxnTotal;
    @FXML private TableView<Transaction> txnReportTable;
    @FXML private TableColumn<Transaction, String> colTxnDate;
    @FXML private TableColumn<Transaction, String> colTxnRef;
    @FXML private TableColumn<Transaction, String> colTxnAccount;
    @FXML private TableColumn<Transaction, String> colTxnType;
    @FXML private TableColumn<Transaction, BigDecimal> colTxnAmount;
    @FXML private TableColumn<Transaction, String> colTxnTarget;
    @FXML private TableColumn<Transaction, String> colTxnDesc;
    @FXML private Button exportTxnReportBtn;

    // --- Loan Report Tab ---
    @FXML private DatePicker loanFromDate;
    @FXML private DatePicker loanToDate;
    @FXML private ComboBox<LoanStatus> loanStatusFilter;
    @FXML private Button generateLoanReportBtn;
    @FXML private Label lblLoanCount;
    @FXML private Label lblLoanOutstanding;
    @FXML private TableView<Loan> loanReportTable;
    @FXML private TableColumn<Loan, Long> colLoanId;
    @FXML private TableColumn<Loan, String> colLoanCustomer;
    @FXML private TableColumn<Loan, BigDecimal> colLoanAmount;
    @FXML private TableColumn<Loan, String> colLoanStart;
    @FXML private TableColumn<Loan, Integer> colLoanTerm;
    @FXML private TableColumn<Loan, String> colLoanStatus;
    @FXML private Button exportLoanReportBtn;

    // --- Account Statement Tab ---
    @FXML private ComboBox<Account> stmtAccountCombo;
    @FXML private DatePicker stmtFromDate;
    @FXML private DatePicker stmtToDate;
    @FXML private Button generateStmtBtn;
    @FXML private Label lblStmtOpening;
    @FXML private Label lblStmtIn;
    @FXML private Label lblStmtOut;
    @FXML private Label lblStmtClosing;
    @FXML private TableView<Transaction> stmtTable;
    @FXML private TableColumn<Transaction, String> colStmtDate;
    @FXML private TableColumn<Transaction, String> colStmtRef;
    @FXML private TableColumn<Transaction, String> colStmtDesc;
    @FXML private TableColumn<Transaction, BigDecimal> colStmtDebit;
    @FXML private TableColumn<Transaction, BigDecimal> colStmtCredit;
    @FXML private Button exportStatementBtn;

    private final ReportService reportService = new ReportService();
    private final ExportService exportService = new ExportService();
    private final AccountDAO accountDAO = new AccountDAO();

    // Data holders for export
    private DashboardStats currentDashboardStats;
    private TransactionReportData currentTxnData;
    private LoanReportData currentLoanData;
    private AccountStatementData currentStmtData;

    @FXML
    public void initialize() {
        setupTables();
        setupFilters();
        setupPermissions();

        // Load initial data for Dashboard
        loadDashboardData();

        // Load account list for filters
        loadAccountList();
    }

    private void setupPermissions() {
        boolean canExport = SessionManager.hasRole(Role.MANAGER, Role.ADMIN);
        exportDashboardBtn.setVisible(canExport);
        exportTxnReportBtn.setVisible(canExport);
        exportLoanReportBtn.setVisible(canExport);
        exportStatementBtn.setVisible(canExport);
    }

    private void setupTables() {
        // Dashboard Top Accounts
        colAccNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colAccCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAccType.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        colAccBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colAccStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        formatCurrencyColumn(colAccBalance);

        // Transaction Report
        colTxnDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colTxnRef.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
        colTxnAccount.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        colTxnType.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        colTxnAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTxnTarget.setCellValueFactory(new PropertyValueFactory<>("targetAccountNumber"));
        colTxnDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        formatDateTimeColumn(colTxnDate);
        formatCurrencyColumn(colTxnAmount);

        // Loan Report
        colLoanId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colLoanCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colLoanAmount.setCellValueFactory(new PropertyValueFactory<>("principalAmount"));
        colLoanStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colLoanTerm.setCellValueFactory(new PropertyValueFactory<>("termMonths"));
        colLoanStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        formatCurrencyColumn(colLoanAmount);

        // Statement
        colStmtDate.setCellValueFactory(new PropertyValueFactory<>("createdDate"));
        colStmtRef.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
        colStmtDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Special handling for Debit/Credit columns in Statement
        colStmtDebit.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            boolean isCredit = isCreditTransaction(t, stmtAccountCombo.getValue());
            return javafx.beans.binding.Bindings.createObjectBinding(() -> isCredit ? null : t.getAmount());
        });

        colStmtCredit.setCellValueFactory(cellData -> {
            Transaction t = cellData.getValue();
            boolean isCredit = isCreditTransaction(t, stmtAccountCombo.getValue());
            return javafx.beans.binding.Bindings.createObjectBinding(() -> isCredit ? t.getAmount() : null);
        });

        formatDateTimeColumn(colStmtDate);
        formatCurrencyColumn(colStmtDebit);
        formatCurrencyColumn(colStmtCredit);
    }

    private boolean isCreditTransaction(Transaction t, Account account) {
        if (account == null) return false;
        if (t.getTransactionType() == TransactionType.DEPOSIT || t.getTransactionType() == TransactionType.LOAN_DISBURSEMENT) return true;
        return t.getTransactionType() == TransactionType.TRANSFER && t.getTargetAccountId() == account.getId();
    }

    private <T> void formatCurrencyColumn(TableColumn<T, BigDecimal> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item));
                }
            }
        });
    }

    private <T> void formatDateTimeColumn(TableColumn<T, ?> column) {
        // Simple default formatter, can be enhanced
        // For LocalDate/LocalDateTime types handled by toString() usually, but better to format
    }

    private void setupFilters() {
        // Txn Types
        txnTypeFilter.setItems(FXCollections.observableArrayList(TransactionType.values()));

        // Loan Status
        loanStatusFilter.setItems(FXCollections.observableArrayList(LoanStatus.values()));

        // Defaults
        txnToDate.setValue(LocalDate.now());
        txnFromDate.setValue(LocalDate.now().minusMonths(1));

        loanToDate.setValue(LocalDate.now());
        loanFromDate.setValue(LocalDate.now().minusMonths(1));

        stmtToDate.setValue(LocalDate.now());
        stmtFromDate.setValue(LocalDate.now().minusMonths(1));

        // Account Converters
        StringConverter<Account> accConverter = new StringConverter<>() {
            @Override
            public String toString(Account object) {
                return object == null ? null : object.getAccountNumber() + " - " + object.getCustomerName();
            }

            @Override
            public Account fromString(String string) {
                return null; // Not needed
            }
        };
        txnAccountFilter.setConverter(accConverter);
        stmtAccountCombo.setConverter(accConverter);
    }

    private void loadAccountList() {
        Task<List<Account>> task = new Task<>() {
            @Override
            protected List<Account> call() throws Exception {
                return accountDAO.findAll();
            }

            @Override
            protected void succeeded() {
                List<Account> accounts = getValue();
                txnAccountFilter.setItems(FXCollections.observableArrayList(accounts));
                stmtAccountCombo.setItems(FXCollections.observableArrayList(accounts));
            }
        };
        new Thread(task).start();
    }

    // --- Dashboard Actions ---

    private void loadDashboardData() {
        showLoading(true);
        Task<DashboardStats> task = new Task<>() {
            @Override
            protected DashboardStats call() throws Exception {
                return reportService.getDashboardStats();
            }

            @Override
            protected void succeeded() {
                currentDashboardStats = getValue();
                updateDashboardUI(currentDashboardStats);
                showLoading(false);
            }

            @Override
            protected void failed() {
                showLoading(false);
                showError("Error", "Failed to load dashboard data: " + getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    private void updateDashboardUI(DashboardStats stats) {
        lblTotalCustomers.setText(String.valueOf(stats.getTotalCustomers()));
        lblTotalBalance.setText(String.format("%,.0f VND", stats.getTotalBalance()));
        lblTotalLoans.setText(String.format("%,.0f VND", stats.getTotalLoans()));
        lblTodayTransactions.setText(String.valueOf(stats.getTodayTransactions()));

        double custChange = stats.getCustomerChangePercent();
        lblCustomerChange.setText(String.format("%+.1f%%", custChange));
        lblCustomerChange.setStyle("-fx-text-fill: " + (custChange >= 0 ? "green" : "red"));

        double balChange = stats.getBalanceChangePercent();
        lblBalanceChange.setText(String.format("%+.1f%%", balChange));
        lblBalanceChange.setStyle("-fx-text-fill: " + (balChange >= 0 ? "green" : "red"));

        // Pie Charts
        updatePieChart(customerDistChart, stats.getCustomerDistribution());
        updatePieChart(accountTypeChart, stats.getAccountTypeDistribution());

        // Bar Charts
        updateBarChart(transactionBarChart, stats.getTransactionTrend());
        updateBarChartFromLoanMap(loanStatusChart, stats.getLoanStatusDistribution());

        // Line Chart
        updateLineChart(balanceTrendChart, stats.getBalanceTrend());

        // Top Accounts
        topAccountsTable.setItems(FXCollections.observableArrayList(stats.getTopAccounts()));
    }

    private <K> void updatePieChart(PieChart chart, Map<K, Integer> data) {
        chart.getData().clear();
        data.forEach((key, value) -> {
            // Get simple string representation for key
            String name = key.toString();
            if (key instanceof CustomerType) name = ((CustomerType) key).getDisplayName();
            chart.getData().add(new PieChart.Data(name, value));
        });
    }

    private void updateBarChart(BarChart<String, Number> chart, Map<String, Integer> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Count");
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        chart.getData().add(series);
    }

    private void updateBarChartFromLoanMap(BarChart<String, Number> chart, Map<LoanStatus, Integer> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Loans");
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key.name(), value)));
        chart.getData().add(series);
    }

    private void updateLineChart(LineChart<String, Number> chart, Map<String, BigDecimal> data) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Net Balance");
        data.forEach((key, value) -> series.getData().add(new XYChart.Data<>(key, value)));
        chart.getData().add(series);
    }

    @FXML
    private void handleExportDashboard() {
        if (currentDashboardStats == null) return;
        export(task -> exportService.exportDashboardReport(currentDashboardStats));
    }

    // --- Transaction Report Actions ---

    @FXML
    private void handleGenerateTxnReport() {
        LocalDate from = txnFromDate.getValue();
        LocalDate to = txnToDate.getValue();
        if (!validateDates(from, to)) return;

        showLoading(true);
        Task<TransactionReportData> task = new Task<>() {
            @Override
            protected TransactionReportData call() throws Exception {
                Long accId = txnAccountFilter.getValue() != null ? txnAccountFilter.getValue().getId() : null;
                return reportService.getTransactionReport(from, to, txnTypeFilter.getValue(), accId);
            }

            @Override
            protected void succeeded() {
                currentTxnData = getValue();
                txnReportTable.setItems(FXCollections.observableArrayList(currentTxnData.getTransactions()));
                lblTxnCount.setText("Total Count: " + currentTxnData.getTotalCount());
                lblTxnTotal.setText("Total Amount: " + String.format("%,.0f VND", currentTxnData.getTotalAmount()));
                exportTxnReportBtn.setDisable(currentTxnData.getTransactions().isEmpty());
                showLoading(false);
            }

            @Override
            protected void failed() {
                showLoading(false);
                showError("Error", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleExportTxnReport() {
        if (currentTxnData == null) return;
        export(task -> exportService.exportTransactionReport(currentTxnData));
    }

    // --- Loan Report Actions ---

    @FXML
    private void handleGenerateLoanReport() {
        LocalDate from = loanFromDate.getValue();
        LocalDate to = loanToDate.getValue();
        if (!validateDates(from, to)) return;

        showLoading(true);
        Task<LoanReportData> task = new Task<>() {
            @Override
            protected LoanReportData call() throws Exception {
                return reportService.getLoanReport(from, to, loanStatusFilter.getValue());
            }

            @Override
            protected void succeeded() {
                currentLoanData = getValue();
                loanReportTable.setItems(FXCollections.observableArrayList(currentLoanData.getLoans()));
                lblLoanCount.setText("Total Loans: " + currentLoanData.getTotalLoans());
                lblLoanOutstanding.setText("Total Outstanding: " + String.format("%,.0f VND", currentLoanData.getTotalOutstanding()));
                exportLoanReportBtn.setDisable(currentLoanData.getLoans().isEmpty());
                showLoading(false);
            }

            @Override
            protected void failed() {
                showLoading(false);
                showError("Error", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleExportLoanReport() {
        if (currentLoanData == null) return;
        export(task -> exportService.exportLoanReport(currentLoanData));
    }

    // --- Statement Actions ---

    @FXML
    private void handleGenerateStatement() {
        Account account = stmtAccountCombo.getValue();
        if (account == null) {
            showError("Input Required", "Please select an account.");
            return;
        }
        LocalDate from = stmtFromDate.getValue();
        LocalDate to = stmtToDate.getValue();
        if (!validateDates(from, to)) return;

        showLoading(true);
        Task<AccountStatementData> task = new Task<>() {
            @Override
            protected AccountStatementData call() throws Exception {
                return reportService.getAccountStatement(account, from, to);
            }

            @Override
            protected void succeeded() {
                currentStmtData = getValue();
                stmtTable.setItems(FXCollections.observableArrayList(currentStmtData.getTransactions()));
                lblStmtOpening.setText("Opening: " + String.format("%,.0f VND", currentStmtData.getOpeningBalance()));
                lblStmtIn.setText("In: " + String.format("%,.0f VND", currentStmtData.getTotalDeposits()));
                lblStmtOut.setText("Out: " + String.format("%,.0f VND", currentStmtData.getTotalWithdrawals()));
                lblStmtClosing.setText("Closing: " + String.format("%,.0f VND", currentStmtData.getClosingBalance()));

                exportStatementBtn.setDisable(currentStmtData.getTransactions().isEmpty());
                showLoading(false);
            }

            @Override
            protected void failed() {
                showLoading(false);
                showError("Error", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleExportStatement() {
        if (currentStmtData == null) return;
        export(task -> exportService.exportAccountStatement(currentStmtData));
    }

    // --- Helpers ---

    private boolean validateDates(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            showError("Input Error", "Please select both start and end dates.");
            return false;
        }
        if (from.isAfter(to)) {
            showError("Input Error", "Start date cannot be after end date.");
            return false;
        }
        return true;
    }

    private interface ExportTask {
        File execute(Void v) throws Exception;
    }

    private void export(ExportTask exportAction) {
        showLoading(true);
        Task<File> task = new Task<>() {
            @Override
            protected File call() throws Exception {
                return exportAction.execute(null);
            }

            @Override
            protected void succeeded() {
                File file = getValue();
                showLoading(false);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Export Success");
                alert.setHeaderText("Report exported successfully!");
                alert.setContentText("File saved at: " + file.getAbsolutePath() + "\n\nDo you want to open it?");
                alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                alert.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.YES) {
                        exportService.openPdf(file);
                    }
                });
            }

            @Override
            protected void failed() {
                showLoading(false);
                showError("Export Error", getException().getMessage());
            }
        };
        new Thread(task).start();
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
