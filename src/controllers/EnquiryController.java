package controllers;

import entities.*;
import services.EnquiryService;

import java.util.List;

/**
 * Controller that mediates between the UI layer and EnquiryService.
 */
public class EnquiryController {
    private final EnquiryService enquiryService;

    public EnquiryController(EnquiryService enquiryService) {
        this.enquiryService = enquiryService;
    }

    /** Applicant submits a new enquiry */
    public Enquiry submitEnquiry(Applicant applicant, BTOProject project, String content) {
        return enquiryService.submitEnquiry(applicant, project, content);
    }

    /** Applicant edits their own open enquiry */
    public void editEnquiry(Applicant applicant, int enquiryId, String newContent) {
        enquiryService.editEnquiry(applicant, enquiryId, newContent);
    }

    /** Applicant deletes their own open enquiry */
    public void deleteEnquiry(Applicant applicant, int enquiryId) {
        enquiryService.deleteEnquiry(applicant, enquiryId);
    }

    /** Officer replies to an enquiry */
    public void replyToEnquiry(HDBOfficer officer, int enquiryId, String reply) {
        enquiryService.replyToEnquiry(officer, enquiryId, reply);
    }

    /** Manager replies to an enquiry */
    public void replyToEnquiry(HDBManager manager, int enquiryId, String reply) {
        enquiryService.replyToEnquiry(manager, enquiryId, reply);
    }

    /** List enquiries submitted by this applicant */
    public List<Enquiry> getSubmittedEnquiries(Applicant applicant) {
        return enquiryService.listByApplicant(applicant);
    }

    /** Officer sees open enquiries for their active projects */
    public List<Enquiry> getEnquiriesOfHandledProject(HDBOfficer officer) {
        return enquiryService.listOpenForOfficer(officer);
    }

    /** Manager sees open enquiries for their projects */
    public List<Enquiry> getEnquiriesOfHandledProject(HDBManager manager) {
        return enquiryService.listOpenForManager(manager);
    }

    /** Manager sees all enquiries (excluding deleted) */
    public List<Enquiry> getAllEnquiries(HDBManager manager) {
        return enquiryService.listAllEnquiries();
    }
}
