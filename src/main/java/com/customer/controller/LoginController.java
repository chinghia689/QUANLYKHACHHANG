package com.customer.controller;

import com.customer.service.AuthService;
import com.customer.util.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    private final AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
        // Focus username field by default
        Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Disable button to prevent double submit
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        try {
            AuthService.LoginResult result = authService.login(username, password);

            if (result.isSuccess()) {
                // Login successful, switch to main view
                loadMainView();
            } else {
                // Login failed
                errorLabel.setText(result.getMessage());
                errorLabel.setVisible(true);
                loginButton.setDisable(false);
            }
        } catch (Exception e) {
            errorLabel.setText("System error: " + e.getMessage());
            errorLabel.setVisible(true);
            loginButton.setDisable(false);
            e.printStackTrace();
        }
    }

    private void loadMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/app-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 700);

            // Re-apply theme
            ThemeManager.getInstance().setScene(scene);
            ThemeManager.getInstance().applyTheme();

            stage.setScene(scene);
            stage.centerOnScreen();

            // Update title to include username
            stage.setTitle("Customer Management - " + com.customer.util.SessionManager.getCurrentUser().getFullName());

        } catch (IOException e) {
            errorLabel.setText("Error loading main view: " + e.getMessage());
            errorLabel.setVisible(true);
            loginButton.setDisable(false);
            e.printStackTrace();
        }
    }
}
