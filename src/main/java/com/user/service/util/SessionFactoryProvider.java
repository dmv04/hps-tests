package com.user.service.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionFactoryProvider {
    private static final Logger logger = LogManager.getLogger(SessionFactoryProvider.class);
    private static SessionFactory instance;

    private SessionFactoryProvider() {}

    public static SessionFactory getInstance() {
        if (instance == null) {
            synchronized (SessionFactoryProvider.class) {
                if (instance == null) {
                    try {
                        Configuration config = new Configuration().configure();

                        String url = System.getProperty("hibernate.connection.url");
                        String username = System.getProperty("hibernate.connection.username");
                        String password = System.getProperty("hibernate.connection.password");

                        if (url != null) config.setProperty("hibernate.connection.url", url);
                        if (username != null) config.setProperty("hibernate.connection.username", username);
                        if (password != null) config.setProperty("hibernate.connection.password", password);

                        instance = config.buildSessionFactory();
                        logger.info("SessionFactory initialized successfully");
                    } catch (Exception e) {
                        logger.error("Failed to initialize SessionFactory", e);
                        throw new RuntimeException("Failed to initialize SessionFactory", e);
                    }
                }
            }
        }
        return instance;
    }

    public static void shutdown() {
        if (instance != null && !instance.isClosed()) {
            instance.close();
            logger.info("SessionFactory closed");
        }
    }
}
