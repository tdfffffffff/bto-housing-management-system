package ui.applicant;

import controllers.ProjectController;
import entities.Applicant;
import entities.BTOProject;
import enums.FlatType;
import enums.SortOption;
import filters.ProjectFilter;

import java.util.List;
import java.util.Scanner;

public class ProjectUI {
    private final Scanner scanner;
    private final ProjectController projController;
    private final ProjectFilter projectFilter;

    public ProjectUI(Scanner scanner, ProjectController projController, ProjectFilter projectFilter) {
        this.scanner = scanner;
        this.projController = projController;
        this.projectFilter = projectFilter;
    }

    protected void handleViewProjects(Applicant applicant) {
        List<BTOProject> filteredProjects = projController.getProjectsForApplicants(applicant, projectFilter);

        if (filteredProjects.isEmpty()) {
            System.out.println("No projects found matching the current filter settings and your eligibility.");
        } else {
            System.out.println("\n=== Eligible Projects ===");
            for (int i = 0; i < filteredProjects.size(); i++) {
                System.out.printf("[%d] %s\n", i + 1, filteredProjects.get(i).getSummary());
            }
        }
    }

    protected void handleProjectOptions(Applicant applicant) {
        int subChoice;
        do {
            System.out.println("\n=== View Projects ===");
            System.out.println("1. View projects with current filters");
            System.out.println("2. Change filter settings");
            System.out.println("3. Return to main menu");
            System.out.print("Enter your choice: ");

            subChoice = scanner.nextInt();
            scanner.nextLine();

            switch (subChoice) {
                case 1 -> handleViewProjects(applicant);
                case 2 -> changeProjectFilter();
                case 3 -> System.out.println("Returning to main menu.");
                default -> System.out.println("Invalid choice. Please try again.");
            }
        } while (subChoice != 3);
    }

    protected void changeProjectFilter() {
        System.out.println("\n=== Change Filter Settings ===");

        System.out.print("Enter Project Name filter (or press Enter to skip): ");
        String projectName = scanner.nextLine().trim();
        projectFilter.setProjectName(projectName.isEmpty() ? null : projectName);

        System.out.print("Enter Location filter (or press Enter to skip): ");
        String location = scanner.nextLine().trim();
        projectFilter.setLocation(location.isEmpty() ? null : location);

        System.out.println("Choose Flat Type filter (or 0 to skip):");
        FlatType[] flatTypes = FlatType.values();
        for (int i = 0; i < flatTypes.length; i++) {
            System.out.printf("%d. %s\n", i + 1, flatTypes[i]);
        }
        System.out.print("Your choice: ");
        int flatChoice = scanner.nextInt();
        scanner.nextLine();
        projectFilter.setFlatType(flatChoice == 0 ? null : flatTypes[flatChoice - 1]);

        System.out.println("Sort by:");
        System.out.println("1. Project Name ASC");
        System.out.println("2. Project Name DESC");
        System.out.println("3. Location ASC");
        System.out.println("4. Location DESC");
        System.out.print("Your choice: ");
        int sortChoice = scanner.nextInt();
        scanner.nextLine();

        switch (sortChoice) {
            case 2 -> projectFilter.setSortBy(SortOption.PROJECT_DESC);
            case 3 -> projectFilter.setSortBy(SortOption.LOCATION_ASC);
            case 4 -> projectFilter.setSortBy(SortOption.LOCATION_DESC);
            default -> projectFilter.setSortBy(SortOption.PROJECT_ASC);
        }

        System.out.println("Filter updated successfully!");
    }


}
