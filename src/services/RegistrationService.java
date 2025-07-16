package services;

import entities.BTOProject;
import entities.HDBOfficer;
import entities.Registration;
import entities.HDBManager;
import enums.RegistrationStatus;
import repositories.RegistrationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for officer registrations on projects.
 */
public class RegistrationService {
    private final RegistrationRepository repo;
    private final ApplicationService appService;
    private ProjectService projectService;

    public RegistrationService(RegistrationRepository repo,
                               ApplicationService appService) {
        this.repo       = repo;
        this.appService = appService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }
    /**
     * Officer submits a registration request for a project.
     * @throws IllegalStateException if already registered in this period or already applied as applicant
     */
    public Registration submitRegistration(HDBOfficer officer, BTOProject project) {
        // Rule #1: Cannot register if this officer has an application as an applicant
        boolean hasApplied = appService.getApplicationByNric(officer.getNric()) != null;
        if (hasApplied) {
            throw new IllegalStateException(
                "Cannot register: you have already applied for a BTO project."
            );
        }

        // Rule #2: Only one registration per application window
        List<Registration> approvedRegs = repo.findByOfficer(officer.getNric()).stream()
            .filter(r -> r.getStatus() == RegistrationStatus.APPROVED)
            .toList();

        boolean hasApprovedInWindow = approvedRegs.stream().anyMatch(r ->
            isOverlapping(
                r.getProject().getOpenDate(), r.getProject().getCloseDate(),
                project.getOpenDate(),        project.getCloseDate()
            )
        );

        if (hasApprovedInWindow) {
            throw new IllegalStateException(
                "You already have an approved registration in this application period."
            );
        }

        Registration reg = new Registration(officer, project);
        repo.addRegistration(reg);
        repo.persist();

        return reg;
    }
    
    /**
     * Manager approves or rejects a registration.
     */
    public void reviewRegistration(Registration reg, boolean approve) {
        if (reg.getStatus() != RegistrationStatus.PENDING) {
            throw new IllegalStateException("Registration already reviewed.");
        }
    
        if (approve) {
            // Check that there is at least one slot remaining
            BTOProject proj = reg.getProject();
            if (proj.getAvailableOfficerSlots() <= 0) {
                throw new IllegalStateException("No officer slots remaining on project " 
                                                + proj.getProjectName());
            }
            reg.setStatus(RegistrationStatus.APPROVED);
            // Decrement the slot count
            projectService.decreaseOfficerSlots(proj);
        } else {
            reg.setStatus(RegistrationStatus.REJECTED);
        }
    
        // mark when it was reviewed
        reg.setReviewedAt(LocalDate.now());
    
        // persist both registration *and* project changes
        repo.persist();
    }
    

    /**
     * List registrations by project.
     */
    public List<Registration> listByProject(String projectName, RegistrationStatus statusFilter) {
        return repo.findByProject(projectName).stream()
                   .filter(r -> statusFilter == null || r.getStatus() == statusFilter)
                   .toList();
    }

    private boolean isOverlapping(LocalDate start1, LocalDate end1,
                                  LocalDate start2, LocalDate end2) {
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }

    public Registration findSpecificRegistration(String officerNric, String projectName) {
        return repo.findByOfficer(officerNric).stream()
                   .filter(r -> r.getProject().getProjectName().equalsIgnoreCase(projectName))
                   .findFirst()
                   .orElse(null);
    }
    
    public List<Registration> findByOfficer(String officerNric) {
        return repo.findByOfficer(officerNric);
    }

    /**
     * List all registrations, regardless of status.
     */
    public List<Registration> listAllRegistrations() {
        return repo.findAll();
    }

    public List<Registration> findByManager(HDBManager manager) {
    // gather all projects this manager owns
    return repo.findAll().stream()
        .filter(r -> r.getProject().getManager().equals(manager))
        .collect(Collectors.toList());
    }

}
