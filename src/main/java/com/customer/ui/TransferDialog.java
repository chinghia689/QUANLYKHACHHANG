package com.customer.ui;

import com.customer.dao.AccountDAO;
import com.customer.model.Account;
import com.customer.model.AccountStatus;
import com.customer.model.Transaction;
import com.customer.service.ReceiptService;
import com.customer.service.TransactionService;
import com.customer.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class TransferDialog extends Stage {

    private final Account sourceAccount;
    private final TransactionService transactionService;
    private final ReceiptService receiptService;
    private final AccountDAO accountDAO;
    private boolean success = false;

    public TransferDialog(Account sourceAccount) {
        this.sourceAccount = sourceAccount;
        this.transactionService = new TransactionService();
        this.receiptService = new ReceiptService();
        this.accountDAO = new AccountDAO();

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Chuyển khoản");
        setResizable(false);

        setupUI();
    }

    private void setupUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label headerLabel = new Label("CHUYỂN KHOẢN / TRANSFER");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #8e44ad;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Source Account Info
        grid.add(new Label("Tài khoản nguồn:"), 0, 0);
        Label accNumLabel = new Label(sourceAccount.getAccountNumber() + " (" + sourceAccount.getCustomerName() + ")");
        accNumLabel.setStyle("-fx-font-weight: bold;");
        grid.add(accNumLabel, 1, 0);

        grid.add(new Label("Số dư khả dụng:"), 0, 1);
        Label balanceLabel = new Label(formatCurrency(sourceAccount.getBalance()));
        balanceLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
        grid.add(balanceLabel, 1, 1);

        // Target Account Selection
        grid.add(new Label("Tài khoản đích:"), 0, 2);
        ComboBox<Account> targetAccountCombo = new ComboBox<>();
        targetAccountCombo.setPromptText("Chọn tài khoản nhận...");
        targetAccountCombo.setPrefWidth(250);
        loadTargetAccounts(targetAccountCombo);

        targetAccountCombo.setConverter(new StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                if (account == null) return null;
                return account.getAccountNumber() + " - " + account.getCustomerName();
            }

            @Override
            public Account fromString(String string) {
                return null; // No need
            }
        });
        grid.add(targetAccountCombo, 1, 2);

        // Input Fields
        grid.add(new Label("Số tiền chuyển:"), 0, 3);
        TextField amountField = new TextField();
        amountField.setPromptText("Nhập số tiền (VND)");
        grid.add(amountField, 1, 3);

        Label amountFormatLabel = new Label("");
        amountFormatLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        grid.add(amountFormatLabel, 1, 4);

        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            try {
                if (!amountField.getText().isEmpty()) {
                    BigDecimal val = new BigDecimal(amountField.getText());
                    amountFormatLabel.setText(formatCurrency(val));
                    if (val.compareTo(sourceAccount.getBalance()) > 0) {
                        amountFormatLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                        amountFormatLabel.setText(formatCurrency(val) + " (Vượt quá số dư!)");
                    } else {
                        amountFormatLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                    }
                } else {
                    amountFormatLabel.setText("");
                }
            } catch (NumberFormatException e) {
                amountFormatLabel.setText("");
            }
        });

        grid.add(new Label("Nội dung:"), 0, 5);
        TextField descField = new TextField();
        descField.setText("Chuyen khoan");
        grid.add(descField, 1, 5);

        CheckBox printReceiptCb = new CheckBox("In phiếu giao dịch sau khi hoàn tất");
        printReceiptCb.setSelected(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Hủy bỏ");
        cancelButton.setOnAction(e -> close());

        Button confirmButton = new Button("Xác nhận chuyển");
        confirmButton.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmButton.setOnAction(e -> {
            handleTransfer(targetAccountCombo.getValue(), amountField.getText(), descField.getText(), printReceiptCb.isSelected());
        });

        buttonBox.getChildren().addAll(cancelButton, confirmButton);

        root.getChildren().addAll(headerLabel, grid, printReceiptCb, buttonBox);

        Scene scene = new Scene(root, 450, 500);
        setScene(scene);
    }

    private void loadTargetAccounts(ComboBox<Account> comboBox) {
        try {
            List<Account> accounts = accountDAO.findAll();
            // Filter out source account, non-active accounts, etc. if needed
            // Requirement says: "source != target (nhưng cho phép cùng customer)"
            accounts.removeIf(a -> a.getId() == sourceAccount.getId() || a.getStatus() != AccountStatus.ACTIVE);
            comboBox.getItems().addAll(accounts);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách tài khoản");
        }
    }

    private void handleTransfer(Account targetAccount, String amountStr, String description, boolean printReceipt) {
        if (targetAccount == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn tài khoản đích");
            return;
        }
        if (amountStr == null || amountStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số tiền");
            return;
        }

        getScene().getRoot().setDisable(true);
        getScene().setCursor(javafx.scene.Cursor.WAIT);

        javafx.concurrent.Task<Transaction> task = new javafx.concurrent.Task<>() {
            @Override
            protected Transaction call() throws Exception {
                BigDecimal amount = new BigDecimal(amountStr);
                long userId = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getId() : 1;
                return transactionService.transfer(sourceAccount.getId(), targetAccount.getId(), amount, description, userId);
            }
        };

        task.setOnSucceeded(e -> {
            getScene().getRoot().setDisable(false);
            getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            Transaction transaction = task.getValue();

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Chuyển khoản thành công!\nMã GD: " + transaction.getReferenceNumber());

            if (printReceipt) {
                new Thread(() -> {
                    try {
                        transaction.setCreatedDate(java.time.LocalDateTime.now());
                        java.io.File file = receiptService.generateReceipt(transaction, sourceAccount);
                        javafx.application.Platform.runLater(() -> receiptService.openReceipt(file));
                    } catch (Exception ex) {
                        javafx.application.Platform.runLater(() ->
                            showAlert(Alert.AlertType.WARNING, "Lỗi in phiếu", "Giao dịch thành công nhưng không thể in phiếu: " + ex.getMessage())
                        );
                        ex.printStackTrace();
                    }
                }).start();
            }

            success = true;
            close();
        });

        task.setOnFailed(e -> {
            getScene().getRoot().setDisable(false);
            getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            Throwable ex = task.getException();
            if (ex instanceof NumberFormatException) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền không hợp lệ");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi giao dịch", ex.getMessage());
            }
            ex.printStackTrace();
        });

        new Thread(task).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VND";
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(amount) + " VND";
    }

    public boolean isSuccess() {
        return success;
    }
}
