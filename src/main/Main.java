package main;

import controllers.*;
import filters.ProjectFilter;
import repositories.*;
import services.*;
import ui.LoginUI;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 0) Shared Scanner (auto-closed)
        try (Scanner scanner = new Scanner(System.in)) {
            
            // 1) Repositories
            UserRepository         userRepo    = new CsvUserRepository();
            ProjectRepository      projectRepo = new CsvProjectRepository(userRepo);
            CsvRegistrationRepository regRepo   = new CsvRegistrationRepository(userRepo, projectRepo);

            // fix the back-reference that CsvProjectRepository needs:
            ((CsvProjectRepository)projectRepo).setRegistrationRepository(regRepo);

            ApplicationRepository  appRepo     = new CsvApplicationRepository(userRepo, projectRepo);
            EnquiryRepository      enquiryRepo = new CsvEnquiryRepository(userRepo, projectRepo);

            // 2) Services
            AuthService            authService          = new AuthService(userRepo);
            ApplicationService     applicationService   = new ApplicationService(appRepo, projectRepo);
            // NOTE: we forward-declare registrationService so we can inject projectService later
            RegistrationService    registrationService  = new RegistrationService(regRepo, applicationService);
            ProjectService         projectService       = new ProjectService(projectRepo, registrationService);
            // now “complete” the circular link
            registrationService.setProjectService(projectService);
            EnquiryService         enquiryService       = new EnquiryService(enquiryRepo, regRepo);

            // 3) Controllers
            AuthController         authController  = new AuthController(authService);
            ApplicationController  appController   = new ApplicationController(applicationService);
            RegistrationController regController   = new RegistrationController(registrationService);
            ProjectController      projController  = new ProjectController(projectService);
            EnquiryController      enqController   = new EnquiryController(enquiryService);

            // 4) Shared filter & UI bootstrap
            ProjectFilter projectFilter = new ProjectFilter();
            LoginUI loginUI = new LoginUI(
                authController,
                appController,
                enqController,
                projController,
                scanner,
                regController,
                projectFilter
            );

            // 5) Kick things off
            loginUI.start();

            // 6) On exit, flush everything
            userRepo.persist();
            projectRepo.persist();
            regRepo.persist();
            appRepo.persist();
            enquiryRepo.persist();
        }
    }
}
