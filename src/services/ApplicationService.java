package services;

import entities.*;
import enums.ApplicationStatus;
import enums.FlatType;
import repositories.ApplicationRepository;
import repositories.ProjectRepository;

import java.util.List;

public class ApplicationService {
    private final ApplicationRepository appRepo;

    private final ProjectRepository projectRepo;

    public ApplicationService(ApplicationRepository appRepo, ProjectRepository projectRepo) {
        this.appRepo = appRepo;
        this.projectRepo = projectRepo;
    }

    /**
     * Applicant submits a new application. Enforces one-per-user and eligibility.
     */
    public Application submitApplication(Applicant applicant,
                                         BTOProject project,
                                         FlatType flatType) {
        // 1) Only one application per applicant at a time
        Application existing = appRepo.findByNRIC(applicant.getNric());
        if (existing != null) {
            throw new IllegalStateException("You already have an active application.");
        }

        // 2) Eligibility check (age, marital status, flat type)
        if (!EligibilityChecker.isEligible(applicant, project) ||
            !project.getFlatsAvailable().containsKey(flatType)) {
            throw new IllegalArgumentException("You are not eligible for this project or flat type.");
        }

        // 3) Create and persist
        Application app = new Application(applicant, project, flatType);
        appRepo.addApplication(app);
        appRepo.persist();
        return app;
    }

    /**
     * Applicant requests withdrawal of an existing application.
     */
    public void requestWithdrawal(Applicant applicant) {
        Application app = appRepo.findByNRIC(applicant.getNric());
        if (app == null) {
            throw new IllegalStateException("No application found for NRIC: " + applicant.getNric());
        }
    
        // Only allow withdrawal when currently SUCCESSFUL or BOOKED, and not already requested
        if ((app.getStatus() == ApplicationStatus.SUCCESSFUL 
             || app.getStatus() == ApplicationStatus.BOOKED)
            && !app.isRequestWithdrawal()) {
    
            app.setRequestWithdrawal(true);
        }
        else {
            throw new IllegalStateException("Application is not in a state to withdraw.");
        }
    
        appRepo.persist();
    }
    

    /**
     * Manager approves or rejects an application.
     */
    public void reviewApplication(Application app, boolean approve) {
        if (app.getStatus() != ApplicationStatus.PENDING || app.isRequestWithdrawal()) {
            throw new IllegalStateException("Invalid application state for review.");
        }
        if (approve) {
            if (app.getProject().getFlatsAvailable().get(app.getFlatType()) <= 0) {
                throw new IllegalStateException("No more units available.");
            }
            app.setStatus(ApplicationStatus.SUCCESSFUL);
        } else {
            app.setStatus(ApplicationStatus.UNSUCCESSFUL);
        }
        appRepo.persist();
    }

    /**
     * Officer books a flat for a successful application.
     */
    public Receipt bookFlat(HDBOfficer officer, Application app) {
        if (app.getStatus() != ApplicationStatus.PENDING_BOOKING) {
            throw new IllegalStateException("Application is not ready for booking.");
        }

        BTOProject project = app.getProject();
        FlatType type = app.getFlatType();

        project.removeFlats(type, 1);


        // Update status to BOOKED
        app.setStatus(ApplicationStatus.BOOKED);

        // Save changes
        appRepo.persist();
        projectRepo.persist();

        return new Receipt(app, officer);
    }

    /**
     * List all applications for a given project.
     */
    public List<Application> listByProject(String projectName) {
        return appRepo.findByProject(projectName);
    }

    /**
     * List all pending withdrawals.
     */
    public List<Application> listWithdrawalRequests() {
        return appRepo.findWithdrawalRequests();
    }

    public Application getApplicationByNric(String nric) {
        return appRepo.findByNRIC(nric);
    }

    public void approveWithdrawal(Application app, boolean approve) {
        if (!app.isRequestWithdrawal()) {
            throw new IllegalStateException("No withdrawal request to process.");
        }
    
        if (approve) {
            // If the application was already BOOKED, free up the flat
            if (app.getStatus() == ApplicationStatus.BOOKED) {
                BTOProject project = app.getProject();
                FlatType flatType = app.getFlatType();
                // Return the flat
                project.addFlats(flatType, 1);
                // Also, if you track “booked” count separately, decrement it here.
            }
            // Mark as withdrawn
            app.setStatus(ApplicationStatus.UNSUCCESSFUL);
        } else {
            throw new IllegalStateException("Withdrawal request rejected by manager.");
        }
    
        // Clear the request flag
        app.setRequestWithdrawal(false);
    
        // Persist changes
        appRepo.persist();
    }

    public List<Application> findByBooked() {
        return appRepo.findByBooked();
    }
    

}
