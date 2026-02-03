package com.customer.ui;

import com.customer.model.Customer;
import com.customer.model.CustomerType;
import com.customer.util.ThemeManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class CustomerDialog extends Dialog<Customer> {
    private final TextField nameField;
    private final TextField phoneField;
    private final TextField emailField;
    private final TextArea addressArea;
    private final DatePicker dobPicker;
    private final ComboBox<CustomerType> typeComboBox;
    private final Customer customer;

    public CustomerDialog(Customer customer, boolean isEdit) {
        this.customer = customer;

        setTitle(isEdit ? "Edit Customer" : "Add New Customer");
        setHeaderText(isEdit ? "Update customer information" : "Enter new customer information");

        // Create form fields
        nameField = new TextField(customer.getFullName());
        nameField.setPromptText("Enter full name");

        phoneField = new TextField(customer.getPhone());
        phoneField.setPromptText("Enter phone number");

        emailField = new TextField(customer.getEmail());
        emailField.setPromptText("Enter email");

        addressArea = new TextArea(customer.getAddress());
        addressArea.setPromptText("Enter address");
        addressArea.setPrefRowCount(3);

        dobPicker = new DatePicker(customer.getDateOfBirth());
        dobPicker.setPromptText("Select birthday");

        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(CustomerType.values());
        typeComboBox.setValue(customer.getCustomerType() != null ? customer.getCustomerType() : CustomerType.REGULAR);

        // Custom cell factory for type combo box
        typeComboBox.setButtonCell(new ListCell<CustomerType>() {
            @Override
            protected void updateItem(CustomerType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        typeComboBox.setCellFactory(lv -> new ListCell<CustomerType>() {
            @Override
            protected void updateItem(CustomerType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Create grid layout
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // Add labels and fields
        grid.add(new Label("Full Name: *"), 0, 0);
        grid.add(nameField, 1, 0);

        grid.add(new Label("Phone Number:"), 0, 1);
        grid.add(phoneField, 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        grid.add(new Label("Address:"), 0, 3);
        grid.add(addressArea, 1, 3);

        grid.add(new Label("Birthday:"), 0, 4);
        grid.add(dobPicker, 1, 4);

        grid.add(new Label("Customer Type: *"), 0, 5);
        grid.add(typeComboBox, 1, 5);

        // Set column constraints for better layout
        nameField.setPrefWidth(300);
        phoneField.setPrefWidth(300);
        emailField.setPrefWidth(300);
        addressArea.setPrefWidth(300);
        dobPicker.setPrefWidth(300);
        typeComboBox.setPrefWidth(300);

        // Create container
        VBox content = new VBox(10);
        content.getChildren().add(grid);

        getDialogPane().setContent(content);

        // Add buttons
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // Style buttons
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.getStyleClass().add("success-button");

        Button cancelButton = (Button) getDialogPane().lookupButton(cancelButtonType);
        cancelButton.getStyleClass().add("secondary-button");

        // Convert result to Customer when Save is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                customer.setFullName(nameField.getText());
                customer.setPhone(phoneField.getText());
                customer.setEmail(emailField.getText());
                customer.setAddress(addressArea.getText());
                customer.setDateOfBirth(dobPicker.getValue());
                customer.setCustomerType(typeComboBox.getValue());
                return customer;
            }
            return null;
        });

        // Request focus on name field
        nameField.requestFocus();

        // Apply current theme to dialog
        applyTheme();
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
