package com.customer.controller;

import com.customer.util.AnimationHelper;
import com.customer.util.ThemeManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

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
    private Button themeToggleBtn;

    @FXML
    private StackPane contentArea;

    private HBox currentSelectedMenu;
    private Node currentContent;

    @FXML
    public void initialize() {
        currentSelectedMenu = menuDashboard;

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
            loadView("/views/customer-view.fxml");
        }
    }

    @FXML
    private void handleThemeToggle() {
        ThemeManager.getInstance().toggleTheme();
        updateThemeButtonText();
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
            AnimationHelper.transitionContent(currentContent, newContent, () -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(newContent);
            });

            currentContent = newContent;

        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
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
