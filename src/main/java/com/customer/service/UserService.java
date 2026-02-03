package com.customer.service;

import com.customer.dao.UserDAO;
import com.customer.model.User;
import com.customer.model.UserStatus;
import com.customer.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userDAO.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public void createUser(User user, String rawPassword) throws ValidationException {
        validateUser(user, true);

        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must have at least 6 characters");
        }

        // Check if username already exists
        if (userDAO.findByUsername(user.getUsername()).isPresent()) {
            throw new ValidationException("Username already exists");
        }

        user.setPasswordHash(PasswordUtil.hash(rawPassword));
        userDAO.insert(user);
    }

    public void updateUser(User user) throws ValidationException {
        validateUser(user, false);

        // Check if username already exists for another user
        Optional<User> existing = userDAO.findByUsername(user.getUsername());
        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            throw new ValidationException("Username already exists");
        }

        userDAO.update(user);
    }

    public void lockUser(Long id) {
        userDAO.updateStatus(id, UserStatus.LOCKED);
    }

    public void unlockUser(Long id) {
        userDAO.updateStatus(id, UserStatus.ACTIVE);
        userDAO.resetFailedAttempts(id);
    }

    public void resetPassword(Long id, String newPassword) throws ValidationException {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("Password must have at least 6 characters");
        }

        String hash = PasswordUtil.hash(newPassword);
        userDAO.updatePassword(id, hash);
        userDAO.resetFailedAttempts(id);
    }

    private void validateUser(User user, boolean isNew) throws ValidationException {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (user.getUsername().length() < 3) {
            throw new ValidationException("Username must have at least 3 characters");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full Name cannot be empty");
        }
        if (user.getRole() == null) {
            throw new ValidationException("Role cannot be empty");
        }
    }

    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
    }
}
