package controllers;

import entities.*;
import enums.FlatType;
import services.ApplicationService;

import java.util.List;

public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    /**
     * Submit a new application for the logged-in applicant.
     */
    public Application submitApplication(Applicant applicant,
                                         BTOProject project,
                                         FlatType flatType) {
        return service.submitApplication(applicant, project, flatType);
    }

    /**
     * Request withdrawal of an existing application.
     */
    public void requestWithdrawal(Applicant applicant) {
        service.requestWithdrawal(applicant);
    }

    /**
     * Manager reviews (approve/reject) an application.
     */
    public void reviewApplication(Application application, boolean approve) {
        service.reviewApplication(application, approve);
    }

    /**
     * Officer books a flat for a successful application.
     */
    public Receipt bookFlat(HDBOfficer officer, Application application) {
        return service.bookFlat(officer, application);
    }


    /**
     * List all applications for a project.
     */
    public List<Application> listByProject(String projectName) {
        return service.listByProject(projectName);
    }

    /**
     * List all pending withdrawal requests.
     */
    public List<Application> listWithdrawalRequests() {
        return service.listWithdrawalRequests();
    }

    public Application getApplicationByNric(String nric) {
        return service.getApplicationByNric(nric);
    }
    
    public void approveWithdrawal(Application application, boolean approve) {
        service.approveWithdrawal(application, approve);
    }

    public List<Application> listAllBooked() {
        return service.findByBooked();
    }

    public void generateReceipt(){

    }

}
