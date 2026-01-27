package com.customer.controller;

import com.customer.dao.CustomerDAO;
import com.customer.model.CustomerType;
import com.customer.util.AnimationHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Controller for the Dashboard view with statistics and charts.
 */
public class DashboardController {

    @FXML
    private Button refreshBtn;

    @FXML
    private HBox statsContainer;

    @FXML
    private VBox cardTotal;

    @FXML
    private VBox cardVip;

    @FXML
    private VBox cardNew;

    @FXML
    private Label lblTotalCount;

    @FXML
    private Label lblVipCount;

    @FXML
    private Label lblNewCount;

    @FXML
    private PieChart typeDistributionChart;

    @FXML
    private Label lblVipPercent;

    @FXML
    private Label lblVipDetail;

    @FXML
    private Label lblRegularPercent;

    @FXML
    private Label lblRegularDetail;

    @FXML
    private Label lblPotentialPercent;

    @FXML
    private Label lblPotentialDetail;

    @FXML
    private StackPane loadingOverlay;

    private final CustomerDAO customerDAO;

    public DashboardController() {
        this.customerDAO = new CustomerDAO();
    }

    @FXML
    public void initialize() {
        // Add hover effects to stat cards
        AnimationHelper.addScaleOnHover(cardTotal);
        AnimationHelper.addScaleOnHover(cardVip);
        AnimationHelper.addScaleOnHover(cardNew);

        // Animate cards on load
        AnimationHelper.slideIn(cardTotal, AnimationHelper.Direction.BOTTOM);
        AnimationHelper.slideIn(cardVip, AnimationHelper.Direction.BOTTOM);
        AnimationHelper.slideIn(cardNew, AnimationHelper.Direction.BOTTOM);

        // Load statistics
        loadStatistics();
    }

    @FXML
    private void handleRefresh() {
        AnimationHelper.pulse(refreshBtn);
        loadStatistics();
    }

    private void loadStatistics() {
        showLoading(true);

        Task<Void> loadTask = new Task<>() {
            private int total = 0;
            private int vipCount = 0;
            private int regularCount = 0;
            private int potentialCount = 0;
            private int newThisMonth = 0;

            @Override
            protected Void call() throws Exception {
                // Get statistics from DAO
                total = customerDAO.countAll();
                Map<CustomerType, Integer> distribution = customerDAO.getTypeDistribution();

                vipCount = distribution.getOrDefault(CustomerType.VIP, 0);
                regularCount = distribution.getOrDefault(CustomerType.REGULAR, 0);
                potentialCount = distribution.getOrDefault(CustomerType.POTENTIAL, 0);
                newThisMonth = customerDAO.countNewThisMonth();

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    // Update labels
                    lblTotalCount.setText(String.valueOf(total));
                    lblVipCount.setText(String.valueOf(vipCount));
                    lblNewCount.setText(String.valueOf(newThisMonth));

                    // Update detail labels
                    updateDetailLabels(total, vipCount, regularCount, potentialCount);

                    // Update pie chart
                    updatePieChart(vipCount, regularCount, potentialCount);

                    showLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    System.err.println("Failed to load statistics: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void updateDetailLabels(int total, int vip, int regular, int potential) {
        if (total == 0) {
            lblVipPercent.setText("0%");
            lblVipDetail.setText("0 khách");
            lblRegularPercent.setText("0%");
            lblRegularDetail.setText("0 khách");
            lblPotentialPercent.setText("0%");
            lblPotentialDetail.setText("0 khách");
            return;
        }

        double vipPercent = (vip * 100.0) / total;
        double regularPercent = (regular * 100.0) / total;
        double potentialPercent = (potential * 100.0) / total;

        lblVipPercent.setText(String.format("%.1f%%", vipPercent));
        lblVipDetail.setText(vip + " khách");

        lblRegularPercent.setText(String.format("%.1f%%", regularPercent));
        lblRegularDetail.setText(regular + " khách");

        lblPotentialPercent.setText(String.format("%.1f%%", potentialPercent));
        lblPotentialDetail.setText(potential + " khách");
    }

    private void updatePieChart(int vip, int regular, int potential) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        if (vip > 0) {
            pieData.add(new PieChart.Data("VIP (" + vip + ")", vip));
        }
        if (regular > 0) {
            pieData.add(new PieChart.Data("Thường (" + regular + ")", regular));
        }
        if (potential > 0) {
            pieData.add(new PieChart.Data("Tiềm năng (" + potential + ")", potential));
        }

        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Chưa có dữ liệu", 1));
        }

        typeDistributionChart.setData(pieData);
        typeDistributionChart.setLabelsVisible(true);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
    }
}
