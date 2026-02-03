package com.customer.controller;

import com.customer.service.AuthService;
import com.customer.util.AnimationHelper;
import com.customer.util.SessionManager;
import com.customer.util.ThemeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the main application layout with sidebar navigation.
 */
public class AppController {

    @FXML
    private VBox sidebar;

    @FXML
    private HBox menuDashboard;

    @FXML
    private HBox menuCustomers;

    @FXML
    private HBox menuAccounts;

    @FXML
    private HBox menuUsers;

    @FXML
    private Button themeToggleBtn;

    @FXML
    private StackPane contentArea;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    private HBox currentSelectedMenu;
    private Node currentContent;
    private final AuthService authService;

    public AppController() {
        this.authService = new AuthService();
    }

    @FXML
    public void initialize() {
        currentSelectedMenu = menuDashboard;

        // Show user info
        if (SessionManager.isLoggedIn()) {
            userNameLabel.setText(SessionManager.getCurrentUser().getFullName());
            userRoleLabel.setText(SessionManager.getCurrentUser().getRole().getDisplayName());

            // Show Users menu only for Admin
            if (SessionManager.isAdmin()) {
                menuUsers.setVisible(true);
                menuUsers.setManaged(true);
            }
        }

        // Load dashboard as default view
        loadView("/views/dashboard-view.fxml");

        // Update theme button text based on current theme
        updateThemeButtonText();
    }

    @FXML
    private void handleMenuDashboard() {
        if (currentSelectedMenu != menuDashboard) {
            selectMenuItem(menuDashboard);
            loadView("/views/dashboard-view.fxml");
        }
    }

    @FXML
    private void handleMenuCustomers() {
        if (currentSelectedMenu != menuCustomers) {
            selectMenuItem(menuCustomers);
            // customer-view.fxml should map to MainController
            // We might need to rename MainController to CustomerController later for consistency
            loadView("/views/customer-view.fxml");
        }
    }

    @FXML
    private void handleMenuAccounts() {
        if (currentSelectedMenu != menuAccounts) {
            selectMenuItem(menuAccounts);
            loadView("/views/account-view.fxml");
        }
    }

    @FXML
    private void handleMenuUsers() {
        if (currentSelectedMenu != menuUsers) {
            selectMenuItem(menuUsers);
            loadView("/views/user-view.fxml");
        }
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme();
        updateThemeButtonText();
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            authService.logout();
            try {
                // Return to login screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) sidebar.getScene().getWindow();
                Scene scene = new Scene(root, 400, 500);

                ThemeManager.getInstance().setScene(scene);
                ThemeManager.getInstance().applyTheme();

                stage.setScene(scene);
                stage.setTitle("Login - Customer Management");
                stage.setMinWidth(400);
                stage.setMinHeight(500);
                stage.setResizable(false);
                stage.centerOnScreen();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleChangePassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter new password details");

        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField oldPassword = new PasswordField();
        oldPassword.setPromptText("Old Password");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("New Password");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirm New Password");

        grid.add(new Label("Old Password:"), 0, 0);
        grid.add(oldPassword, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirm:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Validation
        Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);

        oldPassword.textProperty().addListener((observable, oldValue, newValue) -> {
            changeButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                if (!newPassword.getText().equals(confirmPassword.getText())) {
                    return "ERROR: Confirmation password does not match";
                }

                AuthService.ChangePasswordResult result = authService.changePassword(
                        SessionManager.getCurrentUser().getId(),
                        oldPassword.getText(),
                        newPassword.getText()
                );

                if (result.isSuccess()) {
                    return "SUCCESS: " + result.getMessage();
                } else {
                    return "ERROR: " + result.getMessage();
                }
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(message -> {
            if (message.startsWith("SUCCESS")) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText(message.substring(9));
                alert.showAndWait();
            } else if (message.startsWith("ERROR")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(message.substring(7));
                alert.showAndWait();
            }
        });
    }

    private void selectMenuItem(HBox menuItem) {
        // Remove selected class from previous
        if (currentSelectedMenu != null) {
            currentSelectedMenu.getStyleClass().remove("selected");
        }

        // Add selected class to new item
        menuItem.getStyleClass().add("selected");
        currentSelectedMenu = menuItem;
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newContent = loader.load();

            // Animate transition
            if (contentArea.getChildren().isEmpty()) {
                contentArea.getChildren().add(newContent);
                currentContent = newContent;
            } else {
                AnimationHelper.transitionContent(currentContent, newContent, () -> {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(newContent);
                });
                currentContent = newContent;
            }

        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();

            // Show error in UI
            Label errorLabel = new Label("Error loading view: " + fxmlPath + "\n" + e.getMessage());
            contentArea.getChildren().clear();
            contentArea.getChildren().add(errorLabel);
        }
    }

    private void updateThemeButtonText() {
        if (ThemeManager.getInstance().isDarkTheme()) {
            themeToggleBtn.setText("🌙 Dark");
        } else {
            themeToggleBtn.setText("☀️ Light");
        }
    }
}
