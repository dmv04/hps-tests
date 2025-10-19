package com.user.service;

import com.user.service.entities.User;
import com.user.service.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApplicationRunnerTest {

    @Mock
    private UserService userService;

    private AutoCloseable closeable;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        System.setOut(originalOut);
    }

    @Test
    void run_shouldExitImmediatelyWhenChoosingOption6() {
        String input = "6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User Service (Hibernate + PostgreSQL)").contains("Goodbye!");
        verifyNoInteractions(userService);
    }

    @Test
    void run_shouldCreateUserWhenChoosingOption1() {
        String input = "1\nAlice\nalice@test.com\n30\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User mockUser = new User("Alice", "alice@test.com", 30);
        mockUser.setId(1L);
        when(userService.createUser("Alice", "alice@test.com", 30)).thenReturn(mockUser);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User created: User{id=1").contains("Goodbye!");
        verify(userService).createUser("Alice", "alice@test.com", 30);
    }

    @Test
    void run_shouldHandleInvalidChoice() {
        String input = "99\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Invalid choice. Try again.").contains("Goodbye!");
    }

    @Test
    void run_shouldReadAllUsersWhenChoosingOption2() {
        String input = "2\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User user = new User("Bob", "bob@test.com", 25);
        user.setId(2L);
        when(userService.getAllUsers()).thenReturn(java.util.List.of(user));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("All users:").contains("Bob");
        verify(userService).getAllUsers();
    }

    @Test
    void run_shouldReadUserByIdWhenChoosingOption3() {
        String input = "3\n5\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User user = new User("Charlie", "charlie@test.com", 40);
        user.setId(5L);
        when(userService.getUserById(5L)).thenReturn(Optional.of(user));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Found: User{id=5");
        verify(userService).getUserById(5L);
    }

    @Test
    void run_shouldUpdateUserWhenChoosingOption4() {
        String input = "4\n1\nUpdated\n\n99\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User updatedUser = new User("Updated", "old@test.com", 99);
        updatedUser.setId(1L);

        when(userService.updateUser(1L, "Updated", null, 99)).thenReturn(updatedUser);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);
        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User updated: User{id=1").contains("Goodbye!");

        verify(userService).updateUser(1L, "Updated", null, 99);
    }

    @Test
    void run_shouldDeleteUserWhenChoosingOption5() {
        String input = "5\n7\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        doNothing().when(userService).deleteUser(7L);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User with ID 7 deleted (if existed).").contains("Goodbye!");

        verify(userService).deleteUser(7L);
    }

    @Test
    void createUser_shouldPrintSuccessWhenValidInput() {
        String input = "1\nAlice\nalice@test.com\n30\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User mockUser = new User("Alice", "alice@test.com", 30);
        mockUser.setId(1L);
        when(userService.createUser("Alice", "alice@test.com", 30)).thenReturn(mockUser);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User created: User{id=1").contains("Goodbye!");
    }

    @Test
    void createUser_shouldPrintErrorWhenUserServiceFails() {
        String input = "1\nBob\nbob@test.com\n25\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        when(userService.createUser("Bob", "bob@test.com", 25))
                .thenThrow(new RuntimeException("DB error"));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Failed to create user: DB error").contains("Goodbye!");
    }


    @Test
    void readAllUsers_shouldPrintNoUsersWhenEmpty() {
        String input = "2\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("No users found.").contains("Goodbye!");
    }

    @Test
    void readAllUsers_shouldPrintUsersWhenNotEmpty() {
        String input = "2\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User user1 = new User("User1", "u1@test.com", 20);
        user1.setId(1L);
        User user2 = new User("User2", "u2@test.com", 22);
        user2.setId(2L);
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("All users:").contains("User1").contains("User2").contains("Goodbye!");
    }

    @Test
    void readAllUsers_shouldPrintErrorWhenUserServiceFails() {
        String input = "2\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        when(userService.getAllUsers()).thenThrow(new RuntimeException("Query failed"));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Failed to read users: Query failed").contains("Goodbye!");
    }

    @Test
    void readUserById_shouldPrintFoundUser() {
        String input = "3\n1\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User user = new User("Charlie", "charlie@test.com", 40);
        user.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Found: User{id=1").contains("Goodbye!");
    }

    @Test
    void readUserById_shouldPrintNotFoundWhenEmpty() {
        String input = "3\n999\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User with ID 999 not found.").contains("Goodbye!");
    }

    @Test
    void readUserById_shouldPrintErrorWhenUserServiceFails() {
        String input = "3\n1\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        when(userService.getUserById(1L)).thenThrow(new RuntimeException("DB down"));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Failed to read user: DB down").contains("Goodbye!");
    }

    @Test
    void updateUser_shouldPrintSuccessWhenValidInput() {
        String input = "4\n1\nUpdated\nnew@test.com\n99\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User existing = new User("Old", "old@test.com", 30);
        existing.setId(1L);
        User updated = new User("Updated", "new@test.com", 99);
        updated.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(existing));
        when(userService.updateUser(1L, "Updated", "new@test.com", 99)).thenReturn(updated);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User updated: User{id=1").contains("Goodbye!");
    }

    @Test
    void updateUser_shouldPrintErrorWhenUserServiceFails() {
        String input = "4\n1\nNew\nnew@test.com\n30\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        User existing = new User("Old", "old@test.com", 30);
        existing.setId(1L);
        when(userService.getUserById(1L)).thenReturn(Optional.of(existing));
        when(userService.updateUser(1L, "New", "new@test.com", 30))
                .thenThrow(new RuntimeException("Update failed"));

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Failed to update user: Update failed").contains("Goodbye!");
    }


    @Test
    void deleteUser_shouldPrintSuccess() {
        String input = "5\n7\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        doNothing().when(userService).deleteUser(7L);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("User with ID 7 deleted (if existed).").contains("Goodbye!");
    }

    @Test
    void deleteUser_shouldPrintErrorWhenUserServiceFails() {
        String input = "5\n7\n6\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        PrintStream printStream = new PrintStream(outContent);

        doThrow(new RuntimeException("Delete failed")).when(userService).deleteUser(7L);

        ApplicationRunner runner = new ApplicationRunner(scanner, printStream, userService);

        runner.run();

        String output = outContent.toString();
        assertThat(output).contains("Failed to delete user: Delete failed").contains("Goodbye!");
    }
}
