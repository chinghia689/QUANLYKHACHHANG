package com.customer.controller;

import com.customer.model.Customer;
import com.customer.model.CustomerType;
import com.customer.service.CustomerService;
import com.customer.ui.CustomerDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class MainController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<CustomerType> filterComboBox;
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, String> nameColumn;
    @FXML
    private TableColumn<Customer, String> phoneColumn;
    @FXML
    private TableColumn<Customer, String> emailColumn;
    @FXML
    private TableColumn<Customer, String> addressColumn;
    @FXML
    private TableColumn<Customer, CustomerType> typeColumn;
    @FXML
    private TableColumn<Customer, LocalDate> dobColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button refreshButton;

    private final CustomerService customerService;
    private final ObservableList<Customer> customerList;

    public MainController() {
        this.customerService = new CustomerService();
        this.customerList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("customerType"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));

        // Set custom cell factory for customer type to show display name
        typeColumn.setCellFactory(column -> new TableCell<Customer, CustomerType>() {
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

        customerTable.setItems(customerList);

        // Initialize filter combo box
        filterComboBox.getItems().add(null); // "All" option
        filterComboBox.getItems().addAll(CustomerType.values());
        filterComboBox.setPromptText("All");

        // Custom button cell for combo box
        filterComboBox.setButtonCell(new ListCell<CustomerType>() {
            @Override
            protected void updateItem(CustomerType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("All");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Custom cell factory for dropdown items
        filterComboBox.setCellFactory(lv -> new ListCell<CustomerType>() {
            @Override
            protected void updateItem(CustomerType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("All");
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        // Add listeners
        searchField.textProperty().addListener((observable, oldValue, newValue) -> performSearch());
        filterComboBox.setOnAction(e -> performFilter());

        // Load initial data
        loadCustomers();
    }

    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(customerService.getAllCustomers());
        } catch (Exception e) {
            showError("Load Error", "Cannot load customer list: " + e.getMessage());
        }
    }

    private void performSearch() {
        String keyword = searchField.getText();
        try {
            customerList.clear();
            customerList.addAll(customerService.searchCustomers(keyword));
        } catch (Exception e) {
            showError("Search Error", "Cannot search customers: " + e.getMessage());
        }
    }

    private void performFilter() {
        CustomerType selectedType = filterComboBox.getValue();
        try {
            customerList.clear();
            if (selectedType == null) {
                customerList.addAll(customerService.getAllCustomers());
            } else {
                customerList.addAll(customerService.getCustomersByType(selectedType));
            }
        } catch (Exception e) {
            showError("Filter Error", "Cannot filter customers: " + e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        Customer newCustomer = new Customer();
        newCustomer.setCreatedDate(LocalDateTime.now());

        CustomerDialog dialog = new CustomerDialog(newCustomer, false);
        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(customer -> {
            try {
                customerService.addCustomer(customer);
                loadCustomers();
                showSuccess("Success", "New customer added!");
            } catch (CustomerService.ValidationException e) {
                showError("Validation Error", e.getMessage());
            } catch (Exception e) {
                showError("Error", "Cannot add customer: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a customer to edit!");
            return;
        }

        CustomerDialog dialog = new CustomerDialog(selected, true);
        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(customer -> {
            try {
                customerService.updateCustomer(customer);
                loadCustomers();
                showSuccess("Success", "Customer updated!");
            } catch (CustomerService.ValidationException e) {
                showError("Validation Error", e.getMessage());
            } catch (Exception e) {
                showError("Error", "Cannot update customer: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a customer to delete!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Are you sure you want to delete this customer?");
        confirmDialog.setContentText("Name: " + selected.getFullName() + "\nPhone: " + selected.getPhone());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteCustomer(selected.getId());
                loadCustomers();
                showSuccess("Success", "Customer deleted!");
            } catch (Exception e) {
                showError("Error", "Cannot delete customer: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filterComboBox.setValue(null);
        loadCustomers();
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
