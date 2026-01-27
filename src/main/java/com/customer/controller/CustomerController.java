package com.customer.controller;

import com.customer.model.Customer;
import com.customer.model.CustomerType;
import com.customer.service.CustomerService;
import com.customer.ui.CustomerDialog;
import com.customer.util.AnimationHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Controller for the Customer management view.
 */
public class CustomerController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<CustomerType> filterComboBox;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, Void> avatarColumn;

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

    @FXML
    private StackPane loadingOverlay;

    private final CustomerService customerService;
    private final ObservableList<Customer> customerList;

    public CustomerController() {
        this.customerService = new CustomerService();
        this.customerList = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        // Initialize avatar column with custom cell factory
        avatarColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Customer customer = getTableRow().getItem();
                    setGraphic(createAvatar(customer.getFullName()));
                }
            }
        });

        // Initialize table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("customerType"));
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));

        // Set custom cell factory for customer type to show Vietnamese display name
        typeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(CustomerType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getDisplayName());
                    // Add color based on type
                    switch (item) {
                        case VIP:
                            setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold;");
                            break;
                        case POTENTIAL:
                            setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        customerTable.setItems(customerList);

        // Initialize filter combo box
        filterComboBox.getItems().add(null); // "All" option
        filterComboBox.getItems().addAll(CustomerType.values());
        filterComboBox.setPromptText("Tất cả");

        // Custom button cell for combo box
        filterComboBox.setButtonCell(new ListCell<>() {
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
        filterComboBox.setCellFactory(lv -> new ListCell<>() {
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

        // Add hover effects to buttons
        AnimationHelper.addScaleOnHover(addButton);
        AnimationHelper.addScaleOnHover(editButton);
        AnimationHelper.addScaleOnHover(deleteButton);

        // Load initial data
        loadCustomers();
    }

    /**
     * Create avatar component with initials.
     */
    private VBox createAvatar(String fullName) {
        String initials = getInitials(fullName);

        Label initialsLabel = new Label(initials);
        initialsLabel.getStyleClass().add("avatar-text");

        StackPane avatar = new StackPane(initialsLabel);
        avatar.getStyleClass().add("avatar");

        VBox container = new VBox(avatar);
        container.setAlignment(Pos.CENTER);
        container.getStyleClass().add("avatar-cell");

        return container;
    }

    /**
     * Extract initials from full name (e.g., "Nguyễn Văn An" -> "NA").
     */
    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "?";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        // Get first letter of first and last name
        String firstInitial = parts[0].substring(0, 1).toUpperCase();
        String lastInitial = parts[parts.length - 1].substring(0, 1).toUpperCase();

        return firstInitial + lastInitial;
    }

    private void loadCustomers() {
        showLoading(true);

        Task<Void> loadTask = new Task<>() {
            private java.util.List<Customer> customers;

            @Override
            protected Void call() throws Exception {
                customers = customerService.getAllCustomers();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    customerList.clear();
                    customerList.addAll(customers);
                    showLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Lỗi tải dữ liệu", "Không thể tải danh sách khách hàng: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void performSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.trim().isEmpty()) {
            loadCustomers();
            return;
        }

        showLoading(true);

        Task<Void> searchTask = new Task<>() {
            private java.util.List<Customer> customers;

            @Override
            protected Void call() throws Exception {
                customers = customerService.searchCustomers(keyword);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    customerList.clear();
                    customerList.addAll(customers);
                    showLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Lỗi tìm kiếm", "Không thể tìm kiếm khách hàng: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };

        new Thread(searchTask).start();
    }

    private void performFilter() {
        CustomerType selectedType = filterComboBox.getValue();

        showLoading(true);

        Task<Void> filterTask = new Task<>() {
            private java.util.List<Customer> customers;

            @Override
            protected Void call() throws Exception {
                if (selectedType == null) {
                    customers = customerService.getAllCustomers();
                } else {
                    customers = customerService.getCustomersByType(selectedType);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    customerList.clear();
                    customerList.addAll(customers);
                    showLoading(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Lỗi lọc dữ liệu", "Không thể lọc khách hàng: " + getException().getMessage());
                    showLoading(false);
                });
            }
        };

        new Thread(filterTask).start();
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
