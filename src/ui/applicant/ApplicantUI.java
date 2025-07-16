package ui.applicant;

import controllers.*;
import entities.Applicant;
import entities.Application;
import entities.BTOProject;
import enums.ApplicationStatus;
import enums.FlatType;
import filters.ProjectFilter;
import ui.BaseUserUI;

import java.util.List;
import java.util.Scanner;

public class ApplicantUI extends BaseUserUI {
    private final ProjectUI projectUI;
    private final EnquiryUI enquiryUI;

    public ApplicantUI(AuthController authController,
                       ApplicationController appController,
                       EnquiryController enqController,
                       ProjectController projController,
                       Scanner scanner,
                       ProjectFilter projectFilter,
                       ProjectUI projectUI,
                       EnquiryUI enquiryUI) 
    {
        super(authController, appController, enqController, projController,scanner, projectFilter);
        this.projectUI = projectUI;
        this.enquiryUI = enquiryUI;
    }


    // Getter Methods
    public ProjectUI getProjectUI() {
        return projectUI;
    }

    public EnquiryUI getEnquiryUI() {
        return enquiryUI;
    }

    // Main method to display the menu and handle user input
    @Override
    public void displayMenu() {
        System.out.println("Welcome to the Applicant Dashboard!");
    
        Applicant applicant = (Applicant) authController.getCurrentUser();
    
        int choice;
        do {
            showMenu();
    
            choice = scanner.nextInt();
            scanner.nextLine();
    
            switch (choice) {
                case 1 -> projectUI.handleProjectOptions(applicant);
                case 2 -> handleApply(applicant);
                case 3 -> handleRequestWithdrawal(applicant);
                case 4 -> handleApplicationStatus(applicant);
                case 5 -> enquiryUI.handleEnquire(applicant);
                case 6 -> logOut();
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (choice != 6);
    }

    protected void showMenu(){
        System.out.println("\n=== Applicant Menu ===");
        System.out.println("1. View Projects");
        System.out.println("2. Apply for a Project");
        System.out.println("3. Request Withdrawal");
        System.out.println("4. View Application Status");
        System.out.println("5. Enquire about a Project");
        System.out.println("6. Logout");
        System.out.print("Enter your choice: ");
    }

    // Handle Option 2: Apply for a Project
    protected void handleApply(Applicant applicant) {
        List<BTOProject> eligibleProjects = projController.getProjectsForApplicants(applicant, projectFilter);

        if (eligibleProjects.isEmpty()) {
            System.out.println("No eligible projects available for your profile.");
            return;
        }

        System.out.println("\n=== Eligible Projects ===");
        for (int i = 0; i < eligibleProjects.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, eligibleProjects.get(i).getSummary());
        }

        int choice = -1;
        int attempts = 3;

        while (attempts-- > 0) {
            System.out.print("Enter the number of the project you'd like to apply for (or 0 to cancel): ");
            choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (choice == 0) {
                System.out.println("Cancelled.");
                return;
            }

            if (choice >= 1 && choice <= eligibleProjects.size()) {
                break; // valid input
            }

            System.out.println("Invalid choice. Attempts left: " + attempts);
        }

        if (choice <= 0 || choice > eligibleProjects.size()) {
            System.out.println("Too many invalid attempts. Returning to menu.");
            return;
        }

        BTOProject selected = eligibleProjects.get(choice - 1);
        FlatType flatType = EligibilityChecker.chooseFlatType(applicant, selected, scanner);

        if (flatType == null) {
            System.out.println("Flat type selection failed.");
            return;
        }

        try {
            appController.submitApplication(applicant, selected, flatType);
            System.out.println("Application submitted successfully!");
        } catch (Exception e) {
            System.out.println("Failed to submit application: " + e.getMessage());
        }
    }
    
    // Handle Option 3: Request Withdrawal
    protected void handleRequestWithdrawal(Applicant applicant) {
        System.out.print("Are you sure you want to request withdrawal of your application? (yes/no): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();
    
        if (!confirmation.equals("yes")) {
            System.out.println("Withdrawal request cancelled.");
            return;
        }
    
        try {
            // Controller will throw if no application or invalid state
            appController.requestWithdrawal(applicant);
            System.out.println("Withdrawal request has been submitted.");
        } catch (IllegalStateException e) {
            // e.g. “No application found” or “Cannot withdraw in this state”
            System.out.println(e.getMessage());
        }
    }
    

    // Handle Option 4: View Application Status
    protected void handleApplicationStatus(Applicant applicant) {
        Application application = appController.getApplicationByNric(applicant.getNric());

        if (application == null) {
            System.out.println("You do not have any existing applications.");
            return;
        }

        System.out.println("Your application status: " + application.getStatus());
        System.out.println("Project Name: " + application.getProject().getProjectName());
        System.out.println("Flat Type: " + application.getFlatType());
        System.out.println("Request Withdrawal: " + (application.isRequestWithdrawal() ? "Yes" : "No"));
        if (application.getStatus() == ApplicationStatus.SUCCESSFUL) {
            System.out.print("\nWould you like to proceed to flat booking? (yes/no): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes")) {
                try {
                    application.setStatus(ApplicationStatus.PENDING_BOOKING);
                    System.out.println("✅ Booking request submitted. Awaiting officer approval.");
                } catch (Exception e) {
                    System.out.println("❌ " + e.getMessage());
                }
            } else {
                System.out.println("Booking request cancelled.");
            }
        }
    }
}
