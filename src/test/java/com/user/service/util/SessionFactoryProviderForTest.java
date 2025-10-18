package com.user.service.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class SessionFactoryProviderForTest {

    public static SessionFactory create(String jdbcUrl, String username, String password) {
        Configuration config = new Configuration();
        config.configure("hibernate-test.cfg.xml");
        config.setProperty("hibernate.connection.url", jdbcUrl);
        config.setProperty("hibernate.connection.username", username);
        config.setProperty("hibernate.connection.password", password);
        return config.buildSessionFactory();
    }
}