package entities;

import enums.EnquiryStatus;

import java.time.LocalDateTime;

public class Enquiry {
    private static int counter = 1;
    private int enquiryId;
    private Applicant applicant;
    private BTOProject project;
    private String content;
    private String response = null;
    private EnquiryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private LocalDateTime respondedAt = null;
    private User respondedBy = null; // User since it can be either HDBManager or HDBOfficer

    // Constructor for creating a new enquiry
    public Enquiry(Applicant applicant, BTOProject project, String content) {
        this.enquiryId = counter++;
        this.applicant = applicant;
        this.project = project;
        this.content = content;
        this.status = EnquiryStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.lastModified = this.createdAt;
    }

    // Constructor for loading an enquiry from the repository
    public Enquiry(int enquiryId, Applicant applicant, BTOProject project, String content, String response, EnquiryStatus status, LocalDateTime createdAt, LocalDateTime lastModified, LocalDateTime respondedAt, User respondedBy) { // Constructor for loading an enquiry from the repository
        this.enquiryId = enquiryId;
        this.applicant = applicant;
        this.project = project;
        this.content = content;
        this.response = response;
        this.status = status;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.respondedAt = respondedAt;
        this.respondedBy = respondedBy;
    }

    // Getters Methods
    public int getEnquiryId() {
        return enquiryId;
    }

    public Applicant getApplicant() {
        return applicant;
    }

    public BTOProject getProject() {
        return project;
    }

    public String getContent() {
        return content;
    }

    public String getResponse() {
        return response;
    }

    public EnquiryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public User getRespondedBy() {
        return respondedBy;
    }

    // Setter Methods
    public static void setCounter(int counter) { // The counter should always start from the next value of the highest enquiry ID in the repository, so that there are no duplicates.
        Enquiry.counter = counter;
    }

    // Other Methods
    public void editContent(String newContent) {
        if (this.status == EnquiryStatus.CLOSED) {
            throw new IllegalStateException("Cannot edit enquiry after response.");
        }
        this.content = newContent;
        this.lastModified = LocalDateTime.now();
    }

    public void respond(String response, User user) {
        if (this.status == EnquiryStatus.CLOSED) {
            throw new IllegalStateException("Enquiry already responded to.");
        }
        this.response = response;
        this.respondedBy = user;
        this.respondedAt = LocalDateTime.now();
        this.status = EnquiryStatus.CLOSED;
        this.lastModified = respondedAt;
    }

    public void markDeleted() {
        if (status == EnquiryStatus.CLOSED) {
            throw new IllegalStateException("Cannot delete an already closed enquiry.");
        }
        this.status       = EnquiryStatus.DELETED;
        this.lastModified = LocalDateTime.now();
    }
}
