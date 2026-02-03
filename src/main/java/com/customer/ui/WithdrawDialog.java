package com.customer.ui;

import com.customer.model.Account;
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

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class WithdrawDialog extends Stage {

    private final Account account;
    private final TransactionService transactionService;
    private final ReceiptService receiptService;
    private boolean success = false;

    public WithdrawDialog(Account account) {
        this.account = account;
        this.transactionService = new TransactionService();
        this.receiptService = new ReceiptService();

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Rút tiền từ tài khoản");
        setResizable(false);

        setupUI();
    }

    private void setupUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label headerLabel = new Label("RÚT TIỀN / WITHDRAW");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);

        // Account Info
        grid.add(new Label("Số tài khoản:"), 0, 0);
        Label accNumLabel = new Label(account.getAccountNumber());
        accNumLabel.setStyle("-fx-font-weight: bold;");
        grid.add(accNumLabel, 1, 0);

        grid.add(new Label("Chủ tài khoản:"), 0, 1);
        Label ownerLabel = new Label(account.getCustomerName());
        ownerLabel.setStyle("-fx-font-weight: bold;");
        grid.add(ownerLabel, 1, 1);

        grid.add(new Label("Số dư hiện tại:"), 0, 2);
        Label balanceLabel = new Label(formatCurrency(account.getBalance()));
        balanceLabel.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
        grid.add(balanceLabel, 1, 2);

        // Input Fields
        grid.add(new Label("Số tiền rút:"), 0, 3);
        TextField amountField = new TextField();
        amountField.setPromptText("Nhập số tiền (VND)");
        grid.add(amountField, 1, 3);

        // Real-time formatting helper label
        Label amountFormatLabel = new Label("");
        amountFormatLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        grid.add(amountFormatLabel, 1, 4);

        // Warning Label
        Label warningLabel = new Label("");
        warningLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic; -fx-font-size: 11px;");
        grid.add(warningLabel, 1, 5);

        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                amountField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            try {
                if (!amountField.getText().isEmpty()) {
                    BigDecimal val = new BigDecimal(amountField.getText());
                    amountFormatLabel.setText(formatCurrency(val));

                    // Check balance warning
                    if (val.compareTo(account.getBalance()) > 0) {
                        amountFormatLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                        amountFormatLabel.setText(formatCurrency(val) + " (Vượt quá số dư!)");
                    } else if (val.compareTo(account.getBalance().multiply(new BigDecimal("0.8"))) > 0) {
                        warningLabel.setText("Cảnh báo: Bạn đang rút hơn 80% số dư hiện có");
                        amountFormatLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                    } else {
                        warningLabel.setText("");
                        amountFormatLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                    }
                } else {
                    amountFormatLabel.setText("");
                    warningLabel.setText("");
                }
            } catch (NumberFormatException e) {
                amountFormatLabel.setText("");
            }
        });

        grid.add(new Label("Nội dung:"), 0, 6);
        TextField descField = new TextField();
        descField.setText("Rút tiền mặt");
        grid.add(descField, 1, 6);

        CheckBox printReceiptCb = new CheckBox("In phiếu giao dịch sau khi hoàn tất");
        printReceiptCb.setSelected(true);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Hủy bỏ");
        cancelButton.setOnAction(e -> close());

        Button confirmButton = new Button("Xác nhận rút tiền");
        confirmButton.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmButton.setOnAction(e -> {
            handleWithdraw(amountField.getText(), descField.getText(), printReceiptCb.isSelected());
        });

        buttonBox.getChildren().addAll(cancelButton, confirmButton);

        root.getChildren().addAll(headerLabel, grid, printReceiptCb, buttonBox);

        Scene scene = new Scene(root, 400, 480);
        setScene(scene);
    }

    private void handleWithdraw(String amountStr, String description, boolean printReceipt) {
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
                return transactionService.withdraw(account.getId(), amount, description, userId);
            }
        };

        task.setOnSucceeded(e -> {
            getScene().getRoot().setDisable(false);
            getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            Transaction transaction = task.getValue();

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Giao dịch rút tiền thành công!\nMã GD: " + transaction.getReferenceNumber());

            if (printReceipt) {
                new Thread(() -> {
                    try {
                        transaction.setCreatedDate(java.time.LocalDateTime.now());
                        java.io.File file = receiptService.generateReceipt(transaction, account);
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
