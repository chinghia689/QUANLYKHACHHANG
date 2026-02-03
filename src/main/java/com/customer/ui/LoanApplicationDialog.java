package com.customer.ui;

import com.customer.model.Customer;
import com.customer.service.CustomerService;
import com.customer.service.LoanService;
import com.customer.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LoanApplicationDialog extends Stage {
    private final LoanService loanService;
    private final CustomerService customerService;
    private boolean success = false;

    private ComboBox<Customer> customerComboBox;
    private TextField amountField;
    private ComboBox<Integer> termComboBox;
    private TextArea purposeArea;
    private Label monthlyPaymentLabel;
    private Label totalAmountLabel;
    private Label messageLabel;
    private Button confirmButton;
    private Button cancelButton;

    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public LoanApplicationDialog() {
        this.loanService = new LoanService();
        this.customerService = new CustomerService();

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Tạo Đơn Vay Mới");
        setupUI();
        loadCustomers();
    }

    private void setupUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");
        root.setPrefWidth(500);

        Label headerLabel = new Label("TẠO ĐƠN VAY MỚI");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER_LEFT);

        // Customer
        grid.add(new Label("Khách hàng:"), 0, 0);
        customerComboBox = new ComboBox<>();
        customerComboBox.setPromptText("Chọn khách hàng...");
        customerComboBox.setPrefWidth(300);
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getFullName() + " - " + customer.getPhone();
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
        grid.add(customerComboBox, 1, 0);

        // Amount
        grid.add(new Label("Số tiền vay (VND):"), 0, 1);
        amountField = new TextField();
        amountField.setPromptText("10,000,000 - 1,000,000,000");
        grid.add(amountField, 1, 1);

        // Term
        grid.add(new Label("Kỳ hạn (tháng):"), 0, 2);
        termComboBox = new ComboBox<>(FXCollections.observableArrayList(6, 12, 18, 24, 36, 48, 60));
        termComboBox.setValue(12);
        grid.add(termComboBox, 1, 2);

        // Interest Rate
        grid.add(new Label("Lãi suất:"), 0, 3);
        Label interestRateLabel = new Label("12.00% / năm (Cố định)");
        interestRateLabel.setStyle("-fx-font-weight: bold;");
        grid.add(interestRateLabel, 1, 3);

        // Monthly Payment
        grid.add(new Label("Trả hàng tháng:"), 0, 4);
        monthlyPaymentLabel = new Label("0 ₫");
        monthlyPaymentLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");
        grid.add(monthlyPaymentLabel, 1, 4);

        // Total Amount
        grid.add(new Label("Tổng tiền trả:"), 0, 5);
        totalAmountLabel = new Label("0 ₫");
        totalAmountLabel.setStyle("-fx-font-weight: bold;");
        grid.add(totalAmountLabel, 1, 5);

        // Purpose
        grid.add(new Label("Mục đích vay:"), 0, 6);
        purposeArea = new TextArea();
        purposeArea.setPrefRowCount(3);
        purposeArea.setWrapText(true);
        grid.add(purposeArea, 1, 6);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        confirmButton = new Button("Xác nhận");
        confirmButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmButton.setOnAction(e -> handleConfirm());

        cancelButton = new Button("Hủy");
        cancelButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        cancelButton.setOnAction(e -> close());

        buttonBox.getChildren().addAll(cancelButton, confirmButton);

        messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: red;");

        root.getChildren().addAll(headerLabel, grid, messageLabel, buttonBox);
        Scene scene = new Scene(root);
        setScene(scene);

        // Add listeners for calculation
        amountField.textProperty().addListener((obs, oldVal, newVal) -> calculatePayment());
        termComboBox.valueProperty().addListener((obs, oldVal, newVal) -> calculatePayment());
    }

    private void loadCustomers() {
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() throws Exception {
                return customerService.getAllCustomers();
            }
        };

        task.setOnSucceeded(e -> {
            customerComboBox.setItems(FXCollections.observableArrayList(task.getValue()));
        });

        task.setOnFailed(e -> {
            messageLabel.setText("Lỗi khi tải danh sách khách hàng: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private void calculatePayment() {
        try {
            String amountText = amountField.getText().replaceAll("[^0-9]", "");
            if (amountText.isEmpty()) {
                monthlyPaymentLabel.setText("0 ₫");
                totalAmountLabel.setText("0 ₫");
                return;
            }

            BigDecimal amount = new BigDecimal(amountText);
            Integer term = termComboBox.getValue();

            if (term != null) {
                BigDecimal monthlyPayment = loanService.calculateMonthlyPayment(amount, LoanService.INTEREST_RATE, term);
                BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term));

                monthlyPaymentLabel.setText(currencyFormat.format(monthlyPayment));
                totalAmountLabel.setText(currencyFormat.format(totalAmount));
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
    }

    private void handleConfirm() {
        messageLabel.setText("");
        Customer customer = customerComboBox.getValue();
        if (customer == null) {
            messageLabel.setText("Vui lòng chọn khách hàng.");
            return;
        }

        String amountText = amountField.getText().replaceAll("[^0-9]", "");
        if (amountText.isEmpty()) {
            messageLabel.setText("Vui lòng nhập số tiền vay.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException e) {
            messageLabel.setText("Số tiền không hợp lệ.");
            return;
        }

        Integer term = termComboBox.getValue();
        String purpose = purposeArea.getText();

        confirmButton.setDisable(true);
        getScene().setCursor(javafx.scene.Cursor.WAIT);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                loanService.applyLoan(
                    customer.getId(),
                    amount,
                    term,
                    purpose,
                    SessionManager.getCurrentUser().getId()
                );
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            success = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText(null);
            alert.setContentText("Đã tạo đơn vay thành công!");
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
