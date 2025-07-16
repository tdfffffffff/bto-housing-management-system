package entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class Receipt {
    private final Application application;
    private final HDBOfficer issuedBy;
    private final LocalDate dateIssued;
    private final LocalTime timeIssued;
    
    public Receipt(Application application, HDBOfficer issuedBy) {
        this.application = application;
        this.issuedBy = issuedBy;
        this.dateIssued = LocalDate.now();
        this.timeIssued = LocalTime.now();
    }

    // Getters Methods
    public Application getApplication() {
        return application;
    }

    public HDBOfficer getIssuedBy() {
        return issuedBy;
    }

    public LocalDate getDateIssued() {
        return dateIssued;
    }

    public LocalTime getTimeIssued() {
        return timeIssued;
    }

    // Other Method(s)
    public String generateText() {
        return String.format(
            "=== BTO Application Receipt ===\n" +
            "Date Issued: %s\n" +
            "Time Issued: %s\n" +
            "Applicant Name: %s\n" +
            "Applicant NRIC: %s\n" +
            "Applicant Age: %d\n" +
            "Applicant Marital Status: %s\n" +
            "Project: %s\n" +
            "Flat Type: %s\n" +
            "Issued By: %s\n" +
            "===============================",
            dateIssued,
            timeIssued,
            application.getApplicant().getName(),
            application.getApplicant().getNric(),
            application.getApplicant().getAge(),
            application.getApplicant().getMaritalStatus(),
            application.getProject().getProjectName(),
            application.getFlatType(),
            issuedBy.getName()
        );
    }
}
