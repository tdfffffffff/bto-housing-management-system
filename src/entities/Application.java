package entities;

import enums.ApplicationStatus;
import enums.FlatType;

public class Application {
    private Applicant applicant;
    private BTOProject project;
    private FlatType flatType;
    private ApplicationStatus status;
    private boolean requestWithdrawal;

    // Constructor for creating a new application
    public Application(Applicant applicant, BTOProject project, FlatType flatType) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = ApplicationStatus.PENDING; // Default status when creating a new application
        this.requestWithdrawal = false; // Default value for withdrawal request
    }

    // Loading from CSV
    public Application(Applicant applicant, BTOProject project, FlatType flatType, ApplicationStatus status, boolean requestWithdrawal) {
        this.applicant = applicant;
        this.project = project;
        this.flatType = flatType;
        this.status = status;
        this.requestWithdrawal = requestWithdrawal;
    }

    // Getters Methods
    public Applicant getApplicant() {
        return applicant;
    }

    public BTOProject getProject() {
        return project;
    }

    public FlatType getFlatType() {
        return flatType;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public boolean isRequestWithdrawal() {
        return requestWithdrawal;
    }
    
    // Setters Methods
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setRequestWithdrawal(boolean requestWithdrawal) {
        this.requestWithdrawal = requestWithdrawal;
    }
}
