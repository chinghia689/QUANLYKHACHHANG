package com.customer.controller;

import com.customer.model.Loan;
import com.customer.model.LoanStatus;
import com.customer.service.LoanService;
import com.customer.ui.LoanApplicationDialog;
import com.customer.ui.LoanApprovalDialog;
import com.customer.ui.LoanDetailDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class LoanController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
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
    @FXML private TableColumn<Loan, String> statusColumn;
    @FXML private TableColumn<Loan, String> appliedDateColumn;

    // Detail section buttons
    @FXML private Button viewDetailButton;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button disburseButton;

    @FXML private StackPane loadingOverlay;

    private final LoanService loanService;
    private final ObservableList<Loan> loanList;
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public LoanController() {
        this.loanService = new LoanService();
        this.loanList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        setupButtons();

        loadLoans();
    }

    private void setupTable() {
        loanNumberColumn.setCellValueFactory(new PropertyValueFactory<>("loanNumber"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));

        principalAmountColumn.setCellValueFactory(new PropertyValueFactory<>("principalAmount"));
        principalAmountColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        termMonthsColumn.setCellValueFactory(new PropertyValueFactory<>("termMonths"));
        termMonthsColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " tháng");
                }
            }
        });

        monthlyPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("monthlyPayment"));
        monthlyPaymentColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(currencyFormat.format(item));
                }
            }
        });

        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        statusColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    LoanStatus status = getTableView().getItems().get(getIndex()).getStatus();
                    String color = switch (status) {
                        case PENDING -> "#f39c12"; // Orange
                        case APPROVED -> "#27ae60"; // Green
                        case REJECTED -> "#c0392b"; // Red
                        case DISBURSED -> "#2980b9"; // Blue
                        case PAID -> "#8e44ad"; // Purple
                        case OVERDUE -> "#e74c3c"; // Red
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        appliedDateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getAppliedDate().format(dateTimeFormatter)));

        loanTable.setItems(loanList);

        loanTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonState(newVal);
        });
    }

    private void setupFilters() {
        ObservableList<String> statuses = FXCollections.observableArrayList("Tất cả");
        for (LoanStatus status : LoanStatus.values()) {
            statuses.add(status.getDisplayName());
        }
        statusFilter.setItems(statuses);
        statusFilter.setValue("Tất cả");

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadLoans());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Debounce could be added here
            loadLoans();
        });
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadLoans());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadLoans());
    }

    private void setupButtons() {
        // Initial state
        viewDetailButton.setDisable(true);
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        disburseButton.setDisable(true);

        // Hide sensitive buttons if not authorized
        if (!loanService.canApproveLoan()) {
            approveButton.setVisible(false);
            rejectButton.setVisible(false);
            disburseButton.setVisible(false);
        }
    }

    private void updateButtonState(Loan loan) {
        if (loan == null) {
            viewDetailButton.setDisable(true);
            approveButton.setDisable(true);
            rejectButton.setDisable(true);
            disburseButton.setDisable(true);
            return;
        }

        viewDetailButton.setDisable(false);

        if (loanService.canApproveLoan()) {
            boolean isPending = loan.getStatus() == LoanStatus.PENDING;
            boolean isApproved = loan.getStatus() == LoanStatus.APPROVED;

            approveButton.setDisable(!isPending);
            rejectButton.setDisable(!isPending);
            disburseButton.setDisable(!isApproved);
        }
    }

    private void loadLoans() {
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);

        String keyword = searchField.getText();
        String statusStr = statusFilter.getValue();
        LoanStatus status = null;
        if (statusStr != null && !statusStr.equals("Tất cả")) {
            for (LoanStatus s : LoanStatus.values()) {
                if (s.getDisplayName().equals(statusStr)) {
                    status = s;
                    break;
                }
            }
        }

        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        LoanStatus finalStatus = status;
        Task<List<Loan>> task = new Task<>() {
            @Override
            protected List<Loan> call() throws Exception {
                return loanService.searchLoans(keyword, finalStatus, fromDate, toDate);
            }
        };

        task.setOnSucceeded(e -> {
            loanList.setAll(task.getValue());
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            updateButtonState(loanTable.getSelectionModel().getSelectedItem());
        });

        task.setOnFailed(e -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
            e.getSource().getException().printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách khoản vay: " + e.getSource().getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleNewLoan() {
        LoanApplicationDialog dialog = new LoanApplicationDialog();
        dialog.showAndWait();
        if (dialog.isSuccess()) {
            loadLoans();
        }
    }

    @FXML
    private void handleRefresh() {
        loadLoans();
    }

    @FXML
    private void handleViewDetail() {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            new LoanDetailDialog(selected).show();
        }
    }

    @FXML
    private void handleApprove() {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LoanApprovalDialog dialog = new LoanApprovalDialog(selected, LoanApprovalDialog.ApprovalMode.APPROVE);
            dialog.showAndWait();
            if (dialog.isSuccess()) {
                loadLoans();
            }
        }
    }

    @FXML
    private void handleReject() {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            LoanApprovalDialog dialog = new LoanApprovalDialog(selected, LoanApprovalDialog.ApprovalMode.REJECT);
            dialog.showAndWait();
            if (dialog.isSuccess()) {
                loadLoans();
            }
        }
    }

    @FXML
    private void handleDisburse() {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            loanService.validateForDisbursement(selected.getCustomerId());

            // Phase 2 implementation placeholder
            showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                "Chức năng giải ngân sẽ được triển khai trong bản cập nhật tiếp theo.\n" +
                "Hiện tại chỉ hỗ trợ validation.");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Không thể giải ngân", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
