package entities;

import enums.RegistrationStatus;
import java.time.LocalDate;

public class Registration {
    private HDBOfficer officer;
    private BTOProject project;
    private RegistrationStatus status;
    private LocalDate submittedAt;
    private LocalDate reviewedAt;

    // Constructor for creating new registration
    public Registration(HDBOfficer officer, BTOProject project) {
        this.officer = officer;
        this.project = project;
        this.status = RegistrationStatus.PENDING; // Default status when creating a new registration
        this.submittedAt = LocalDate.now(); // Set to current date
        this.reviewedAt = null; // Not reviewed yet 
    }

    // Constructor for loading
    public Registration(HDBOfficer officer, BTOProject project, RegistrationStatus status, LocalDate submittedAt, LocalDate reviewedAt) {
        this.officer = officer;
        this.project = project;
        this.status = status;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
    }

    // Getters Methods
    public HDBOfficer getOfficer() {
        return officer;
    }

    public BTOProject getProject() {
        return project;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public LocalDate getSubmittedAt() {
        return submittedAt;
    }

    public LocalDate getReviewedAt() {
        return reviewedAt;
    }

    // Setters Method(s)
    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public void setReviewedAt(LocalDate reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
