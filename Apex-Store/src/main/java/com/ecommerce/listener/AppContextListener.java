package com.ecommerce.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("E-Commerce Web Application initialized on Tomcat 11.");
        sce.getServletContext().setAttribute("appName", "ApexStore");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("E-Commerce Web Application stopping. Cleaning up JDBC drivers...");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                LOGGER.info("Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error deregistering driver: " + driver, e);
            }
        }
    }
}
