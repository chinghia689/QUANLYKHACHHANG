package com.customer;

import com.customer.util.ThemeManager;
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
            // Load FXML - now loading app-view.fxml with sidebar layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/app-view.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 1200, 700);

            // Initialize ThemeManager and apply theme
            ThemeManager themeManager = ThemeManager.getInstance();
            themeManager.setScene(scene);
            themeManager.applyTheme();

            // Configure stage
            primaryStage.setTitle("Quản Lý Khách Hàng - Customer Management");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);

            // Show stage
            primaryStage.show();

            System.out.println("Application started successfully!");

        } catch (IOException e) {
            System.err.println("Failed to load application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("Application closing...");
        com.customer.dao.DatabaseManager.getInstance().close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
