package ui.applicant;

import controllers.EnquiryController;
import controllers.ProjectController;
import entities.*;
import enums.EnquiryStatus;

import java.util.List;
import java.util.Scanner;

public class EnquiryUI {
    private final Scanner scanner;
    private final EnquiryController enqController;
    private final ProjectController projectController;

    public EnquiryUI(Scanner scanner, EnquiryController enqController, ProjectController projectController) {
        this.scanner = scanner;
        this.enqController = enqController;
        this.projectController = projectController;
    }

    public void handleEnquire(Applicant applicant) {
        int choice;
        do {
            System.out.println("\n=== Project Enquiries ===");
            System.out.println("1. Submit a new enquiry");
            System.out.println("2. View my enquiries");
            System.out.println("3. Edit an enquiry");
            System.out.println("4. Delete an enquiry");
            System.out.println("5. Back to main menu");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> submitNewEnquiry(applicant);
                case 2 -> viewMyEnquiries(applicant);
                case 3 -> editEnquiry(applicant);
                case 4 -> deleteEnquiry(applicant);
                case 5 -> System.out.println("Returning to main menu.");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 5);
    }

    public void handleEnquire(HDBOfficer officer) {
        int choice;
        do {
            System.out.println("\n=== Project Enquiries ===");
            System.out.println("1. Submit a new enquiry");
            System.out.println("2. View my enquiries");
            System.out.println("3. Edit an enquiry");
            System.out.println("4. Delete an enquiry");
            System.out.println("5. Back to main menu");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> submitNewEnquiry(officer);
                case 2 -> viewMyEnquiries(officer);
                case 3 -> editEnquiry(officer);
                case 4 -> deleteEnquiry(officer);
                case 5 -> System.out.println("Returning to main menu.");
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 5);
    }

