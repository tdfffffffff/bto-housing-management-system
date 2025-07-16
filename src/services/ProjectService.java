package services;

import entities.Applicant;
import entities.BTOProject;
import entities.HDBManager;
import entities.HDBOfficer;
import enums.FlatType;
import enums.VisibilityStatus;
import filters.ProjectFilter;
import repositories.ProjectRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectService {
    private final ProjectRepository repo;
    private final RegistrationService registrationService;

    public ProjectService(ProjectRepository repo, RegistrationService registrationService) {
        this.registrationService = registrationService;
        this.repo = repo;
    }

    /**
     * Returns the list of projects visible to the current user,
     * filtered by the given filter and sorted appropriately.
     * Officers see all projects; Applicants see only visible ones.
     */
    public List<BTOProject> getProjectsForApplicant(Applicant applicant,
                                                    ProjectFilter filter) {
        // 0) if filter is null, fall back to a no‐op filter
        List<BTOProject> candidates =
                (filter == null)
                        ? repo.findAll()                        // get everything
                        : repo.findFilteredProjects(filter);    // apply the filter

        // 1) visibility + flat‐eligibility
        List<BTOProject> base = candidates.stream()
                .filter(p -> p.getVisibilityStatus() == VisibilityStatus.VISIBLE)
                .filter(p -> EligibilityChecker.isEligible(applicant, p))
                .collect(Collectors.toList());

        // 2) officer‐specific rule
        if (applicant instanceof HDBOfficer officer) {
            return base.stream()
                    .filter(p ->
                            registrationService
                                    .findSpecificRegistration(officer.getNric(), p.getProjectName())
                                    == null
                    )
                    .collect(Collectors.toList());
        }

        // 3) true applicants
        return base;
    }

    public List<BTOProject> getProjectsForOfficer(HDBOfficer officer) {
        return repo.findVisible();
    }


//    public List<BTOProject> getProjectsForOfficer(HDBOfficer officer,
//                                                    ProjectFilter filter) {
//        // 0) if filter is null, fall back to a no‐op filter
//        List<BTOProject> candidates =
//                (filter == null)
//                        ? repo.findAll()                        // get everything
//                        : repo.findFilteredProjects(filter);    // apply the filter
//
//        // 1) visibility + flat‐eligibility
//        List<BTOProject> base = candidates.stream()
//                .filter(p -> p.getVisibilityStatus() == VisibilityStatus.VISIBLE)
//                .filter(p -> EligibilityChecker.isEligible(applicant, p))
//                .collect(Collectors.toList());
//
//        // 2) officer‐specific rule
//        if (applicant instanceof HDBOfficer officer) {
//            return base.stream()
//                    .filter(p ->
//                            registrationService
//                                    .findSpecificRegistration(officer.getNric(), p.getProjectName())
//                                    == null
//                    )
//                    .collect(Collectors.toList());
//        }
//
//        // 3) true applicants
//        return base;
//    }



    /**
     * Create a new project under this manager.
     * Validates name uniqueness and application dates.
     */
    public BTOProject createProject(HDBManager manager,
                                    String projectName,
                                    String neighborhood,
                                    Map<FlatType, Integer> flatsAvailable,
                                    Map<FlatType, Integer> sellingPrice,
                                    VisibilityStatus visibilityStatus,
                                    int officerSlots,
                                    LocalDate openDate,
                                    LocalDate closeDate) {
        // 1) Name must be unique
        if (repo.findAll().stream()
                .anyMatch(p -> p.getProjectName().equalsIgnoreCase(projectName))) {
            throw new IllegalArgumentException("Project name already exists");
        }

        // 2) Date consistency
        if (closeDate.isBefore(openDate)) {
            throw new IllegalArgumentException("Closing date must be after opening date");
        }
        // 3) **No overlap with this manager’s existing projects**
        List<BTOProject> mine = repo.findByManager(manager.getNric());
        for (BTOProject existing : mine) {
            LocalDate exOpen  = existing.getOpenDate();
            LocalDate exClose = existing.getCloseDate();
            boolean overlaps = !(closeDate.isBefore(exOpen) || openDate.isAfter(exClose));
            if (overlaps) {
                throw new IllegalArgumentException(
                        String.format("New project window [%s–%s] overlaps with your existing project “%s” [%s–%s].",
                                openDate, closeDate,
                                existing.getProjectName(),
                                exOpen, exClose)
                );
            }
        }
        // 4) Construct and persist
        BTOProject proj = new BTOProject(
            projectName, neighborhood, flatsAvailable, sellingPrice,
            visibilityStatus, openDate, closeDate, officerSlots, manager
        );
        repo.addProject(proj);
        repo.persist();
        return proj;
    }

    /**
     * Edit an existing project. Only the creating manager may do this.
     */
    public void editProject(HDBManager manager,
                            BTOProject project,
                            String projectName,
                            String neighborhood,
                            Map<FlatType, Integer> flatsAvailable,
                            Map<FlatType, Integer> sellingPrice,
                            VisibilityStatus visibilityStatus,
                            int officerSlots,
                            LocalDate openDate,
                            LocalDate closeDate) {
        if (!project.getManager().equals(manager)) {
            throw new IllegalArgumentException("Only the assigned manager can edit this project");
        }
        if (closeDate.isBefore(openDate)) {
            throw new IllegalArgumentException("Closing date must be after opening date");
        }
        project.setProjectName(projectName);
        project.setNeighborhood(neighborhood);
        project.setFlatsAvailable(flatsAvailable);
        project.setSellingPrice(sellingPrice);
        project.setVisibilityStatus(visibilityStatus);
        project.setAvailableOfficerSlots(officerSlots);
        project.setOpenDate(openDate);
        project.setCloseDate(closeDate);

        repo.persist();
    }

    /**
     * Delete a project. Only the creating manager may delete.
     */
    public void deleteProject(HDBManager manager, BTOProject project) {
        if (!project.getManager().equals(manager)) {
            throw new IllegalArgumentException("Only the assigned manager can delete this project");
        }
        repo.removeProject(project);
        repo.persist();
    }

    /**
     * Toggle visibility on/off. Only the creating manager may toggle.
     */
    public void toggleVisibility(HDBManager manager, BTOProject project) {
        if (!project.getManager().equals(manager)) {
            throw new IllegalArgumentException("Only the assigned manager can toggle visibility");
        }
        if (project.getVisibilityStatus() == VisibilityStatus.VISIBLE) {
            project.setVisibilityStatus(VisibilityStatus.HIDDEN);
        } else {
            project.setVisibilityStatus(VisibilityStatus.VISIBLE);
        }
        repo.persist();
    }

    /**
     * Get a projects by manager.
     */
    public List<BTOProject> getProjectsForManager(HDBManager manager) {
        return repo.findByManager(manager.getNric());
    }

    /**
     * Get all projects
     */
    public List<BTOProject> getAllProjects() {
        return repo.findAll();
    }

    public void decreaseOfficerSlots(BTOProject project) {
        if (project.getAvailableOfficerSlots() > 0) {
            project.setAvailableOfficerSlots(project.getAvailableOfficerSlots() - 1);
        } else {
            throw new IllegalStateException("No available officer slots left.");
        }
        repo.persist();
    }

}
