package com.user.service.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionFactoryProvider {
    private static final Logger logger = LogManager.getLogger(SessionFactoryProvider.class);

    private static final SessionFactory INSTANCE;

    static {
        try {
            INSTANCE = new Configuration().configure().buildSessionFactory();
            logger.info("SessionFactory initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    private SessionFactoryProvider() {
    }

    public static SessionFactory getInstance() {
        return INSTANCE;
    }

    public static void shutdown() {
        if (INSTANCE != null && !INSTANCE.isClosed()) {
            INSTANCE.close();
            logger.info("SessionFactory closed");
        }
    }
}