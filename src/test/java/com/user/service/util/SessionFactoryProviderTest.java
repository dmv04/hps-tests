package com.user.service.util;

import com.user.service.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Testcontainers
class SessionFactoryProviderTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());

        sessionFactory = SessionFactoryProvider.getInstance();
    }

    @AfterAll
    static void tearDown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        System.clearProperty("hibernate.connection.url");
        System.clearProperty("hibernate.connection.username");
        System.clearProperty("hibernate.connection.password");
    }

    @Test
    void shouldCreateValidSessionFactory() {
        assertThat(sessionFactory).isNotNull();
        assertThat(sessionFactory.isClosed()).isFalse();
    }

    @Test
    void shouldPersistAndRetrieveEntity() {
        try (Session session = sessionFactory.openSession()) {
            var tx = session.beginTransaction();

            User user = new User("IT Test", "it@test.com", 42);
            session.persist(user);
            tx.commit();

            assertThat(user.getId()).isNotNull();

            User found = session.get(User.class, user.getId());
            assertThat(found).isNotNull();
            assertThat(found.getName()).isEqualTo("IT Test");
        }
    }

    @Test
    void shouldInitializeSessionFactory() {
        SessionFactory sf = SessionFactoryProvider.getInstance();
        assertThat(sf).isNotNull();
    }

    @Test
    void getInstance_shouldThrowWhenConfigInvalid() {
        try {
            var field = SessionFactoryProvider.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:9999/bad-db");
        System.setProperty("hibernate.connection.username", "bad-user");
        System.setProperty("hibernate.connection.password", "bad-pass");

        assertThatThrownBy(SessionFactoryProvider::getInstance)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to initialize SessionFactory");

        System.clearProperty("hibernate.connection.url");
        System.clearProperty("hibernate.connection.username");
        System.clearProperty("hibernate.connection.password");
    }

    @Test
    void shutdown_shouldCloseInstanceIfOpen() {
        SessionFactory sf = SessionFactoryProvider.getInstance();
        assertThat(sf).isNotNull();

        SessionFactoryProvider.shutdown();

        assertThat(sf.isClosed()).isTrue();
    }

    @Test
    void shutdown_shouldDoNothingIfInstanceIsNull() {
        try {
            var field = SessionFactoryProvider.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set instance to null", e);
        }

        SessionFactoryProvider.shutdown();

        try {
            var field = SessionFactoryProvider.class.getDeclaredField("instance");
            field.setAccessible(true);
            Object instance = field.get(null);
            assertThat(instance).isNull();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read instance after shutdown", e);
        }
    }
}