    private void submitNewEnquiry(Applicant applicant) {
        List<BTOProject> projects = projectController.getProjectsForApplicants(applicant, null);
        if (projects.isEmpty()) {
            System.out.println("No projects available to enquire about.");
            return;
        }

        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, projects.get(i).getProjectName());
        }
        System.out.print("Choose project (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice <= 0 || choice > projects.size()) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.print("Enter your enquiry: ");
        String content = scanner.nextLine().trim();

        if (content.isEmpty()) {
            System.out.println("Cannot submit empty enquiry.");
            return;
        }

        enqController.submitEnquiry(applicant, projects.get(choice - 1), content);
        System.out.println("Enquiry submitted.");
    }

    private void submitNewEnquiry(HDBOfficer officer) {
        List<BTOProject> projects = projectController.getProjectsForOfficers(officer, null);
        if (projects.isEmpty()) {
            System.out.println("No projects available to enquire about.");
            return;
        }

        for (int i = 0; i < projects.size(); i++) {
            System.out.printf("[%d] %s\n", i + 1, projects.get(i).getProjectName());
        }
        System.out.print("Choose project (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice <= 0 || choice > projects.size()) {
            System.out.println("Cancelled.");
            return;
        }

        System.out.print("Enter your enquiry: ");
        String content = scanner.nextLine().trim();

        if (content.isEmpty()) {
            System.out.println("Cannot submit empty enquiry.");
            return;
        }

        enqController.submitEnquiry(officer, projects.get(choice - 1), content);
        System.out.println("Enquiry submitted.");
    }

    private void viewMyEnquiries(Applicant applicant) {
        List<Enquiry> list = enqController.getSubmittedEnquiries(applicant);
        if (list.isEmpty()) {
            System.out.println("You have no enquiries.");
            return;
        }

        for (Enquiry e : list) {
            System.out.printf("ID:%d | Project:%s | Status:%s | Content:%s\n",
                    e.getEnquiryId(), e.getProject().getProjectName(), e.getStatus(), e.getContent());
            if (e.getResponse() != null) {
                System.out.printf("  → Reply: %s (by %s at %s)\n",
                        e.getResponse(), e.getRespondedBy().getName(), e.getRespondedAt());
            }
        }
    }

    private void editEnquiry(Applicant applicant) {
        viewMyEnquiries(applicant);
        System.out.print("Enter ID to edit (0 to cancel): ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Enquiry target = enqController.getSubmittedEnquiries(applicant).stream()
                .filter(e -> e.getEnquiryId() == id)
                .findFirst().orElse(null);

        if (target == null || target.getStatus() == EnquiryStatus.CLOSED) {
            System.out.println("Cannot edit this enquiry.");
            return;
        }

        System.out.print("Enter new content: ");
        String updated = scanner.nextLine().trim();
        if (updated.isEmpty()) {
            System.out.println("Content cannot be empty.");
            return;
        }

        enqController.editEnquiry(applicant, id, updated);
        System.out.println("Enquiry updated.");
    }

    private void deleteEnquiry(Applicant applicant) {
        viewMyEnquiries(applicant);
        System.out.print("Enter ID to delete (0 to cancel): ");
        int id = scanner.nextInt();
        scanner.nextLine();

        Enquiry target = enqController.getSubmittedEnquiries(applicant).stream()
                .filter(e -> e.getEnquiryId() == id)
                .findFirst().orElse(null);

        if (target == null || target.getStatus() == EnquiryStatus.CLOSED) {
            System.out.println("Cannot delete this enquiry.");
            return;
        }

        enqController.deleteEnquiry(applicant, id);
        System.out.println("Enquiry deleted.");
    }

    public void handleReplyEnquiries(HDBOfficer officer) {
        // 1) Fetch open enquiries for projects this officer handles
        List<Enquiry> open = enqController.getEnquiriesOfHandledProject(officer).stream()
            .filter(e -> e.getStatus() != EnquiryStatus.CLOSED)
            .toList();

        if (open.isEmpty()) {
            System.out.println("No open enquiries to reply to.");
            return;
        }

        // 2) Display them
        System.out.println("\n=== Open Enquiries ===");
        open.forEach(e -> {
            System.out.printf("[%d] From: %s | Project: %s%n    \"%s\"%n",
                e.getEnquiryId(),
                e.getApplicant().getName(),
                e.getProject().getProjectName(),
                e.getContent()
            );
        });

        // 3) Prompt for choice
        System.out.print("Enter enquiry ID to reply (0 to cancel): ");
        int id = scanner.nextInt(); 
        scanner.nextLine();  // consume newline
        if (id == 0) return;

        // 4) Find the selected enquiry
        Enquiry target = open.stream()
            .filter(e -> e.getEnquiryId() == id)
            .findFirst()
            .orElse(null);

        if (target == null) {
            System.out.println("Invalid enquiry ID.");
            return;
        }

        // 5) Read reply content
        System.out.print("Enter your reply: ");
        String reply = scanner.nextLine().trim();
        if (reply.isEmpty()) {
            System.out.println("Reply cannot be empty.");
            return;
        }

        // 6) Delegate to controller
        try {
            enqController.replyToEnquiry(officer, id, reply);
            System.out.println("Reply sent successfully.");
        } catch (Exception ex) {
            System.out.println("Failed to send reply: " + ex.getMessage());
        }
    }

    public void handleReplyEnquiries(HDBManager manager) {
        // 1) Fetch open enquiries for projects this officer handles
        List<Enquiry> open = enqController.getEnquiriesOfHandledProject(manager).stream()
                .filter(e -> e.getStatus() != EnquiryStatus.CLOSED)
                .toList();

        if (open.isEmpty()) {
            System.out.println("No open enquiries to reply to.");
            return;
        }

        // 2) Display them
        System.out.println("\n=== Open Enquiries ===");
        open.forEach(e -> {
            System.out.printf("[%d] From: %s | Project: %s%n    \"%s\"%n",
                    e.getEnquiryId(),
                    e.getApplicant().getName(),
                    e.getProject().getProjectName(),
                    e.getContent()
            );
        });

        // 3) Prompt for choice
        System.out.print("Enter enquiry ID to reply (0 to cancel): ");
        int id = scanner.nextInt();
        scanner.nextLine();  // consume newline
        if (id == 0) return;

        // 4) Find the selected enquiry
        Enquiry target = open.stream()
                .filter(e -> e.getEnquiryId() == id)
                .findFirst()
                .orElse(null);

        if (target == null) {
            System.out.println("Invalid enquiry ID.");
            return;
        }

        // 5) Read reply content
        System.out.print("Enter your reply: ");
        String reply = scanner.nextLine().trim();
        if (reply.isEmpty()) {
            System.out.println("Reply cannot be empty.");
            return;
        }

        // 6) Delegate to controller
        try {
            enqController.replyToEnquiry(manager, id, reply);
            System.out.println("Reply sent successfully.");
        } catch (Exception ex) {
            System.out.println("Failed to send reply: " + ex.getMessage());
        }
    }

}