package com.customer.ui;

import com.customer.model.Loan;
import com.customer.service.LoanService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class LoanDetailDialog extends Stage {
    private final Loan loan;
    private final LoanService loanService;
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public LoanDetailDialog(Loan loan) {
        this.loan = loan;
        this.loanService = new LoanService();

        initModality(Modality.APPLICATION_MODAL);
        setTitle("Chi Tiết Khoản Vay - " + loan.getLoanNumber());
        setWidth(900);
        setHeight(650);

        setupUI();
    }

    private void setupUI() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");

        // Top: Loan Summary
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(0, 0, 15, 0));

        Label headerLabel = new Label("THÔNG TIN KHOẢN VAY");
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 5;");

        addDetailRow(grid, 0, "Mã khoản vay:", loan.getLoanNumber(), "Khách hàng:", loan.getCustomerName());
        addDetailRow(grid, 1, "Số tiền vay:", currencyFormat.format(loan.getPrincipalAmount()), "Lãi suất:", loan.getInterestRate() + "% / năm");
        addDetailRow(grid, 2, "Kỳ hạn:", loan.getTermMonths() + " tháng", "Trả hàng tháng:", currencyFormat.format(loan.getMonthlyPayment()));
        addDetailRow(grid, 3, "Trạng thái:", loan.getStatus().getDisplayName(), "Ngày nộp đơn:", loan.getAppliedDate().format(dateTimeFormatter));

        String approvedInfo = loan.getApprovedDate() != null
            ? loan.getApprovedDate().format(dateTimeFormatter) + " bởi " + (loan.getApproverName() != null ? loan.getApproverName() : "N/A")
            : "Chưa duyệt";
        addDetailRow(grid, 4, "Ngày duyệt:", approvedInfo, "Ghi chú:", loan.getApprovalNote() != null ? loan.getApprovalNote() : "");

        String startDateStr = loan.getStartDate() != null ? loan.getStartDate().format(dateFormatter) : "Chưa giải ngân";
        String endDateStr = loan.getEndDate() != null ? loan.getEndDate().format(dateFormatter) : "---";
        addDetailRow(grid, 5, "Ngày bắt đầu:", startDateStr, "Ngày đáo hạn:", endDateStr);

        topBox.getChildren().addAll(headerLabel, grid);
        root.setTop(topBox);

        // Center: Amortization Schedule
        VBox centerBox = new VBox(10);

        HBox tableHeader = new HBox(10);
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        Label scheduleLabel = new Label("LỊCH TRẢ NỢ (DỰ KIẾN)");
        scheduleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        if (loan.getStartDate() == null) {
            Label warningLabel = new Label("(Chưa giải ngân - ngày thanh toán mang tính tham khảo)");
            warningLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-style: italic;");
            tableHeader.getChildren().addAll(scheduleLabel, warningLabel);
        } else {
            tableHeader.getChildren().add(scheduleLabel);
        }

        TableView<LoanService.AmortizationEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LoanService.AmortizationEntry, Integer> noCol = new TableColumn<>("Kỳ");
        noCol.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getPaymentNumber()));
        noCol.setPrefWidth(50);

        TableColumn<LoanService.AmortizationEntry, String> dateCol = new TableColumn<>("Ngày đến hạn");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDueDate().format(dateFormatter)));

        TableColumn<LoanService.AmortizationEntry, String> principalCol = new TableColumn<>("Gốc");
        principalCol.setCellValueFactory(data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getPrincipalPortion())));
        principalCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<LoanService.AmortizationEntry, String> interestCol = new TableColumn<>("Lãi");
        interestCol.setCellValueFactory(data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getInterestPortion())));
        interestCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<LoanService.AmortizationEntry, String> totalCol = new TableColumn<>("Tổng cộng");
        totalCol.setCellValueFactory(data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getTotalPayment())));
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        TableColumn<LoanService.AmortizationEntry, String> balanceCol = new TableColumn<>("Dư nợ còn lại");
        balanceCol.setCellValueFactory(data -> new SimpleStringProperty(currencyFormat.format(data.getValue().getRemainingBalance())));
        balanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(noCol, dateCol, principalCol, interestCol, totalCol, balanceCol);

        // Load data
        List<LoanService.AmortizationEntry> schedule = loanService.generateAmortizationSchedule(loan);
        table.setItems(FXCollections.observableArrayList(schedule));

        centerBox.getChildren().addAll(tableHeader, table);
        root.setCenter(centerBox);

        // Bottom: Close Button
        HBox bottomBox = new HBox();
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        Button closeButton = new Button("Đóng");
        closeButton.setPrefWidth(100);
        closeButton.setOnAction(e -> close());

        bottomBox.getChildren().add(closeButton);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private void addDetailRow(GridPane grid, int row, String label1, String value1, String label2, String value2) {
        Label l1 = new Label(label1);
        l1.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Label v1 = new Label(value1);
        v1.setWrapText(true);

        Label l2 = new Label(label2);
        l2.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        Label v2 = new Label(value2);
        v2.setWrapText(true);

        grid.add(l1, 0, row);
        grid.add(v1, 1, row);
        grid.add(l2, 2, row);
        grid.add(v2, 3, row);
    }
}
