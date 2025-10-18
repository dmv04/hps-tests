package com.user.service;

import com.user.service.dao.UserDAO;
import com.user.service.service.UserService;
import com.user.service.util.SessionFactoryProvider;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        UserService userService = new UserService(userDAO);
        ApplicationRunner runner = new ApplicationRunner(
                new java.util.Scanner(System.in),
                System.out,
                userService
        );

        try {
            runner.run();
        } finally {
            SessionFactoryProvider.shutdown();
        }
    }
}
