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

        // Set custom cell factory for customer type to show Vietnamese display name
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
        filterComboBox.setPromptText("Tất cả");

        // Custom button cell for combo box
        filterComboBox.setButtonCell(new ListCell<CustomerType>() {
            @Override
            protected void updateItem(CustomerType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tất cả");
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
                    setText("Tất cả");
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
            showError("Lỗi tải dữ liệu", "Không thể tải danh sách khách hàng: " + e.getMessage());
        }
    }

    private void performSearch() {
        String keyword = searchField.getText();
        try {
            customerList.clear();
            customerList.addAll(customerService.searchCustomers(keyword));
        } catch (Exception e) {
            showError("Lỗi tìm kiếm", "Không thể tìm kiếm khách hàng: " + e.getMessage());
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
            showError("Lỗi lọc dữ liệu", "Không thể lọc khách hàng: " + e.getMessage());
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
                showSuccess("Thành công", "Đã thêm khách hàng mới!");
            } catch (CustomerService.ValidationException e) {
                showError("Lỗi xác thực", e.getMessage());
            } catch (Exception e) {
                showError("Lỗi", "Không thể thêm khách hàng: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleEdit() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để sửa!");
            return;
        }

        CustomerDialog dialog = new CustomerDialog(selected, true);
        Optional<Customer> result = dialog.showAndWait();

        result.ifPresent(customer -> {
            try {
                customerService.updateCustomer(customer);
                loadCustomers();
                showSuccess("Thành công", "Đã cập nhật thông tin khách hàng!");
            } catch (CustomerService.ValidationException e) {
                showError("Lỗi xác thực", e.getMessage());
            } catch (Exception e) {
                showError("Lỗi", "Không thể cập nhật khách hàng: " + e.getMessage());
            }
        });
    }

    @FXML
    private void handleDelete() {
        Customer selected = customerTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Chưa chọn khách hàng", "Vui lòng chọn một khách hàng để xóa!");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Bạn có chắc chắn muốn xóa khách hàng này?");
        confirmDialog.setContentText("Tên: " + selected.getFullName() + "\nSĐT: " + selected.getPhone());

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                customerService.deleteCustomer(selected.getId());
                loadCustomers();
                showSuccess("Thành công", "Đã xóa khách hàng!");
            } catch (Exception e) {
                showError("Lỗi", "Không thể xóa khách hàng: " + e.getMessage());
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
