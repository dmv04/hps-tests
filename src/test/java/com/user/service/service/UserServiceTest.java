package com.user.service.service;

import com.user.service.dao.UserDAO;
import com.user.service.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_shouldSaveValidUser() {
        String name = "John";
        String email = "john@example.com";
        Integer age = 30;

        when(userDAO.findById(any())).thenReturn(Optional.empty());

        User created = userService.createUser(name, email, age);

        assertNotNull(created);
        assertEquals(name, created.getName());
        assertEquals(email, created.getEmail());
        assertEquals(age, created.getAge());
        verify(userDAO).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowOnNullName() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(null, "test@test.com", 25)
        );
        assertTrue(ex.getMessage().contains("Name"));
    }

    @Test
    void createUser_shouldThrowOnInvalidEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John", "invalid-email", 25)
        );
    }

    @Test
    void updateUser_shouldUpdateFields() {
        Long id = 1L;
        User existing = new User("Old", "old@test.com", 40);
        existing.setId(id);
        when(userDAO.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateUser(id, "New", "new@test.com", 45);

        assertEquals("New", updated.getName());
        assertEquals("new@test.com", updated.getEmail());
        assertEquals(45, updated.getAge());
        verify(userDAO).update(existing);
    }

    @Test
    void updateUser_shouldThrowIfUserNotFound() {
        when(userDAO.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(999L, "X", "x@test.com", 10)
        );
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void deleteUser_shouldCallDAO() {
        userService.deleteUser(1L);
        verify(userDAO).deleteById(1L);
    }

    @Test
    void getUserById_shouldDelegateToDAO() {
        User user = new User("Test", "test@test.com", 20);
        user.setId(1L);
        when(userDAO.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void createUser_shouldThrowWhenDAOFails() {
        doThrow(new RuntimeException("DB error"))
                .when(userDAO).save(any(User.class));

        assertThrows(RuntimeException.class, () -> userService.createUser("Alice", "a@a.com", 20));
    }

    @Test
    void updateUser_shouldThrowIfAgeNegative() {
        doThrow(new RuntimeException("Age cannot be negative"))
                .when(userDAO).save(any(User.class));

        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, "Alice", "a@a.com", -1));
    }

    @Test
    void createUser_shouldThrowWhenNameIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(null, "test@test.com", 25)
        );
        assertEquals("Name is required", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenNameIsEmpty() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("", "test@test.com", 25)
        );
        assertEquals("Name is required", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenNameIsBlank() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("   ", "test@test.com", 25)
        );
        assertEquals("Name is required", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenEmailIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John", null, 25)
        );
        assertEquals("Valid email is required", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenEmailIsEmpty() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John", "", 25)
        );
        assertEquals("Valid email is required", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenEmailIsInvalid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John", "invalid-email", 25)
        );
        assertEquals("Valid email is required", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenAgeIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John", "john@test.com", null)
        );
        assertEquals("Age must be a non-negative number", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowWhenAgeIsNegative() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("John", "john@test.com", -5)
        );
        assertEquals("Age must be a non-negative number", ex.getMessage());
    }

    @Test
    void updateUser_shouldThrowWhenAgeIsNegative() {
        Long id = 1L;
        User existing = new User("Old", "old@test.com", 30);
        existing.setId(id);
        when(userDAO.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(id, "New", "new@test.com", -5),
                "Age cannot be negative"
        );

        verify(userDAO, never()).update(any());
    }

    @Test
    void updateUser_shouldUpdateNameWhenProvided() {
        Long id = 1L;
        User existing = new User("Old", "old@test.com", 30);
        existing.setId(id);
        when(userDAO.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateUser(id, "Updated", null, null);

        assertEquals("Updated", updated.getName());
        verify(userDAO).update(updated);
    }

    @Test
    void updateUser_shouldUpdateEmailWhenProvided() {
        Long id = 1L;
        User existing = new User("Old", "old@test.com", 30);
        existing.setId(id);
        when(userDAO.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateUser(id, null, "updated@test.com", null);

        assertEquals("updated@test.com", updated.getEmail());
        verify(userDAO).update(updated);
    }

    @Test
    void updateUser_shouldSkipNameWhenEmpty() {
        Long id = 1L;
        User existing = new User("Old", "old@test.com", 30);
        existing.setId(id);
        when(userDAO.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateUser(id, "", null, null);

        assertEquals("Old", updated.getName());
        verify(userDAO).update(updated);
    }

    @Test
    void updateUser_shouldSkipEmailWhenEmpty() {
        Long id = 1L;
        User existing = new User("Old", "old@test.com", 30);
        existing.setId(id);
        when(userDAO.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateUser(id, null, "", null);

        assertEquals("old@test.com", updated.getEmail());
        verify(userDAO).update(updated);
    }
}
