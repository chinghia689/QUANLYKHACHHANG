package com.customer.service;

import com.customer.dao.CustomerDAO;
import com.customer.model.Customer;
import com.customer.model.CustomerType;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerService {
    private final CustomerDAO customerDAO;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public void addCustomer(Customer customer) throws SQLException, ValidationException {
        validateCustomer(customer);
        customerDAO.save(customer);
    }

    public void updateCustomer(Customer customer) throws SQLException, ValidationException {
        validateCustomer(customer);
        customerDAO.update(customer);
    }

    public void deleteCustomer(long id) throws SQLException {
        customerDAO.delete(id);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.findAll();
    }

    public Customer getCustomerById(long id) throws SQLException {
        return customerDAO.findById(id);
    }

    public List<Customer> searchCustomers(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerDAO.search(keyword);
    }

    public List<Customer> getCustomersByType(CustomerType type) throws SQLException {
        return customerDAO.findByType(type);
    }

    private void validateCustomer(Customer customer) throws ValidationException {
        if (customer.getFullName() == null || customer.getFullName().trim().isEmpty()) {
            throw new ValidationException("Tên khách hàng không được để trống!");
        }

        if (customer.getFullName().trim().length() < 2) {
            throw new ValidationException("Tên khách hàng phải có ít nhất 2 ký tự!");
        }

        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
                throw new ValidationException("Email không hợp lệ!");
            }
        }

        if (customer.getPhone() != null && !customer.getPhone().trim().isEmpty()) {
            String phone = customer.getPhone().replaceAll("[\\s-]", "");
            if (!phone.matches("\\d{9,11}")) {
                throw new ValidationException("Số điện thoại phải có 9-11 chữ số!");
            }
        }

        if (customer.getCustomerType() == null) {
            throw new ValidationException("Loại khách hàng không được để trống!");
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}
