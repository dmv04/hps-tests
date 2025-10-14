package com.user.service.service;

import com.user.service.dao.UserDAO;
import com.user.service.entities.User;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createUser(String name, String email, Integer age) {
        validateUserInput(name, email, age);
        User user = new User(name, email, age);
        userDAO.save(user);
        return user;
    }

    public Optional<User> getUserById(Long id) {
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        Optional<User> existingOpt = userDAO.findById(id);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + id);
        }

        User user = existingOpt.get();

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name);
        }
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }
        if (age != null) {
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }
            user.setAge(age);
        }

        userDAO.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        userDAO.deleteById(id);
    }

    private void validateUserInput(String name, String email, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (age == null || age < 0) {
            throw new IllegalArgumentException("Age must be a non-negative number");
        }
    }
}