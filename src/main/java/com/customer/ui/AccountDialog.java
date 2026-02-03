package com.customer.ui;

import com.customer.model.Account;
import com.customer.model.AccountStatus;
import com.customer.model.AccountType;
import com.customer.model.Customer;
import com.customer.service.AccountService;
import com.customer.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AccountDialog extends Dialog<Account> {
    private final ComboBox<Customer> customerComboBox;
    private final ComboBox<AccountType> typeComboBox;
    private final ComboBox<Integer> termComboBox;
    private final Label interestRateLabel;
    private final Label balanceLabel;
    private final Label statusLabel;
    private final Label accountNumberLabel;

    private final Account account;
    private final boolean isViewOnly;
    private final AccountService accountService;

    public AccountDialog(Account account, List<Customer> customers, boolean isViewOnly) {
        this.account = account;
        this.isViewOnly = isViewOnly;
        this.accountService = new AccountService();

        setTitle(isViewOnly ? "Account Details" : "Open New Account");
        setHeaderText(isViewOnly ? "Account Information" : "Enter new account details");

        // Initialize controls
        customerComboBox = new ComboBox<>();
        if (customers != null) {
            customerComboBox.getItems().addAll(customers);
        }
        customerComboBox.setPromptText("Select Customer");
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getFullName() + " (" + customer.getPhone() + ")";
            }

            @Override
            public Customer fromString(String string) {
                return null; // Not needed
            }
        });

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(AccountType.values());
        typeComboBox.setValue(AccountType.CHECKING);

        termComboBox = new ComboBox<>();
        termComboBox.getItems().addAll(3, 6, 12);
        termComboBox.setPromptText("Select Term");
        termComboBox.setDisable(true); // Disabled for Checking

        interestRateLabel = new Label("0.50 %"); // Default for Checking
        balanceLabel = new Label("0.00 VND");
        statusLabel = new Label("NEW");
        accountNumberLabel = new Label("(Auto Generated)");

        // Set values if viewing existing
        if (account.getId() != 0) {
            // We can't easily set customer combo without looping, but for view mode we display label usually
            // For now let's just use what we have
            if (isViewOnly) {
                accountNumberLabel.setText(account.getAccountNumber());
                statusLabel.setText(account.getStatus().name());
                balanceLabel.setText(String.format("%,.0f VND", account.getBalance()));
                interestRateLabel.setText(String.format("%.2f %%", account.getInterestRate()));

                typeComboBox.setValue(account.getAccountType());
                typeComboBox.setDisable(true);

                termComboBox.setValue(account.getTermMonths());
                termComboBox.setDisable(true);

                customerComboBox.setDisable(true);
                // Find and set customer
                if (customers != null) {
                    for (Customer c : customers) {
                        if (c.getId() == account.getCustomerId()) {
                            customerComboBox.setValue(c);
                            break;
                        }
                    }
                }
            }
        }

        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        int row = 0;

        if (isViewOnly) {
             grid.add(new Label("Account Number:"), 0, row);
             grid.add(accountNumberLabel, 1, row++);

             grid.add(new Label("Status:"), 0, row);
             grid.add(statusLabel, 1, row++);

             if (account.getClosedDate() != null) {
                 grid.add(new Label("Closed Date:"), 0, row);
                 grid.add(new Label(account.getClosedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))), 1, row++);
             }
        }

        grid.add(new Label("Customer: *"), 0, row);
        grid.add(customerComboBox, 1, row++);

        grid.add(new Label("Account Type: *"), 0, row);
        grid.add(typeComboBox, 1, row++);

        grid.add(new Label("Term (Months):"), 0, row);
        grid.add(termComboBox, 1, row++);

        grid.add(new Label("Interest Rate:"), 0, row);
        grid.add(interestRateLabel, 1, row++);

        if (isViewOnly) {
            grid.add(new Label("Current Balance:"), 0, row);
            grid.add(balanceLabel, 1, row++);
        }

        // Event Listeners
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateInterestRate();
            termComboBox.setDisable(newVal != AccountType.SAVINGS);
            if (newVal == AccountType.CHECKING) {
                termComboBox.setValue(null);
            }
        });

        termComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateInterestRate();
        });

        customerComboBox.setPrefWidth(300);
        typeComboBox.setPrefWidth(300);
        termComboBox.setPrefWidth(300);

        getDialogPane().setContent(grid);

        // Buttons
        if (isViewOnly) {
            ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().add(closeButtonType);
            Button closeButton = (Button) getDialogPane().lookupButton(closeButtonType);
            closeButton.getStyleClass().add("primary-button");
        } else {
            ButtonType saveButtonType = new ButtonType("Open Account", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

            Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
            saveButton.getStyleClass().add("success-button");

            // Validation
            saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (customerComboBox.getValue() == null) {
                    showAlert("Validation Error", "Please select a customer.");
                    event.consume();
                    return;
                }
                if (typeComboBox.getValue() == AccountType.SAVINGS && termComboBox.getValue() == null) {
                     showAlert("Validation Error", "Please select a term for Savings account.");
                     event.consume();
                     return;
                }
            });

            // Result Converter
            setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    account.setCustomerId(customerComboBox.getValue().getId());
                    account.setAccountType(typeComboBox.getValue());
                    account.setTermMonths(termComboBox.getValue() != null ? termComboBox.getValue() : 0);

                    // Interest rate is calculated in service or here?
                    // Let's set it here based on what's displayed
                    double rate = accountService.getInterestRateByTerm(account.getTermMonths());
                    if (account.getAccountType() == AccountType.CHECKING) {
                        rate = AccountService.RATE_DEFAULT;
                    }
                    account.setInterestRate(rate);

                    return account;
                }
                return null;
            });
        }

        applyTheme();
    }

    private void updateInterestRate() {
        if (isViewOnly) return;

        AccountType type = typeComboBox.getValue();
        if (type == AccountType.CHECKING) {
            interestRateLabel.setText(String.format("%.2f %%", AccountService.RATE_DEFAULT));
        } else {
            Integer term = termComboBox.getValue();
            if (term != null) {
                double rate = accountService.getInterestRateByTerm(term);
                interestRateLabel.setText(String.format("%.2f %%", rate));
            } else {
                interestRateLabel.setText("Select term");
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void applyTheme() {
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().clear();

        // Add common styles
        String commonCss = getClass().getResource("/styles/common.css").toExternalForm();
        dialogPane.getStylesheets().add(commonCss);

        // Add theme-specific styles
        ThemeManager themeManager = ThemeManager.getInstance();
        String themeCss;
        if (themeManager.isDarkTheme()) {
            themeCss = getClass().getResource("/styles/dark-theme.css").toExternalForm();
        } else {
            themeCss = getClass().getResource("/styles/light-theme.css").toExternalForm();
        }
        dialogPane.getStylesheets().add(themeCss);
    }
}
