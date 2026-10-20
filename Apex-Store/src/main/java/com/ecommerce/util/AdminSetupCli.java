package com.ecommerce.util;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;

import java.util.Scanner;

/**
 * Utility tool to create an Administrator account securely from the command line.
 * Can be run with: java -cp target/ecommerce/WEB-INF/classes com.ecommerce.util.AdminSetupCli
 */
public class AdminSetupCli {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   ApexStore Administrator Provisioning Tool     ");
        System.out.println("==================================================");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter Admin Full Name: ");
            String name = scanner.nextLine().trim();

            System.out.print("Enter Admin Email: ");
            String email = scanner.nextLine().trim().toLowerCase();

            System.out.print("Enter Admin Password: ");
            String password = scanner.nextLine();

            System.out.print("Enter Admin Phone (optional): ");
            String phone = scanner.nextLine().trim();

            if (name.isEmpty() || email.isEmpty() || password.length() < 6) {
                System.err.println("Error: Name and Email are required. Password must be at least 6 characters.");
                return;
            }

            UserDAO userDAO = new UserDAO();
            if (userDAO.emailExists(email)) {
                System.err.println("Error: A user with email '" + email + "' already exists!");
                return;
            }

            String passwordHash = PasswordUtil.hashPassword(password);

            User admin = new User();
            admin.setName(name);
            admin.setEmail(email);
            admin.setPasswordHash(passwordHash);
            admin.setPhone(phone);
            admin.setRole(Role.ADMIN);

            boolean success = userDAO.createUser(admin);
            if (success) {
                System.out.println("SUCCESS: Administrator account '" + email + "' provisioned with ID " + admin.getId());
            } else {
                System.err.println("FAILURE: Could not create administrator account. Please check database connectivity.");
            }
        }
    }
}
