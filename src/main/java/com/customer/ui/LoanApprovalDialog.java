package com.customer.ui;

import com.customer.model.Loan;
import com.customer.service.LoanService;
import com.customer.util.SessionManager;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class LoanApprovalDialog extends Stage {
    public enum ApprovalMode {
        APPROVE, REJECT
    }

    private final Loan loan;
    private final ApprovalMode mode;
    private final LoanService loanService;
    private boolean success = false;

    private TextArea noteArea;
    private Label messageLabel;
    private Button confirmButton;

    public LoanApprovalDialog(Loan loan, ApprovalMode mode) {
        this.loan = loan;
        this.mode = mode;
        this.loanService = new LoanService();

        initModality(Modality.APPLICATION_MODAL);
        setTitle(mode == ApprovalMode.APPROVE ? "Duyệt Khoản Vay" : "Từ Chối Khoản Vay");
        setupUI();
    }

    private void setupUI() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(400);

        Label headerLabel = new Label(mode == ApprovalMode.APPROVE ? "DUYỆT KHOẢN VAY" : "TỪ CHỐI KHOẢN VAY");
        headerLabel.setStyle(mode == ApprovalMode.APPROVE
            ? "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #27ae60;"
            : "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        // Loan Info Summary
        VBox infoBox = new VBox(5);
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        infoBox.getChildren().addAll(
            new Label("Mã khoản vay: " + loan.getLoanNumber()),
            new Label("Khách hàng: " + loan.getCustomerName()),
            new Label("Số tiền: " + currencyFormat.format(loan.getPrincipalAmount())),
            new Label("Kỳ hạn: " + loan.getTermMonths() + " tháng")
        );

        Label noteLabel = new Label(mode == ApprovalMode.APPROVE ? "Ghi chú (tùy chọn):" : "Lý do từ chối (bắt buộc):");
        noteArea = new TextArea();
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        confirmButton = new Button(mode == ApprovalMode.APPROVE ? "Đồng ý Duyệt" : "Xác nhận Từ chối");
        confirmButton.setStyle(mode == ApprovalMode.APPROVE
            ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;"
            : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmButton.setOnAction(e -> handleConfirm());

        Button cancelButton = new Button("Hủy bỏ");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(cancelButton, confirmButton);

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(headerLabel, infoBox, noteLabel, noteArea, messageLabel, buttonBox);
        Scene scene = new Scene(root);
        setScene(scene);
    }

    private void handleConfirm() {
        String note = noteArea.getText();
        if (mode == ApprovalMode.REJECT && (note == null || note.trim().isEmpty())) {
            messageLabel.setText("Vui lòng nhập lý do từ chối.");
            return;
        }

        confirmButton.setDisable(true);
        getScene().setCursor(javafx.scene.Cursor.WAIT);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                long userId = SessionManager.getCurrentUser().getId();
                if (mode == ApprovalMode.APPROVE) {
                    loanService.approveLoan(loan.getId(), userId, note);
                } else {
                    loanService.rejectLoan(loan.getId(), userId, note);
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            success = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText(mode == ApprovalMode.APPROVE
                ? "Đã duyệt khoản vay thành công!"
                : "Đã từ chối khoản vay.");
            alert.showAndWait();
            close();
        });

        task.setOnFailed(e -> {
            confirmButton.setDisable(false);
            getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            messageLabel.setText("Lỗi: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    public boolean isSuccess() {
        return success;
    }
}
