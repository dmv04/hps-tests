package com.user.service;

import com.user.service.dao.UserDAO;
import com.user.service.entities.User;
import com.user.service.service.UserService;
import com.user.service.util.SessionFactoryProvider;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final UserDAO userDAO = new UserDAO();
    private static final UserService userService = new UserService(userDAO);


    public static void main(String[] args) {
        try {
            System.out.println("User Service (Hibernate + PostgreSQL)");
            boolean running = true;

            while (running) {
                System.out.println("\nChoose an operation:");
                System.out.println("1. Create User");
                System.out.println("2. Read All Users");
                System.out.println("3. Read User by ID");
                System.out.println("4. Update User");
                System.out.println("5. Delete User");
                System.out.println("6. Exit");
                System.out.print("Enter choice: ");

                int choice = getIntInput();
                switch (choice) {
                    case 1 -> createUser();
                    case 2 -> readAllUsers();
                    case 3 -> readUserById();
                    case 4 -> updateUser();
                    case 5 -> deleteUser();
                    case 6 -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid choice. Try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            SessionFactoryProvider.shutdown();
            scanner.close();
        }
    }

    private static void createUser() {
        try {
            System.out.print("Enter name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter age: ");
            Integer age = getIntInput();

            User user = userService.createUser(name, email, age);
            System.out.println("User created: " + user);
        } catch (Exception e) {
            System.err.println("Failed to create user: " + e.getMessage());
        }
    }

    private static void readAllUsers() {
        try {
            var users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("No users found.");
            } else {
                System.out.println("All users:");
                users.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.err.println("Failed to read users: " + e.getMessage());
        }
    }

    private static void readUserById() {
        try {
            System.out.print("Enter user ID: ");
            Long id = getLongInput();
            var userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                System.out.println("Found: " + userOpt.get());
            } else {
                System.out.println("User with ID " + id + " not found.");
            }
        } catch (Exception e) {
            System.err.println("Failed to read user: " + e.getMessage());
        }
    }

    private static void updateUser() {
        try {
            System.out.print("Enter user ID to update: ");
            Long id = getLongInput();

            System.out.print("Enter new name (leave empty to skip): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = null;

            System.out.print("Enter new email (leave empty to skip): ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = null;

            System.out.print("Enter new age (leave empty to skip): ");
            String ageStr = scanner.nextLine().trim();
            Integer age = null;
            if (!ageStr.isEmpty()) {
                age = Integer.parseInt(ageStr);
            }

            User updated = userService.updateUser(id, name, email, age);
            System.out.println("User updated: " + updated);
        } catch (Exception e) {
            System.err.println("Failed to update user: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        try {
            System.out.print("Enter user ID to delete: ");
            Long id = getLongInput();
            userService.deleteUser(id);
            System.out.println("User with ID " + id + " deleted (if existed).");
        } catch (Exception e) {
            System.err.println("Failed to delete user: " + e.getMessage());
        }
    }

    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    private static long getLongInput() {
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid ID. Try again: ");
            }
        }
    }
}
