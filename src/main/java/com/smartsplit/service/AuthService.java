package com.smartsplit.service;

import com.smartsplit.dao.UserDAO;
import com.smartsplit.model.User;
import com.smartsplit.util.PasswordUtil;
import com.smartsplit.util.Validator;

import java.util.Optional;

/** Authentication and registration use cases for the desktop application. */
public class AuthService {
    private final UserDAO dao = new UserDAO();
    private User current;

    public User login(String username, String password) {
        String cleanUsername = Validator.required(username, "Username");
        Validator.required(password, "Password");
        Optional<User> user = dao.authenticate(cleanUsername, password);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        current = user.get();
        return current;
    }

    public int register(String name, String username, String email, String password) {
        String cleanName = Validator.required(name, "Name");
        String cleanUsername = Validator.required(username, "Username");
        String cleanEmail = Validator.required(email, "Email");
        Validator.required(password, "Password");

        if (cleanUsername.length() < 3 || !cleanUsername.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException(
                    "Username must be at least 3 characters and use only letters, numbers, dots, underscores or hyphens.");
        }
        if (!cleanEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }

        User newUser = new User(0, cleanName, cleanUsername, cleanEmail);
        return dao.create(newUser, PasswordUtil.hash(password));
    }

    public User getCurrent() {
        return current;
    }

    public void logout() {
        current = null;
    }
}
