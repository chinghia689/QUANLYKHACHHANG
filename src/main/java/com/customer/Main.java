package com.customer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main-view.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 1200, 700);

            // Load CSS
            String css = getClass().getResource("/styles/main.css").toExternalForm();
            scene.getStylesheets().add(css);

            // Configure stage
            primaryStage.setTitle("Quản Lý Khách Hàng - Customer Management");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);

            // Show stage
            primaryStage.show();

            System.out.println("🚀 Application started successfully!");

        } catch (IOException e) {
            System.err.println("❌ Failed to load application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("👋 Application closing...");
        com.customer.dao.DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
