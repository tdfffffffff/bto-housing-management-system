package repositories;

import entities.BTOProject;
import java.util.List;
import filters.ProjectFilter;

/**
 * Data-access contract for BTOProject entities.
 */
public interface ProjectRepository {

    /** Add a new project (in-memory). */
    void addProject(BTOProject project);

    /** Remove a project (in-memory). */
    void removeProject(BTOProject project);

    /** Lookup a project by its unique ID. */
    BTOProject findByName(String name);

    /** List all projects, regardless of visibility. */
    List<BTOProject> findAll();

    /** List only those projects whose visibility == VISIBLE. */
    List<BTOProject> findVisible();

    /** List all projects created by a given manager. */
    List<BTOProject> findByManager(String managerNric);

    List<BTOProject> findFilteredProjects(ProjectFilter filter);
    /**
     * Overwrite the backing CSV (or other store) with the current in-memory data.
     */
    void persist();
}
