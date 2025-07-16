package ui.manager;

import controllers.*;
import entities.*;
import enums.*;
import filters.ProjectFilter;
import ui.BaseUserUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ManagerUI extends BaseUserUI {
    private final RegistrationController regController;

    public ManagerUI(AuthController authController,
                     ApplicationController appController,
                     EnquiryController enqController,
                     ProjectController projController,
                     RegistrationController regController,
                     Scanner scanner,
                     ProjectFilter projectFilter) 
    {
        super(authController, appController, enqController, projController, scanner, projectFilter);
        this.regController = regController;
    }

    @Override
    public void displayMenu() {
        HDBManager manager = (HDBManager) authController.getCurrentUser();
        int choice;
        do {
            System.out.println("\n=== Manager Menu ===");
            System.out.println("1. Create Project");
            System.out.println("2. Edit Project");
            System.out.println("3. Delete Project");
            System.out.println("4. Toggle Project Visibility");
            System.out.println("5. View All Projects");
            System.out.println("6. View My Projects");
            System.out.println("7. Handle Officer Registrations");
            System.out.println("8. Handle Applications");
            System.out.println("9. Handle Withdrawals");
            System.out.println("10. Generate Report");
            System.out.println("11. View/Reply to Enquiries");
            System.out.println("12. Logout");
            System.out.print  ("Enter choice: ");
            choice = scanner.nextInt(); scanner.nextLine();

            switch (choice) {
                case 1  -> handleCreateProject(manager);
                case 2  -> handleEditProject(manager);
                case 3  -> handleDeleteProject(manager);
                case 4  -> handleToggleVisibility(manager);
                case 5  -> handleViewAllProjects(manager);
                case 6  -> handleViewMyProjects(manager);
                case 7  -> handleOfficerRegistrations(manager);
                case 8  -> handleApplications(manager);
                case 9  -> handleWithdrawals(manager);
                case 10 -> handleGenerateReport(manager);
                case 11 -> handleReplyEnquiries(manager);
                case 12 -> logOut();
                default -> System.out.println("Invalid choice.");
            }
        } while (choice != 12);
    }

    private void handleCreateProject(HDBManager manager) {
        System.out.println("\n=== Create Project ===");
        try {
            System.out.print("Project Name: ");
            String projectName = scanner.nextLine().trim();
    
            System.out.print("Neighborhood: ");
            String neighborhood = scanner.nextLine().trim();
    
            // Gather flat quotas and prices for each fixed FlatType
            Map<FlatType, Integer> flatsAvailable = new EnumMap<>(FlatType.class);
            Map<FlatType, Integer> sellingPrice   = new EnumMap<>(FlatType.class);
    
            System.out.printf("Available units for %s: ", FlatType.TWO_ROOM);
            flatsAvailable.put(FlatType.TWO_ROOM, Integer.parseInt(scanner.nextLine().trim()));
            System.out.printf("Selling price for %s:    ", FlatType.TWO_ROOM);
            sellingPrice.put(FlatType.TWO_ROOM, Integer.parseInt(scanner.nextLine().trim()));
    
            System.out.printf("Available units for %s: ", FlatType.THREE_ROOM);
            flatsAvailable.put(FlatType.THREE_ROOM, Integer.parseInt(scanner.nextLine().trim()));
            System.out.printf("Selling price for %s:    ", FlatType.THREE_ROOM);
            sellingPrice.put(FlatType.THREE_ROOM, Integer.parseInt(scanner.nextLine().trim()));
    
            System.out.print("Officer slots (max 10): ");
            int officerSlots = Integer.parseInt(scanner.nextLine().trim());
    
            System.out.print("Opening date (yyyy-MM-dd): ");
            LocalDate openDate = LocalDate.parse(scanner.nextLine().trim());
    
            System.out.print("Closing date (yyyy-MM-dd): ");
            LocalDate closeDate = LocalDate.parse(scanner.nextLine().trim());
    
            // Determine initial visibility based on dates
            VisibilityStatus visibility = 
                (LocalDate.now().isBefore(openDate) || LocalDate.now().isAfter(closeDate))
                ? VisibilityStatus.HIDDEN
                : VisibilityStatus.VISIBLE;
    
            // Delegate to controller
            projController.createProject(
                manager,
                projectName,
                neighborhood,
                flatsAvailable,
                sellingPrice,
                visibility,
                officerSlots,
                openDate,
                closeDate
            );
    
            System.out.println("Project created successfully!");
        } catch (Exception e) {
            System.out.println("Failed to create project: " + e.getMessage());
        }
    }

    private void handleEditProject(HDBManager m) {
        BTOProject p = pickOne(
          projController.getProjectsForManager(m),
          BTOProject::getSummary
        );
        if (p == null) {
            System.out.println("Cancelled.");
            return;
        }
    
        System.out.println("\n=== Editing “" + p.getProjectName() + "” ===");
        try {
            // 1) Name & Neighborhood
            System.out.print("Project Name [" + p.getProjectName() + "]: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = p.getProjectName();
    
            System.out.print("Neighborhood [" + p.getNeighborhood() + "]: ");
            String nb = scanner.nextLine().trim();
            if (nb.isEmpty()) nb = p.getNeighborhood();
    
            // 2) Flat quotas & prices
            Map<FlatType,Integer> flatsAvailable = new EnumMap<>(p.getFlatsAvailable());
            Map<FlatType,Integer> sellingPrice   = new EnumMap<>(p.getSellingPrice());
            for (FlatType ft : FlatType.values()) {
                // units
                System.out.printf("Units for %s [%d]: ", ft, flatsAvailable.get(ft));
                String ua = scanner.nextLine().trim();
                if (!ua.isEmpty()) {
                    while (!ua.matches("\\d+")) {
                        System.out.print(" Invalid, enter a non-negative integer: ");
                        ua = scanner.nextLine().trim();
                    }
                    flatsAvailable.put(ft, Integer.parseInt(ua));
                }
                // price
                System.out.printf("Price for %s [%d]: ", ft, sellingPrice.get(ft));
                String pr = scanner.nextLine().trim();
                if (!pr.isEmpty()) {
                    while (!pr.matches("\\d+")) {
                        System.out.print(" Invalid, enter a non-negative integer: ");
                        pr = scanner.nextLine().trim();
                    }
                    sellingPrice.put(ft, Integer.parseInt(pr));
                }
            }
    
            // 3) Officer slots (1–10)
            System.out.printf("Officer slots [%d]: ", p.getAvailableOfficerSlots());
            String os = scanner.nextLine().trim();
            int slots = p.getAvailableOfficerSlots();
            if (!os.isEmpty()) {
                while (!os.matches("\\d+") || Integer.parseInt(os) < 1 || Integer.parseInt(os) > 10) {
                    System.out.print(" Must be 1 to 10. Try again: ");
                    os = scanner.nextLine().trim();
                }
                slots = Integer.parseInt(os);
            }
    
            // 4) Dates
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
    
            // Open date
            LocalDate openDate = p.getOpenDate();
            System.out.print("Opening date [" + openDate.format(fmt) + "] (yyyy-MM-dd): ");
            String od = scanner.nextLine().trim();
            if (!od.isEmpty()) {
                openDate = LocalDate.parse(od, fmt);
            }
    
            // Close date
            LocalDate closeDate = p.getCloseDate();
            System.out.print("Closing date [" + closeDate.format(fmt) + "] (yyyy-MM-dd): ");
            String cd = scanner.nextLine().trim();
            if (!cd.isEmpty()) {
                LocalDate candidate = LocalDate.parse(cd, fmt);
                while (!candidate.isAfter(openDate)) {
                    System.out.print("  ↪️ Must be after opening date (" 
                        + openDate.format(fmt) + "). Try again: ");
                    cd = scanner.nextLine().trim();
                    candidate = LocalDate.parse(cd, fmt);
                }
                closeDate = candidate;
            }
    
            // 5) Visibility
            System.out.printf("Visibility [%s] (VISIBLE/HIDDEN): ",
                              p.getVisibilityStatus());
            String vs = scanner.nextLine().trim().toUpperCase();
            VisibilityStatus visibility = vs.isEmpty()
                ? p.getVisibilityStatus()
                : VisibilityStatus.valueOf(vs);
    
            // 6) Commit
            projController.editProject(
                m, p,
                name, nb,
                flatsAvailable, sellingPrice,
                visibility,
                slots,
                openDate, closeDate
            );
            System.out.println("Project updated successfully!");
        } catch (Exception e) {
            System.out.println("Failed to update project: " + e.getMessage());
        }
    }
    


    private void handleDeleteProject(HDBManager m) {
        List<BTOProject> mine = projController.getProjectsForManager(m);
        if (mine.isEmpty()) {
            System.out.println("No projects to delete.");
            return;
        }
        System.out.println("\n=== Delete Project ===");
        for (int i = 0; i < mine.size(); i++)
            System.out.printf("[%d] %s\n", i+1, mine.get(i).getProjectName());
        System.out.print("Choose (0 to cancel): ");
        int idx = scanner.nextInt(); scanner.nextLine();
        if (idx <= 0 || idx > mine.size()) return;

        BTOProject p = mine.get(idx-1);
        projController.deleteProject(m, p);
        System.out.println("Deleted.");
    }

    private void handleToggleVisibility(HDBManager m) {
        List<BTOProject> mine = projController.getProjectsForManager(m);
        if (mine.isEmpty()) {
            System.out.println("No projects.");
            return;
        }
        System.out.println("\n=== Toggle Visibility ===");
        for (int i = 0; i < mine.size(); i++)
            System.out.printf("[%d] %s (%s)\n", i+1,
                mine.get(i).getProjectName(), mine.get(i).getVisibilityStatus());
        System.out.print("Choose (0 to cancel): ");
        int idx = scanner.nextInt(); scanner.nextLine();
        if (idx <= 0 || idx > mine.size()) return;

        projController.toggleVisibility(m, mine.get(idx-1));
        System.out.println("Toggled.");
    }

    private void handleViewAllProjects(HDBManager manager) {
        System.out.println("\n=== All Projects ===");
        projController.getAllProjects().forEach(p -> System.out.println(p.getSummary()));
    }

    private void handleViewMyProjects(HDBManager m) {
        System.out.println("\n=== My Projects ===");
        projController.getProjectsForManager(m).forEach(p -> System.out.println(p.getSummary()));
    }

    private void handleOfficerRegistrations(HDBManager manager) {
        while (true) {
            System.out.println("\n=== Officer Registrations Menu ===");
            System.out.println("1. All registrations");
            System.out.println("2. Registrations under my projects");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            int scope = Integer.parseInt(scanner.nextLine().trim());
            if (scope == 0) return;
    
            // pick status filter
            System.out.println("\n-- Status Filter --");
            System.out.println("1. Pending only");
            System.out.println("2. Approved only");
            System.out.println("3. All");
            System.out.print("Choice: ");
            int statusChoice = Integer.parseInt(scanner.nextLine().trim());
    
            // fetch list
            List<Registration> regs;
            if (scope == 1) {
                regs = regController.listAllRegistrations();
            } else {
                regs = regController.findByManager(manager);
            }
    
            // apply status filter
            regs = regs.stream()
                .filter(r -> {
                    if (statusChoice == 1) return r.getStatus() == RegistrationStatus.PENDING;
                    if (statusChoice == 2) return r.getStatus() == RegistrationStatus.APPROVED;
                    return true;
                })
                .toList();
    
            if (regs.isEmpty()) {
                System.out.println("No registrations found for those criteria.");
                continue;
            }
    
            // show list
            System.out.println("\n=== Matching Registrations ===");
            for (int i = 0; i < regs.size(); i++) {
                Registration r = regs.get(i);
                System.out.printf("[%d] %s | %s | %s%n",
                    i + 1,
                    r.getOfficer().getName(),
                    r.getProject().getProjectName(),
                    r.getStatus());
            }
            System.out.print("Select one to process (0 to back): ");
            int idx = Integer.parseInt(scanner.nextLine().trim());
            if (idx == 0) continue;
            if (idx < 1 || idx > regs.size()) {
                System.out.println("Invalid selection.");
                continue;
            }
    
            Registration chosen = regs.get(idx - 1);
            System.out.print("Approve (y) or Reject (n)? ");
            boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
            try {
                regController.reviewRegistration(chosen, approve);
                System.out.println(approve ? "Approved." : "Rejected.");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            // after processing one, ask if manager wants to continue
            System.out.print("Process another? (y/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                return;
            }
        }
    }
    
    private void handleApplications(HDBManager manager) {
        // 1) Fetch only the projects this manager is in charge of
        List<BTOProject> myProjects = projController.getProjectsForManager(manager);
    
        // 2) Gather all pending applications under those projects
        List<Application> pending = new ArrayList<>();
        for (BTOProject project : myProjects) {
            List<Application> apps = appController.listByProject(project.getProjectName())
                .stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
                .toList();
            pending.addAll(apps);
        }
    
        if (pending.isEmpty()) {
            System.out.println("No pending applications for your projects.");
            return;
        }
    
        // 3) Group by project name in sorted order
        Map<String, List<Application>> byProject = pending.stream()
            .collect(Collectors.groupingBy(
                a -> a.getProject().getProjectName(),
                TreeMap::new,       // ensures projects come out alphabetically
                Collectors.toList()
            ));
    
        // 4) Display them with a flat index for selection
        System.out.println("\n=== Pending Applications by Project ===");
        List<Application> flatList = new ArrayList<>();
        int idx = 1;
        for (var entry : byProject.entrySet()) {
            System.out.println("\nProject: " + entry.getKey());
            for (Application app : entry.getValue()) {
                System.out.printf("  [%d] NRIC: %s | Flat: %s%n",
                    idx, app.getApplicant().getNric(), app.getFlatType());
                flatList.add(app);
                idx++;
            }
        }
    
        // 5) Prompt manager to choose one
        System.out.print("Choose application to process (0 to cancel): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());
        if (choice <= 0 || choice > flatList.size()) {
            System.out.println("Cancelled.");
            return;
        }
        Application selected = flatList.get(choice - 1);
    
        // 6) Approve or reject
        System.out.print("Approve? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        try {
            appController.reviewApplication(selected, approve);
            System.out.println(approve ? "Approved." : "Rejected.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void handleWithdrawals(HDBManager manager) {
        // 1) Fetch only the projects this manager is in charge of
        List<BTOProject> myProjects = projController.getProjectsForManager(manager);
    
        // 2) Gather all withdrawal‐requested applications under those projects
        List<Application> withdrawals = new ArrayList<>();
        for (BTOProject project : myProjects) {
            List<Application> apps = appController.listByProject(project.getProjectName())
                .stream()
                .filter(Application::isRequestWithdrawal)
                .collect(Collectors.toList());
            withdrawals.addAll(apps);
        }
    
        if (withdrawals.isEmpty()) {
            System.out.println("No withdrawal requests for your projects.");
            return;
        }
    
        // 3) Group by project name alphabetically
        Map<String, List<Application>> byProject = withdrawals.stream()
            .collect(Collectors.groupingBy(
                a -> a.getProject().getProjectName(),
                TreeMap::new,
                Collectors.toList()
            ));
    
        // 4) Print them out with a flat index
        System.out.println("\n=== Withdrawal Requests by Project ===");
        List<Application> flatList = new ArrayList<>();
        int idx = 1;
        for (var entry : byProject.entrySet()) {
            System.out.println("\nProject: " + entry.getKey());
            for (Application a : entry.getValue()) {
                System.out.printf("  [%d] NRIC: %s | Status: %s | Booked? %b%n",
                    idx, a.getApplicant().getNric(), a.getStatus(),
                    a.getStatus() == ApplicationStatus.BOOKED);
                flatList.add(a);
                idx++;
            }
        }
    
        // 5) Prompt manager to choose one
        System.out.print("Choose request to process (0 to cancel): ");
        int choice = Integer.parseInt(scanner.nextLine().trim());
        if (choice <= 0 || choice > flatList.size()) {
            System.out.println("Cancelled.");
            return;
        }
        Application selected = flatList.get(choice - 1);
    
        // 6) Approve or reject
        System.out.print("Approve withdrawal? (y/n): ");
        boolean approve = scanner.nextLine().trim().equalsIgnoreCase("y");
        try {
            appController.approveWithdrawal(selected, approve);
            System.out.println(approve ? "Withdrawn." : "Request denied.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void handleGenerateReport(HDBManager m) {
        System.out.println("\n=== Booking Report ===");
        List<Application> booked = appController.listAllBooked();
        if (booked.isEmpty()) {
            System.out.println("No bookings yet.");
            return;
        }

        //Ask which filters to apply
        System.out.println("Apply filters—enter Y to enable, N to skip:");
        boolean doMarital = promptYesNo("  • Marital status?");
        final MaritalStatus msFilter = doMarital
                ? promptEnumChoice("Select marital status:", MaritalStatus.values())
                : null;

        boolean doFlat = promptYesNo("  • Flat type?");
        final FlatType ftFilter = doFlat
                ? promptEnumChoice("Select flat type:", FlatType.values())
                : null;

        boolean doAge = promptYesNo("  • Age range?");
        final int minAge, maxAge;
        if (doAge) {
            System.out.print("  • Minimum age: ");
            minAge = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("  • Maximum age: ");
            maxAge = Integer.parseInt(scanner.nextLine().trim());
        } else {
            minAge = 0;
            maxAge = Integer.MAX_VALUE;
        }

        //Apply all enabled filters in sequence
        List<Application> filtered = booked.stream()
                .filter(a -> !doMarital || a.getApplicant().getMaritalStatus() == msFilter)
                .filter(a -> !doFlat    || a.getFlatType() == ftFilter)
                .filter(a -> {
                    int age = a.getApplicant().getAge();
                    return !doAge || (age >= minAge && age <= maxAge);
                })
                .toList();

        // Display
        if (filtered.isEmpty()) {
            System.out.println("No bookings match your filters.");
            return;
        }
        System.out.println("\n=== Filtered Booking Report ===");
        filtered.forEach(a -> System.out.printf(
                "%s | age: %d | %s | project: %s%n",
                a.getApplicant().getNric(),
                a.getApplicant().getAge(),
                a.getFlatType(),
                a.getProject().getProjectName()
        ));
    }

    /**
     * Prompt a yes/no question, returning true for Y and false for N.
     */
    private boolean promptYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String in = scanner.nextLine().trim().toUpperCase();
            if (in.equals("Y")) return true;
            if (in.equals("N")) return false;
            System.out.println("Please enter Y or N.");
        }
    }

    /**
     * Prompt the user to pick one of the given enum constants by number.
     */
    private <E extends Enum<E>> E promptEnumChoice(String prompt, E[] options) {
        System.out.println(prompt);
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, options[i]);
        }
        while (true) {
            System.out.print("Enter choice (1–" + options.length + "): ");
            String in = scanner.nextLine().trim();
            try {
                int idx = Integer.parseInt(in);
                if (idx >= 1 && idx <= options.length) {
                    return options[idx - 1];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid selection.");
        }
    }
    
    private void handleReplyEnquiries(HDBManager manager) {
        // 1) Build two lists up front:
        List<Enquiry> allEnquiries = enqController.getAllEnquiries(manager).stream()
            .filter(e -> e.getStatus() == EnquiryStatus.OPEN)
            .collect(Collectors.toList());
    
        // Projects this manager owns
        Set<BTOProject> myProjects = projController.getProjectsForManager(manager).stream()
            .collect(Collectors.toSet());
    
        // Only those open enquiries under their projects
        List<Enquiry> myEnquiries = allEnquiries.stream()
            .filter(e -> myProjects.contains(e.getProject()))
            .collect(Collectors.toList());
    
        // 2) Show submenu
        System.out.println("\n=== Enquiry Menu ===");
        System.out.println("1. View all open enquiries");
        System.out.println("2. Reply to an enquiry under your projects");
        System.out.print  ("Enter choice: ");
    
        int choice = scanner.nextInt();
        scanner.nextLine();
    
        switch (choice) {
            case 1 -> {
                if (allEnquiries.isEmpty()) {
                    System.out.println("No open enquiries.");
                } else {
                    System.out.println("\n=== All Open Enquiries ===");
                    allEnquiries.forEach(e ->
                        System.out.printf("[%d] From:%s | Project:%s | \"%s\"\n",
                            e.getEnquiryId(),
                            e.getApplicant().getNric(),
                            e.getProject().getProjectName(),
                            e.getContent())
                    );
                }
            }
    
            case 2 -> {
                if (myEnquiries.isEmpty()) {
                    System.out.println("No open enquiries under your projects.");
                    return;
                }
                System.out.println("\n=== Your Projects’ Open Enquiries ===");
                myEnquiries.forEach(e ->
                    System.out.printf("[%d] From:%s | Project:%s | \"%s\"\n",
                        e.getEnquiryId(),
                        e.getApplicant().getNric(),
                        e.getProject().getProjectName(),
                        e.getContent())
                );
    
                System.out.print("Enter enquiry ID to reply (0 to cancel): ");
                int id = scanner.nextInt();
                scanner.nextLine();
                if (id == 0) return;
    
                // find in myEnquiries
                Enquiry toReply = myEnquiries.stream()
                    .filter(e -> e.getEnquiryId() == id)
                    .findFirst()
                    .orElse(null);
    
                if (toReply == null) {
                    System.out.println("Invalid ID or not authorized.");
                    return;
                }
    
                System.out.print("Enter your reply: ");
                String reply = scanner.nextLine().trim();
    
                try {
                    enqController.replyToEnquiry(manager, toReply.getEnquiryId(), reply);
                    System.out.println("Reply sent.");
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
    
            default -> System.out.println("Invalid choice.");
        }
    }
    

    private <T> T pickOne(List<T> items, Function<T,String> summary) {
        if (items.isEmpty()) {
            System.out.println("  (none)");
            return null;
        }
        // print the numbered list
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("  %2d) %s%n", i + 1, summary.apply(items.get(i)));
        }
        System.out.print("Choose (0 to cancel): ");

        int choice;
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                choice = Integer.parseInt(line);
                if (choice >= 0 && choice <= items.size()) break;
            } catch (NumberFormatException ignored) {}
            System.out.print("Invalid—enter a number between 0 and " + items.size() + ": ");
        }

        return choice == 0 ? null : items.get(choice - 1);
    }
}
