package filters;

import entities.BTOProject;
import enums.FlatType;
import enums.SortOption;

public class ProjectFilter {
    private String projectName;
    private String location;
    private FlatType flatType;
    private SortOption sortBy;
    
    public ProjectFilter() {
        this.sortBy = SortOption.PROJECT_ASC; // Default sorting option, alphabetically by project name
    }

    // Getter Methods
    public String getProjectName() {
        return projectName;
    }

    public String getLocation() {
        return location;
    }

    public FlatType getFlatType() {
        return flatType;
    }

    public SortOption getSortBy() {
        return sortBy;
    }

    // Setter Methods
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFlatType(FlatType flatType) {
        this.flatType = flatType;
    }

    public void setSortBy(SortOption sortBy) {
        this.sortBy = sortBy;
    }

    // Other Methods
    public boolean matches(BTOProject project) {
        boolean match = true;

        if (projectName != null && !project.getProjectName().toLowerCase().contains(projectName.toLowerCase())) {
            match = false;
        }

        if (location != null && !project.getNeighborhood().toLowerCase().contains(location.toLowerCase())) {
            match = false;
        }

        if (flatType != null && !project.getFlatsAvailable().containsKey(flatType)) {
            match = false;
        }

        return match;
    }

    public boolean filter(BTOProject project) {
        return matches(project);
    }
}
