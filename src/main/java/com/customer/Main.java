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
            // Load FXML - load login-view.fxml first
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 400, 500);

            // Initialize ThemeManager and apply theme
            ThemeManager themeManager = ThemeManager.getInstance();
            themeManager.setScene(scene);
            themeManager.applyTheme();

            // Configure stage
            primaryStage.setTitle("Login - Customer Management");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

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
