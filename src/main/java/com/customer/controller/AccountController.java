package com.customer.controller;

import com.customer.model.Account;
import com.customer.model.AccountStatus;
import com.customer.model.AccountType;
import com.customer.model.Customer;
import com.customer.service.AccountService;
import com.customer.service.CustomerService;
import com.customer.ui.AccountDialog;
import com.customer.util.AnimationHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AccountController {

    @FXML private TextField searchField;
    @FXML private ComboBox<AccountType> typeFilter;
    @FXML private ComboBox<AccountStatus> statusFilter;
    @FXML private TableView<Account> accountTable;
    @FXML private TableColumn<Account, String> accountNumberColumn;
    @FXML private TableColumn<Account, String> customerNameColumn;
    @FXML private TableColumn<Account, AccountType> typeColumn;
    @FXML private TableColumn<Account, BigDecimal> balanceColumn;
    @FXML private TableColumn<Account, Double> interestRateColumn;
    @FXML private TableColumn<Account, AccountStatus> statusColumn;
    @FXML private TableColumn<Account, LocalDateTime> createdDateColumn;
    @FXML private Button openAccountButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button freezeUnfreezeButton;
    @FXML private Button closeAccountButton;
    @FXML private Button refreshButton;
    @FXML private StackPane loadingOverlay;

    private final AccountService accountService;
    private final CustomerService customerService;
    private final ObservableList<Account> accountList;

    public AccountController() {
        this.accountService = new AccountService();
        this.customerService = new CustomerService();
        this.accountList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Setup Columns
        accountNumberColumn.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        balanceColumn.setCellValueFactory(new PropertyValueFactory<>("balance"));
        interestRateColumn.setCellValueFactory(new PropertyValueFactory<>("interestRate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdDate"));

        // Format Balance
        balanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item));
                }
            }
        });

        // Format Interest Rate
        interestRateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f %%", item));
                }
            }
        });

        // Format Status
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(AccountStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case ACTIVE: setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;"); break;
                        case FROZEN: setStyle("-fx-text-fill: #F59E0B; -fx-font-weight: bold;"); break;
                        case CLOSED: setStyle("-fx-text-fill: #EF4444; -fx-font-weight: bold;"); break;
                    }
                }
            }
        });

        // Format Date
        createdDateColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        accountTable.setItems(accountList);

        // Filters
        typeFilter.getItems().add(null);
        typeFilter.getItems().addAll(AccountType.values());
        typeFilter.setPromptText("All Types");

        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(AccountStatus.values());
        statusFilter.setPromptText("All Status");

        // Listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadAccounts());
        typeFilter.setOnAction(e -> loadAccounts());
        statusFilter.setOnAction(e -> loadAccounts());

        // Buttons
        AnimationHelper.addScaleOnHover(openAccountButton);
        AnimationHelper.addScaleOnHover(viewDetailsButton);
        AnimationHelper.addScaleOnHover(freezeUnfreezeButton);
        AnimationHelper.addScaleOnHover(closeAccountButton);

        // Initial Load
        loadAccounts();
    }

    private void loadAccounts() {
        String keyword = searchField.getText();
        AccountType type = typeFilter.getValue();
        AccountStatus status = statusFilter.getValue();

        showLoading(true);

        Task<java.util.List<Account>> task = new Task<>() {
            @Override
            protected java.util.List<Account> call() throws Exception {
                return accountService.searchAccounts(keyword, type, status);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    accountList.clear();
                    accountList.addAll(getValue());
                    showLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Error", "Failed to load accounts: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };

        new Thread(task).start();
    }

    @FXML
    private void handleOpenAccount() {
        if (!accountService.canOpenAccount()) {
            showError("Permission Denied", "You do not have permission to open accounts.");
            return;
        }

        // Load customers first for dropdown
        Task<java.util.List<Customer>> loadCustomersTask = new Task<>() {
            @Override
            protected java.util.List<Customer> call() throws Exception {
                return customerService.getAllCustomers();
            }

            @Override
            protected void succeeded() {
                Account newAccount = new Account();
                AccountDialog dialog = new AccountDialog(newAccount, getValue(), false);
                Optional<Account> result = dialog.showAndWait();

                result.ifPresent(account -> {
                    try {
                        accountService.openAccount(account);
                        loadAccounts();
                        showSuccess("Success", "Account " + account.getAccountNumber() + " opened successfully.");
                    } catch (Exception e) {
                        showError("Error", e.getMessage());
                    }
                });
            }

            @Override
            protected void failed() {
                showError("Error", "Failed to load customers.");
            }
        };
        new Thread(loadCustomersTask).start();
    }

    @FXML
    private void handleViewDetails() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an account.");
            return;
        }

        // We need customer list even for view mode to show correct customer in dropdown (though disabled)
        // Or we can just pass null if view mode handles it gracefully?
        // Let's pass null for now as we set transient name anyway but dialog expects list for combo
        // Actually dialog uses combo to display customer. If we pass null, combo is empty.
        // Let's just fetch customers. It's fast enough or we can optimize later.

        Task<java.util.List<Customer>> loadCustomersTask = new Task<>() {
             @Override
             protected java.util.List<Customer> call() throws Exception {
                 return customerService.getAllCustomers();
             }

             @Override
             protected void succeeded() {
                 AccountDialog dialog = new AccountDialog(selected, getValue(), true);
                 dialog.showAndWait();
             }
        };
        new Thread(loadCustomersTask).start();
    }

    @FXML
    private void handleFreezeUnfreeze() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an account.");
            return;
        }

        try {
            if (selected.getStatus() == AccountStatus.ACTIVE) {
                if (!accountService.canFreezeAccount()) {
                    showError("Permission Denied", "You cannot freeze accounts.");
                    return;
                }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Freeze");
                confirm.setHeaderText("Freeze account " + selected.getAccountNumber() + "?");
                confirm.setContentText("The customer will not be able to transact.");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    accountService.freezeAccount(selected);
                    loadAccounts();
                    showSuccess("Success", "Account frozen.");
                }

            } else if (selected.getStatus() == AccountStatus.FROZEN) {
                if (!accountService.canUnfreezeAccount()) {
                    showError("Permission Denied", "Only Managers/Admins can unfreeze accounts.");
                    return;
                }
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Unfreeze");
                confirm.setHeaderText("Unfreeze account " + selected.getAccountNumber() + "?");

                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    accountService.unfreezeAccount(selected);
                    loadAccounts();
                    showSuccess("Success", "Account unfrozen.");
                }
            } else {
                showWarning("Action Invalid", "Closed accounts cannot be modified.");
            }
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void handleCloseAccount() {
        Account selected = accountTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select an account.");
            return;
        }

        if (selected.getStatus() == AccountStatus.CLOSED) {
            showWarning("Already Closed", "This account is already closed.");
            return;
        }

        if (!accountService.canCloseAccount()) {
            showError("Permission Denied", "Only Managers/Admins can close accounts.");
            return;
        }

        if (selected.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            showError("Balance Not Zero", "Account balance must be 0 to close.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Close");
        confirm.setHeaderText("Permanently close account " + selected.getAccountNumber() + "?");
        confirm.setContentText("This action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                accountService.closeAccount(selected);
                loadAccounts();
                showSuccess("Success", "Account closed.");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        typeFilter.setValue(null);
        statusFilter.setValue(null);
        loadAccounts();
    }

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);
        }
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
}
