package controllers;

import entities.BTOProject;
import entities.HDBOfficer;
import entities.Registration;
import entities.HDBManager;
import enums.RegistrationStatus;
import services.RegistrationService;

import java.util.List;

public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Officer submits a registration request for a project.
     */
    public Registration submitRegistration(HDBOfficer officer, BTOProject project) {
        return registrationService.submitRegistration(officer, project);
    }

    /**
     * Manager approves or rejects a registration.
     */
    public void reviewRegistration(Registration registration, boolean approve) {
        registrationService.reviewRegistration(registration, approve);
    }

    /**
     * List registrations for a project, optionally filtered by status.
     */
    public List<Registration> listByProject(String projectName, RegistrationStatus statusFilter) {
        return registrationService.listByProject(projectName, statusFilter);
    }

    /**
     * Find a specific registration for an officer on a project.
     */
    public Registration findSpecificRegistration(String officerNric, String projectName) {
        return registrationService.findSpecificRegistration(officerNric, projectName);
    }

    public List<Registration> findByOfficer(String officerNric) {
        return registrationService.findByOfficer(officerNric);
    }

    /**
     * List all registrations, regardless of status.
     */
    public List<Registration> listAllRegistrations() {
        return registrationService.listAllRegistrations();
    }

    /**
     * List registrations by manager.
     */
    public List<Registration> findByManager(HDBManager manager) {
        return registrationService.findByManager(manager);
    }
}
