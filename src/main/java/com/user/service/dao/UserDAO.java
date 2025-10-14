package com.user.service.dao;

import com.user.service.entities.User;
import com.user.service.util.SessionFactoryProvider;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static final Logger logger = LogManager.getLogger(UserDAO.class);

    public void save(User user) {
        Transaction transaction = null;
        try (Session session = SessionFactoryProvider.getInstance().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("User saved: {}", user);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving user", e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Session session = SessionFactoryProvider.getInstance().openSession()) {
            User user = session.get(User.class, id);
            logger.info("User found by ID {}: {}", id, user);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw new RuntimeException("Failed to find user by ID", e);
        }
    }

    public List<User> findAll() {
        try (Session session = SessionFactoryProvider.getInstance().openSession()) {
            List<User> users = session.createQuery("FROM User", User.class).list();
            logger.info("Retrieved {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error fetching all users", e);
            throw new RuntimeException("Failed to fetch users", e);
        }
    }

    public void update(User user) {
        Transaction transaction = null;
        try (Session session = SessionFactoryProvider.getInstance().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            logger.info("User updated: {}", user);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user", e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public void deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = SessionFactoryProvider.getInstance().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                transaction.commit();
                logger.info("User deleted: {}", user);
            } else {
                logger.warn("Attempt to delete non-existent user with ID: {}", id);
            }
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }
}
