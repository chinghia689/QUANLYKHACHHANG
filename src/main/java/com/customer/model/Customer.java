package com.customer.model;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Customer {
    private final LongProperty id;
    private final StringProperty fullName;
    private final StringProperty phone;
    private final StringProperty email;
    private final StringProperty address;
    private final ObjectProperty<LocalDate> dateOfBirth;
    private final ObjectProperty<CustomerType> customerType;
    private final ObjectProperty<LocalDateTime> createdDate;

    public Customer() {
        this(null, "", "", "", "", null, CustomerType.REGULAR, LocalDateTime.now());
    }

    public Customer(Long id, String fullName, String phone, String email,
            String address, LocalDate dateOfBirth, CustomerType customerType,
            LocalDateTime createdDate) {
        this.id = new SimpleLongProperty(id != null ? id : 0);
        this.fullName = new SimpleStringProperty(fullName);
        this.phone = new SimpleStringProperty(phone);
        this.email = new SimpleStringProperty(email);
        this.address = new SimpleStringProperty(address);
        this.dateOfBirth = new SimpleObjectProperty<>(dateOfBirth);
        this.customerType = new SimpleObjectProperty<>(customerType);
        this.createdDate = new SimpleObjectProperty<>(createdDate);
    }

    // ID Property
    public long getId() {
        return id.get();
    }

    public void setId(long id) {
        this.id.set(id);
    }

    public LongProperty idProperty() {
        return id;
    }

    // Full Name Property
    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    // Phone Property
    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    // Email Property
    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    // Address Property
    public String getAddress() {
        return address.get();
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public StringProperty addressProperty() {
        return address;
    }

    // Date of Birth Property
    public LocalDate getDateOfBirth() {
        return dateOfBirth.get();
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth.set(dateOfBirth);
    }

    public ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirth;
    }

    // Customer Type Property
    public CustomerType getCustomerType() {
        return customerType.get();
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType.set(customerType);
    }

    public ObjectProperty<CustomerType> customerTypeProperty() {
        return customerType;
    }

    // Created Date Property
    public LocalDateTime getCreatedDate() {
        return createdDate.get();
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate.set(createdDate);
    }

    public ObjectProperty<LocalDateTime> createdDateProperty() {
        return createdDate;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + getId() +
                ", fullName='" + getFullName() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", customerType=" + getCustomerType() +
                '}';
    }
}
