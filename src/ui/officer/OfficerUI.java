package ui.officer;

import controllers.*;
import entities.*;
import enums.ApplicationStatus;
import enums.RegistrationStatus;
import filters.ProjectFilter;
import ui.applicant.ApplicantUI;
import ui.applicant.EnquiryUI;
import ui.applicant.ProjectUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class OfficerUI extends ApplicantUI {
    private final RegistrationController regController;

    public OfficerUI(AuthController authController,
                     ApplicationController appController,
                     EnquiryController enqController,
                     ProjectController projController,
                     RegistrationController regController,
                     Scanner scanner,
                     ProjectFilter projectFilter,
                     ProjectUI projectUI,
                     EnquiryUI enquiryUI) {
        super(authController, appController, enqController,
              projController, scanner, projectFilter,
              projectUI, enquiryUI);

        this.regController = regController;
    }

    @Override
    public void displayMenu() {
        HDBOfficer officer = (HDBOfficer) getAuthController().getCurrentUser();
        int choice;
        do {
            showOfficerMenu();
            choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1 -> handleProjectRegistration(officer);
                case 2 -> super.handleApply(officer);               // same as applicant apply
                case 3 -> super.handleRequestWithdrawal(officer);   // same as applicant
                case 4 -> super.handleApplicationStatus(officer);            // same as applicant
                case 5 -> super.getEnquiryUI().handleEnquire(officer);
                case 6 -> handleViewRegistrations(officer);
                case 7 -> super.getEnquiryUI().handleReplyEnquiries(officer); // inherited
                case 8 -> handleBooking(officer);
                case 9 -> super.logOut();
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 9);
    }



    private void showOfficerMenu() {
        System.out.println("\n=== Officer Menu ===");
        System.out.println("1. Register for a Project");
        System.out.println("2. Apply for a Project");           // inherited
        System.out.println("3. Request Withdrawal");            // inherited
        System.out.println("4. View Application Status");       // inherited
        System.out.println("5. Enquire about a Project");       // inherited
        System.out.println("6. View My Registrations");
        System.out.println("7. Reply To Enquiries");     // inherited
        System.out.println("8. Confirm the Booking for Applicant");     // inherited
        System.out.println("9. Logout");
        System.out.print("Enter choice: ");
    }

    private void handleBooking(HDBOfficer officer) {
        // 1. Get projects this officer is APPROVED for
        List<BTOProject> assignedProjects = regController.findByOfficer(officer.getNric()).stream()
                .filter(r -> r.getStatus() == RegistrationStatus.APPROVED)
                .map(Registration::getProject)
                .collect(Collectors.toList());

        if (assignedProjects.isEmpty()) {
            System.out.println("❌ You are not approved for any project.");
            return;
        }

        // 2. Gather all PENDING_BOOKING applications from these projects
        List<Application> bookings = new ArrayList<>();
        for (BTOProject project : assignedProjects) {
            bookings.addAll(
                    appController.listByProject(project.getProjectName()).stream()
                            .filter(app -> app.getStatus() == ApplicationStatus.PENDING_BOOKING)
                            .toList()
            );
        }

        if (bookings.isEmpty()) {
            System.out.println("✅ No booking requests to process.");
            return;
        }

        // 3. Show applications to process
        System.out.println("\n=== Pending Bookings ===");
        for (int i = 0; i < bookings.size(); i++) {
            Application a = bookings.get(i);
            System.out.printf("[%d] NRIC: %s | Project: %s | Flat: %s%n",
                    i + 1, a.getApplicant().getNric(), a.getProject().getProjectName(), a.getFlatType());
        }

        System.out.print("Select application to confirm booking (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (choice <= 0 || choice > bookings.size()) {
            System.out.println("Cancelled.");
            return;
        }

        // 4. Confirm booking
        Application selected = bookings.get(choice - 1);
        try {
            Receipt receipt = appController.bookFlat(officer, selected);
            System.out.println("✅ Booking successful. Receipt:\n" + receipt.generateText());
        } catch (Exception e) {
            System.out.println("❌ " + (e.getMessage() == null ? "Unknown error." : e.getMessage()));
            e.printStackTrace();
        }
    }

    private void handleProjectRegistration(HDBOfficer officer) {
        // Delegate to controller/service
        List<BTOProject> projects =
            getProjController().getProjectsForOfficers(officer, getProjectFilter());

        if (projects.isEmpty()) {
            System.out.println("No projects available for registration.");
            return;
        }

        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("[%d] %s%n", i+1, projects.get(i).getSummary());
        }
        System.out.print("Choose (0 to cancel): ");
        int idx = scanner.nextInt(); scanner.nextLine();
        if (idx <= 0 || idx > projects.size()) return;

        try {
            regController.submitRegistration(officer, projects.get(idx-1));
            System.out.println("Registration request submitted.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleViewRegistrations(HDBOfficer officer) {
        var regs = regController.findByOfficer(officer.getNric());
        if (regs.isEmpty()) {
            System.out.println("You have no registrations.");
            return;
        }

        System.out.println("\n=== My Registrations ===");
        for (Registration r : regs) {
            System.out.printf("Project: %s | Status: %s%n",
                    r.getProject().getProjectName(),
                    r.getStatus());

            if (r.getStatus() == RegistrationStatus.APPROVED) {
                BTOProject p = r.getProject();
                System.out.println("  → Neighborhood : " + p.getNeighborhood());
                System.out.println("  → Open Date     : " + p.getOpenDate());
                System.out.println("  → Close Date    : " + p.getCloseDate());
                System.out.println("  → Flats Available:");
                p.getFlatsAvailable().forEach((type, count) ->
                        System.out.printf("      • %s : %d%n", type, count)
                );
                System.out.println("  → Selling Price:");
                p.getSellingPrice().forEach((type, price) ->
                        System.out.printf("      • %s : $%,d%n", type, price)
                );
                System.out.println();  // blank line for readability
            }
        }
    }
}
