package com.customer.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int BCRYPT_ROUNDS = 10;

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    public static boolean verify(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(password, hash);
    }
}
