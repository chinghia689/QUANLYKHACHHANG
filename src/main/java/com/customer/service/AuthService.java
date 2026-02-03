package com.customer.service;

import com.customer.dao.UserDAO;
import com.customer.model.User;
import com.customer.model.UserStatus;
import com.customer.util.PasswordUtil;
import com.customer.util.SessionManager;

import java.time.LocalDateTime;
import java.util.Optional;

public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 5;

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return new LoginResult(false, "Please enter username");
        }
        if (password == null || password.isEmpty()) {
            return new LoginResult(false, "Please enter password");
        }

        Optional<User> optionalUser = userDAO.findByUsername(username.trim());
        if (optionalUser.isEmpty()) {
            return new LoginResult(false, "Username does not exist");
        }

        User user = optionalUser.get();

        // Check if account is permanently locked
        if (user.getStatus() == UserStatus.LOCKED) {
            return new LoginResult(false, "Account is permanently locked. Please contact administrator");
        }

        // Check if account is inactive
        if (user.getStatus() == UserStatus.INACTIVE) {
            return new LoginResult(false, "Account is inactive");
        }

        // Check temporary lock
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes() + 1;
            return new LoginResult(false, "Account is temporarily locked. Please try again after " + remainingMinutes + " minutes");
        }

        // Verify password
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            userDAO.incrementFailedAttempts(user.getId());
            int newFailedAttempts = user.getFailedAttempts() + 1;

            if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
                userDAO.lockUser(user.getId(), lockUntil);
                return new LoginResult(false, "Incorrect password " + MAX_FAILED_ATTEMPTS + " times. Account locked for " + LOCK_DURATION_MINUTES + " minutes");
            }

            int remainingAttempts = MAX_FAILED_ATTEMPTS - newFailedAttempts;
            return new LoginResult(false, "Incorrect password. " + remainingAttempts + " attempts remaining");
        }

        // Login successful
        userDAO.resetFailedAttempts(user.getId());
        userDAO.updateLastLogin(user.getId());
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());

        SessionManager.login(user);

        return new LoginResult(true, "Login successful");
    }

    public void logout() {
        SessionManager.logout();
    }

    public ChangePasswordResult changePassword(Long userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            return new ChangePasswordResult(false, "Please enter old password");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return new ChangePasswordResult(false, "Please enter new password");
        }
        if (newPassword.length() < 6) {
            return new ChangePasswordResult(false, "New password must have at least 6 characters");
        }

        Optional<User> optionalUser = userDAO.findById(userId);
        if (optionalUser.isEmpty()) {
            return new ChangePasswordResult(false, "User not found");
        }

        User user = optionalUser.get();

        if (!PasswordUtil.verify(oldPassword, user.getPasswordHash())) {
            return new ChangePasswordResult(false, "Incorrect old password");
        }

        String newHash = PasswordUtil.hash(newPassword);
        userDAO.updatePassword(userId, newHash);

        return new ChangePasswordResult(true, "Password changed successfully");
    }

    public static class LoginResult {
        private final boolean success;
        private final String message;

        public LoginResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class ChangePasswordResult {
        private final boolean success;
        private final String message;

        public ChangePasswordResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
