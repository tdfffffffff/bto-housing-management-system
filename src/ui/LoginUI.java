package ui;

import controllers.*;
import entities.User;
import enums.MaritalStatus;
import filters.ProjectFilter;
import ui.applicant.ApplicantUI;
import ui.applicant.EnquiryUI;
import ui.applicant.ProjectUI;
import ui.manager.ManagerUI;
import ui.officer.OfficerUI;

import java.util.Scanner;

public class LoginUI {
    private AuthController authController;
    private ProjectController projController;
    private ApplicationController appController;
    private EnquiryController enqController;
    private RegistrationController regController;
    private Scanner scanner;
    private ProjectFilter projectFilter;
    private BaseUserUI menu;

    public LoginUI(AuthController authController, 
                   ApplicationController appController, 
                   EnquiryController enqController, 
                   ProjectController projController, 
                   Scanner scanner, 
                   RegistrationController regController, 
                   ProjectFilter projectFilter) {
        this.authController = authController;
        this.scanner = scanner;
        this.appController = appController;
        this.enqController = enqController;
        this.projController = projController;
        this.regController = regController;
        this.projectFilter = projectFilter;
    }

    public void start() {
        System.out.println("""   
                 ################## 
                ##      ####      ##
                #     ########    ##
                #   ######  ####  ##
                # ########   ####### 
                #    #####   ##   ##
                #    #####   ##   ##
                #    #####   ##   ##
                ##   ##########   ##
                 ################## 
                    """);
        System.out.println("Welcome to the BTO System!");

        boolean exit = false;

        while (!exit) {
            System.out.println("\n===== Main Menu =====");
            System.out.println("1. Login");
            System.out.println("2. Change Password");
            System.out.println("3. Create New Account");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleChangePassword();
                    break;
                case "3":
                    handleCreateNewAccount();
                    break;
                case "4":
                    exit = true;
                    System.out.println("Exiting the system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void handleLogin() {
        User user = null;
        int count = 3;

        while (user == null && count -- > 0) {
            System.out.print("Enter NRIC: ");
            String nric = scanner.nextLine().trim();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine().trim();


            user = authController.authenticate(nric, password);

            if (user == null && count > 0) {
                System.out.println("Invalid credentials. Try again. Remaining try: " + count);
            }
        }

        if (user == null) {
            System.out.println("Too many failed attempts. Returning to main menu...");
            return;
        }

        System.out.println("Login successful! Welcome, " + user.getName());

        // Deciding which menu to show based on user role
        switch (user.getRole()) {
            case APPLICANT:
                ProjectUI projectUI = new ProjectUI(scanner, projController, projectFilter);
                EnquiryUI enquiryUI = new EnquiryUI(scanner, enqController, projController);
                menu = new ApplicantUI(authController, appController, enqController, projController, scanner, projectFilter, projectUI, enquiryUI);
                break;
            case HDB_OFFICER:
                projectUI = new ProjectUI(scanner, projController, projectFilter);
                enquiryUI = new EnquiryUI(scanner, enqController, projController);;
                menu = new OfficerUI(authController, appController, enqController, projController, regController, scanner, projectFilter, projectUI, enquiryUI);
                break;
            case HDB_MANAGER:
                menu = new ManagerUI(authController, appController, enqController, projController, regController, scanner, projectFilter);
                break;
            default:
                System.out.println("Unknown user role. Exiting.");
                return;
        }

        menu.displayMenu();
    }
    private void handleCreateNewAccount() {
        System.out.println("\n===== Create New Account =====");

        System.out.print("Enter NRIC: ");
        String nric = scanner.nextLine().trim();
        if (!EligibilityChecker.NRICValidator(nric)) {
            System.out.println("NRIC you've entered is not valid. Returning to main menu..");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.print("Enter Age: ");
        int age;
        try {
            age = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Returning to main menu.");
            return;
        }

        System.out.println("What is your Marital Status?");
        System.out.println("1. Single");
        System.out.println("2. Married");
        int choice;
        do {
            System.out.print("Enter choice (1 or 2): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter 1 or 2: ");
                scanner.next();
            }
            choice = scanner.nextInt();
            if (choice != 1 && choice != 2) {
                System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 1 && choice != 2);
        scanner.nextLine();

        MaritalStatus status = (choice == 1)
                ? MaritalStatus.SINGLE
                : MaritalStatus.MARRIED;

        User newUser = authController.createNewUser(nric, name, age, status, password);
        if (newUser != null) {
            System.out.println("Account created successfully! Welcome, " + newUser.getName());
        } else {
            System.out.println("Failed to create account. Account may already exist.");
        }
    }

    private void handleChangePassword() {
        System.out.println("\n===== Change Password =====");
        System.out.print("Enter your NRIC: ");
        String nric = scanner.nextLine().trim();

        System.out.print("Enter your old password: ");
        String oldPassword = scanner.nextLine().trim();

        System.out.print("Enter your new password: ");
        String newPassword = scanner.nextLine().trim();

        if (authController.changePassword(nric, newPassword, oldPassword)) {
            System.out.println("Password changed successfully!");
        } else {
            System.out.println("Failed to change password. NRIC or old password is incorrect.");
        }
    }
}
