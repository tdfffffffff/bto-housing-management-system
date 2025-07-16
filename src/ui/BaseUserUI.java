package ui;

import controllers.ApplicationController;
import controllers.AuthController;
import controllers.EnquiryController;
import controllers.ProjectController;
import filters.ProjectFilter;

import java.util.Scanner;

public abstract class BaseUserUI {
    protected final Scanner scanner;
    protected final AuthController authController;
    protected final ApplicationController appController;
    protected final EnquiryController enqController;
    protected final ProjectController projController;
    protected final ProjectFilter projectFilter;

    public BaseUserUI(AuthController authController,
                      ApplicationController appController,
                      EnquiryController enqController,
                      ProjectController projController,
                      Scanner scanner,
                      ProjectFilter projectFilter) 
    {
        this.authController = authController;
        this.appController = appController;
        this.enqController = enqController;
        this.projController = projController;
        this.scanner = scanner;
        this.projectFilter = projectFilter;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public AuthController getAuthController() {
        return authController;
    }

    public ApplicationController getAppController() {
        return appController;
    }

    public EnquiryController getEnqController() {
        return enqController;
    }

    public ProjectController getProjController() {
        return projController;
    }

    public ProjectFilter getProjectFilter() {
        return projectFilter;
    }

    public abstract void displayMenu();

    public void logOut() {
        authController.logout();
        System.out.println("You have been logged out successfully.");
    };
}