package com.user.service.dao;

import com.user.service.entities.User;
import com.user.service.util.SessionFactoryProviderForTest;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDAOTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserDAO userDAO;
    private SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        sessionFactory = SessionFactoryProviderForTest.create(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        userDAO = new UserDAO(sessionFactory);
    }

    @AfterEach
    void tearDown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    @Test
    void save_shouldPersistUser() {
        User user = new User("Alice", "alice@example.com", 30);
        userDAO.save(user);

        assertNotNull(user.getId());
        assertThat(user.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = new User("Bob", "bob@example.com", 25);
        userDAO.save(user);

        var found = userDAO.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Bob", found.get().getName());
        assertEquals("bob@example.com", found.get().getEmail());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        var found = userDAO.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userDAO.save(new User("User1", "u1@test.com", 20));
        userDAO.save(new User("User2", "u2@test.com", 22));

        List<User> users = userDAO.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void update_shouldModifyExistingUser() {
        User user = new User("Old", "old@test.com", 40);
        userDAO.save(user);

        user.setName("New Name");
        user.setEmail("new@test.com");
        user.setAge(45);
        userDAO.update(user);

        var updated = userDAO.findById(user.getId()).get();
        assertEquals("New Name", updated.getName());
        assertEquals("new@test.com", updated.getEmail());
        assertEquals(45, updated.getAge());
    }

    @Test
    void deleteById_shouldRemoveUser() {
        User user = new User("ToDelete", "del@test.com", 33);
        userDAO.save(user);

        userDAO.deleteById(user.getId());

        var found = userDAO.findById(user.getId());
        assertFalse(found.isPresent());
    }
}
