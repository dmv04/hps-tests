package com.user.service;

import com.user.service.entities.User;
import com.user.service.service.UserService;

import java.io.PrintStream;
import java.util.Scanner;

public class ApplicationRunner {
    private final Scanner scanner;
    private final PrintStream out;
    private final UserService userService;

    public ApplicationRunner(Scanner scanner, PrintStream out, UserService userService) {
        this.scanner = scanner;
        this.out = out;
        this.userService = userService;
    }

    public void run() {
        out.println("User Service (Hibernate + PostgreSQL)");
        boolean running = true;

        while (running) {
            out.println("\nChoose an operation:");
            out.println("1. Create User");
            out.println("2. Read All Users");
            out.println("3. Read User by ID");
            out.println("4. Update User");
            out.println("5. Delete User");
            out.println("6. Exit");
            out.print("Enter choice: ");

            int choice = getIntInput();
            switch (choice) {
                case 1 -> createUser();
                case 2 -> readAllUsers();
                case 3 -> readUserById();
                case 4 -> updateUser();
                case 5 -> deleteUser();
                case 6 -> {
                    running = false;
                    out.println("Goodbye!");
                }
                default -> out.println("Invalid choice. Try again.");
            }
        }
    }

    private void createUser() {
        try {
            out.print("Enter name: ");
            String name = scanner.nextLine().trim();
            out.print("Enter email: ");
            String email = scanner.nextLine().trim();
            out.print("Enter age: ");
            Integer age = getIntInput();

            User user = userService.createUser(name, email, age);
            out.println("User created: " + user);
        } catch (Exception e) {
            out.println("Failed to create user: " + e.getMessage());
        }
    }

    private void readAllUsers() {
        try {
            var users = userService.getAllUsers();
            if (users.isEmpty()) {
                out.println("No users found.");
            } else {
                out.println("All users:");
                users.forEach(out::println);
            }
        } catch (Exception e) {
            out.println("Failed to read users: " + e.getMessage());
        }
    }

    private void readUserById() {
        try {
            out.print("Enter user ID: ");
            Long id = getLongInput();
            var userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                out.println("Found: " + userOpt.get());
            } else {
                out.println("User with ID " + id + " not found.");
            }
        } catch (Exception e) {
            out.println("Failed to read user: " + e.getMessage());
        }
    }

    private void updateUser() {
        try {
            out.print("Enter user ID to update: ");
            Long id = getLongInput();

            out.print("Enter new name (leave empty to skip): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = null;

            out.print("Enter new email (leave empty to skip): ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = null;

            out.print("Enter new age (leave empty to skip): ");
            String ageStr = scanner.nextLine().trim();
            Integer age = null;
            if (!ageStr.isEmpty()) {
                age = Integer.parseInt(ageStr);
            }

            User updated = userService.updateUser(id, name, email, age);
            out.println("User updated: " + updated);
        } catch (Exception e) {
            out.println("Failed to update user: " + e.getMessage());
        }
    }

    private void deleteUser() {
        try {
            out.print("Enter user ID to delete: ");
            Long id = getLongInput();
            userService.deleteUser(id);
            out.println("User with ID " + id + " deleted (if existed).");
        } catch (Exception e) {
            out.println("Failed to delete user: " + e.getMessage());
        }
    }

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                out.print("Invalid number. Try again: ");
            }
        }
    }

    private long getLongInput() {
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                out.print("Invalid ID. Try again: ");
            }
        }
    }
}