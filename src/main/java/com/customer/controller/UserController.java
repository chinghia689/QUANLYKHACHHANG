package com.customer.controller;

import com.customer.model.Role;
import com.customer.model.User;
import com.customer.model.UserStatus;
import com.customer.service.UserService;
import com.customer.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class UserController {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Long> idColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> fullNameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, String> lastLoginColumn;

    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button lockButton;
    @FXML
    private Button unlockButton;
    @FXML
    private Button resetPasswordButton;

    private final UserService userService;
    private final ObservableList<User> userList;

    public UserController() {
        this.userService = new UserService();
        this.userList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        setupPermissions();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        roleColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getRole().getDisplayName()));

        statusColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));

        lastLoginColumn.setCellValueFactory(cellData -> {
            LocalDateTime lastLogin = cellData.getValue().getLastLogin();
            if (lastLogin != null) {
                return new SimpleStringProperty(lastLogin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        userTable.setItems(userList);
    }

    private void setupPermissions() {
        boolean isAdmin = SessionManager.isAdmin();
        addButton.setVisible(isAdmin);
        editButton.setVisible(isAdmin);
        lockButton.setVisible(isAdmin);
        unlockButton.setVisible(isAdmin);
        resetPasswordButton.setVisible(isAdmin);
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(userService.getAllUsers());
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
    }

    @FXML
    private void handleAdd() {
        Dialog<Pair<User, String>> dialog = createUserDialog(null);
        Optional<Pair<User, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            try {
                userService.createUser(pair.getKey(), pair.getValue());
                loadUsers();
                showSuccess("Success", "New user added");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No user selected", "Please select a user to edit");
            return;
        }

        Dialog<Pair<User, String>> dialog = createUserDialog(selected);
        Optional<Pair<User, String>> result = dialog.showAndWait();

        result.ifPresent(pair -> {
            try {
                userService.updateUser(pair.getKey());
                loadUsers();
                showSuccess("Success", "User information updated");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleLock() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No user selected", "Please select a user to lock");
            return;
        }

        if (selected.getId().equals(SessionManager.getCurrentUser().getId())) {
            showError("Error", "Cannot lock yourself");
            return;
        }

        if (showConfirm("Confirm Lock", "Are you sure you want to lock this user?")) {
            userService.lockUser(selected.getId());
            loadUsers();
            showSuccess("Success", "User locked");
        }
    }

    @FXML
    private void handleUnlock() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No user selected", "Please select a user to unlock");
            return;
        }

        if (showConfirm("Confirm Unlock", "Are you sure you want to unlock this user?")) {
            userService.unlockUser(selected.getId());
            loadUsers();
            showSuccess("Success", "User unlocked");
        }
    }

    @FXML
    private void handleResetPassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No user selected", "Please select a user to reset password");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Enter new password for user: " + selected.getUsername());
        dialog.setContentText("New password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            try {
                userService.resetPassword(selected.getId(), password);
                showSuccess("Success", "Password reset successfully");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        });
    }

    private Dialog<Pair<User, String>> createUserDialog(User user) {
        Dialog<Pair<User, String>> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add User" : "Edit User");
        dialog.setHeaderText(null);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Username");

        TextField fullName = new TextField();
        fullName.setPromptText("Full Name");

        TextField email = new TextField();
        email.setPromptText("Email");

        ComboBox<Role> role = new ComboBox<>();
        role.getItems().addAll(Role.values());

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        if (user != null) {
            username.setText(user.getUsername());
            // Disable username editing
            username.setDisable(true);

            fullName.setText(user.getFullName());
            email.setText(user.getEmail());
            role.setValue(user.getRole());

            // Hide password field when editing
            password.setVisible(false);
        } else {
            role.setValue(Role.STAFF);
        }

        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullName, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(email, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(role, 1, 3);

        if (user == null) {
            grid.add(new Label("Password:"), 0, 4);
            grid.add(password, 1, 4);
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User resultUser = (user != null) ? user : new User();
                resultUser.setUsername(username.getText());
                resultUser.setFullName(fullName.getText());
                resultUser.setEmail(email.getText());
                resultUser.setRole(role.getValue());

                return new Pair<>(resultUser, password.getText());
            }
            return null;
        });

        return dialog;
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}
