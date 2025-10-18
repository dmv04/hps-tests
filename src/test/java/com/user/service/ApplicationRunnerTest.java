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
import java.util.Optional;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
}
