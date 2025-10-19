package com.user.service.dao;

import com.user.service.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.hibernate.query.Query;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserDAOTest {

    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private Transaction transaction;

    @InjectMocks
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.beginTransaction()).thenReturn(transaction);
    }

    @Test
    void save_shouldPersistUserAndCommit() {
        User user = new User("Alice", "alice@test.com", 30);

        userDAO.save(user);

        verify(session).persist(user);
        verify(transaction).commit();
    }

    @Test
    void save_shouldRollbackAndThrowOnException() {
        User user = new User("Bob", "bob@test.com", 25);
        doThrow(new RuntimeException("DB error")).when(session).persist(user);

        assertThatThrownBy(() -> userDAO.save(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to save user");

        verify(transaction).rollback();
    }


    @Test
    void findById_shouldReturnUserWhenExists() {
        User user = new User("Charlie", "charlie@test.com", 40);
        user.setId(1L);
        when(session.get(User.class, 1L)).thenReturn(user);

        var result = userDAO.findById(1L);

        assertThat(result).isPresent().containsSame(user);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(session.get(User.class, 999L)).thenReturn(null);

        var result = userDAO.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldThrowOnDatabaseError() {
        when(session.get(User.class, 1L)).thenThrow(new RuntimeException("DB down"));

        assertThatThrownBy(() -> userDAO.findById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to find user by ID");
    }


    @Test
    void findAll_shouldReturnUsers() {
        User user1 = new User("User1", "u1@test.com", 20);
        User user2 = new User("User2", "u2@test.com", 22);
        List<User> users = List.of(user1, user2);

        @SuppressWarnings("unchecked")
        Query<User> query = mock(Query.class);
        when(query.list()).thenReturn(users);

        when(session.createQuery(("FROM User"), (User.class))).thenReturn(query);

        List<User> result = userDAO.findAll();

        assertThat(result).hasSize(2).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    void findAll_shouldThrowOnDatabaseError() {
        @SuppressWarnings("unchecked")
        Query<User> query = mock(Query.class);
        when(query.list()).thenThrow(new RuntimeException("Database connection failed"));

        when(session.createQuery(("FROM User"), (User.class))).thenReturn(query);

        assertThatThrownBy(() -> userDAO.findAll())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to fetch users");
    }


    @Test
    void update_shouldMergeUserAndCommit() {
        User user = new User("Old", "old@test.com", 30);

        userDAO.update(user);

        verify(session).merge(user);
        verify(transaction).commit();
    }

    @Test
    void update_shouldRollbackAndThrowOnException() {
        User user = new User("Fail", "fail@test.com", 50);
        doThrow(new RuntimeException("Update error")).when(session).merge(user);

        assertThatThrownBy(() -> userDAO.update(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to update user");

        verify(transaction).rollback();
    }


    @Test
    void deleteById_shouldRemoveUserAndCommit() {
        User user = new User("ToDelete", "del@test.com", 33);
        user.setId(5L);
        when(session.get(User.class, 5L)).thenReturn(user);

        userDAO.deleteById(5L);

        verify(session).remove(user);
        verify(transaction).commit();
    }

    @Test
    void deleteById_shouldDoNothingWhenUserNotFound() {
        when(session.get(User.class, 999L)).thenReturn(null);

        userDAO.deleteById(999L);

        verify(session, never()).remove(any());
        verify(transaction).commit();
    }

    @Test
    void deleteById_shouldRollbackAndThrowOnDatabaseError() {
        when(session.get(User.class, 1L)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> userDAO.deleteById(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete user");

        verify(transaction).rollback();
    }

    @Test
    void constructor_shouldThrowWhenSessionFactoryIsNull() {
        assertThatThrownBy(() -> new UserDAO(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("SessionFactory cannot be null");
    }

    @Test
    void deleteById_shouldRollbackWhenRemoveFails() {
        User user = new User("ToDelete", "del@test.com", 33);
        user.setId(5L);
        when(session.get(User.class, 5L)).thenReturn(user);
        doThrow(new RuntimeException("Remove failed")).when(session).remove(user);

        assertThatThrownBy(() -> userDAO.deleteById(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete user");
    }

    @Test
    void deleteById_shouldLogWarningWhenRollbackFails() {
        User user = new User("ToDelete", "del@test.com", 33);
        user.setId(5L);

        when(session.get(User.class, 5L)).thenReturn(user);
        doThrow(new RuntimeException("Remove failed")).when(session).remove(user);
        doThrow(new RuntimeException("Rollback failed")).when(transaction).rollback();

        assertThatThrownBy(() -> userDAO.deleteById(5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to delete user");

        verify(transaction).rollback();
    }
}
