package com.customer.util;

import com.customer.model.Role;
import com.customer.model.User;

public class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(Role... roles) {
        if (currentUser == null) {
            return false;
        }
        for (Role role : roles) {
            if (currentUser.getRole() == role) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public static boolean isManager() {
        return hasRole(Role.MANAGER);
    }

    public static boolean isStaff() {
        return hasRole(Role.STAFF);
    }
}
