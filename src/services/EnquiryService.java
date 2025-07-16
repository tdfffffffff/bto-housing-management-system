package services;

import entities.Applicant;
import entities.Enquiry;
import entities.HDBOfficer;
import entities.Registration;
import entities.HDBManager;
import entities.BTOProject;
import entities.User;
import enums.EnquiryStatus;
import enums.RegistrationStatus;
import repositories.EnquiryRepository;
import repositories.RegistrationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for creating, editing, deleting, and replying to enquiries.
 */
public class EnquiryService {
    private final EnquiryRepository repo;
    private final RegistrationRepository regRepo;

    public EnquiryService(EnquiryRepository repo, RegistrationRepository regRepo) {
        this.repo = repo;
        this.regRepo = regRepo;
    }

    /**
     * Applicant submits a new enquiry.
     */
    public Enquiry submitEnquiry(Applicant applicant, BTOProject project, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Enquiry content cannot be empty.");
        }
        Enquiry e = new Enquiry(applicant, project, content);
        repo.addEnquiry(e);
        repo.persist();
        return e;
    }

    /**
     * Applicant edits their own open enquiry.
     */
    public void editEnquiry(Applicant applicant, int enquiryId, String newContent) {
        Enquiry e = repo.findById(enquiryId);
        if (e == null) {
            throw new IllegalStateException("Enquiry not found.");
        }

        if (!e.getApplicant().equals(applicant)) {
            throw new IllegalStateException("Cannot edit someone else's enquiry.");
        }
        if (e.getStatus() != EnquiryStatus.OPEN) {
            throw new IllegalStateException("Only open enquiries can be edited.");
        }
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Enquiry content cannot be empty.");
        }
        e.editContent(newContent);
        repo.persist();
    }

    /**
     * Applicant deletes their own open enquiry.
     */
    public void deleteEnquiry(Applicant applicant, int enquiryId) {
        Enquiry e = repo.findById(enquiryId);
        if (e == null) {
            throw new IllegalStateException("Enquiry not found.");
        }
        if (e.getStatus() == EnquiryStatus.CLOSED) {
            throw new IllegalStateException("Cannot delete a closed enquiry.");
        }
        if (!e.getApplicant().equals(applicant)) {
            throw new IllegalStateException("Cannot delete someone else's enquiry.");
        }
        e.markDeleted();
        repo.persist();
    }

    /**
     * Officer or Manager replies to an open enquiry on a project they handle.
     */
    public void replyToEnquiry(User responder, int enquiryId, String replyText) {
        Enquiry e = repo.findById(enquiryId);
        if (e == null) {
            throw new IllegalStateException("Enquiry not found.");
        }
        if (replyText == null || replyText.isBlank()) {
            throw new IllegalArgumentException("Reply text cannot be empty.");
        }
        if (e.getStatus() != EnquiryStatus.OPEN) {
            throw new IllegalStateException("Only open enquiries can be replied to.");
        }
        // authorization: only officers/managers of that project
        BTOProject project = e.getProject();
        boolean authorized = false;
        if (responder instanceof HDBOfficer) {
            authorized = regRepo.findSpecificRegistration(responder.getNric(), project.getProjectName()) != null;
        } else if (responder instanceof HDBManager manager) {
            authorized = project.getManager().equals(manager);
        }
        if (!authorized) {
            throw new IllegalStateException("Not authorized to reply to this enquiry.");
        }
        e.respond(replyText, responder);
        repo.persist();
    }

    /**
     * Get all enquiries submitted by this applicant.
     */
    public List<Enquiry> listByApplicant(Applicant applicant) {
        return repo.findByApplicant(applicant.getNric());
    }

    /**
     * Get all open enquiries for projects handled by this officer.
     */
    public List<Enquiry> listOpenForOfficer(HDBOfficer officer) {
    LocalDate today = LocalDate.now();

    // 1) All APPROVED registrations for this officer
    List<Registration> approved = regRepo.findByOfficer(officer.getNric()).stream()
        .filter(r -> r.getStatus() == RegistrationStatus.APPROVED)
        .toList();

    // 2) Only those whose project application window is still open
    List<BTOProject> handling = approved.stream()
        .map(Registration::getProject)
        .filter(p ->
            ( ! today.isBefore(p.getOpenDate()) ) &&
            ( ! today.isAfter  (p.getCloseDate()) )
        )
        .toList();

    // 3) For each such project, grab its enquiries and filter OPEN
    return handling.stream()
        .flatMap(proj ->
            repo.findByProject(proj.getProjectName()).stream()
               .filter(e -> e.getStatus() == EnquiryStatus.OPEN)
        )
        .collect(Collectors.toList());
}

    /**
     * Get all open enquiries for projects managed by this manager.
     */
    public List<Enquiry> listOpenForManager(HDBManager manager) {
        return repo.findAll().stream()
            // Only enquiries for projects this manager owns
            .filter(e -> e.getProject().getManager().equals(manager))
            // Only those still open
            .filter(e -> e.getStatus() == EnquiryStatus.OPEN)
            .collect(Collectors.toList());
    }

    public List<Enquiry> listAllEnquiries() {
        return repo.findAll();
    }
    
}

