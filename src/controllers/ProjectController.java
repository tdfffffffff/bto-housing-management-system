package controllers;

import entities.BTOProject;
import entities.HDBManager;
import entities.Applicant;
import entities.HDBOfficer;
import enums.FlatType;
import enums.VisibilityStatus;
import filters.ProjectFilter;
import services.ProjectService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * List projects according to the given filter and the current user’s role.
     */
    public List<BTOProject> getProjectsForApplicants(Applicant applicant, ProjectFilter filter) {
        return projectService.getProjectsForApplicant(applicant, filter);
    }

    public List<BTOProject> getProjectsForOfficers(HDBOfficer officer, ProjectFilter filter) {
        return projectService.getProjectsForOfficer(officer);
    }

    /**
     * Create a new project. All validation (name uniqueness, date windows, etc.)
     * lives in the service.
     */
    public BTOProject createProject(HDBManager manager,
                                    String projectName,
                                    String neighborhood,
                                    Map<FlatType, Integer> flatsAvailable,
                                    Map<FlatType, Integer> sellingPrice,
                                    VisibilityStatus visibilityStatus,
                                    int officerSlots,
                                    LocalDate openDate,
                                    LocalDate closeDate) {
        return projectService.createProject(
            manager,
            projectName,
            neighborhood,
            flatsAvailable,
            sellingPrice,
            visibilityStatus,
            officerSlots,
            openDate,
            closeDate
        );
    }

    /**
     * Edit an existing project.  Only the assigned manager may do this.
     */
    public void editProject(HDBManager manager,
                            BTOProject project,
                            String projectName,
                            String neighborhood,
                            Map<FlatType, Integer> flatsAvailable,
                            Map<FlatType, Integer> sellingPrice,
                            VisibilityStatus visibilityStatus,
                            int officerSlots,
                            LocalDate openDate,
                            LocalDate closeDate) {
        projectService.editProject(
            manager,
            project,
            projectName,
            neighborhood,
            flatsAvailable,
            sellingPrice,
            visibilityStatus,
            officerSlots,
            openDate,
            closeDate
        );
    }

    /**
     * Delete a project.  Only the assigned manager may delete.
     */
    public void deleteProject(HDBManager manager, BTOProject project) {
        projectService.deleteProject(manager, project);
    }

    /**
     * Toggle visibility on/off.  Only the assigned manager may toggle.
     */
    public void toggleVisibility(HDBManager manager, BTOProject project) {
        projectService.toggleVisibility(manager, project);
    }

    /**
     * Get all projects for a given manager.
     */
    public List<BTOProject> getProjectsForManager(HDBManager manager) {
        return projectService.getProjectsForManager(manager);
    }

    /**
     * Get all projects
     */
    public List<BTOProject> getAllProjects() {
        return projectService.getAllProjects();
    }
}
