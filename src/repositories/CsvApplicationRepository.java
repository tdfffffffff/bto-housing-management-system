package repositories;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import entities.Application;
import entities.Applicant;
import entities.BTOProject;
import enums.ApplicationStatus;
import enums.FlatType;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class CsvApplicationRepository implements ApplicationRepository {
    private static final String CSV_PATH = "data/ApplicationList.csv";

    private final List<Application> store = new ArrayList<>();
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public CsvApplicationRepository(UserRepository userRepository, ProjectRepository projectRepository) {
        this.userRepository    = userRepository;
        this.projectRepository = projectRepository;
        loadFromCsv();
    }

    @Override
    public void addApplication(Application application) {
        store.add(application);
        persist();
    }

    @Override
    public List<Application> findPendingApplications(String projectName) {
        return store.stream()
                    .filter(a -> a.getProject().getProjectName().equals(projectName) && a.getStatus() == ApplicationStatus.PENDING)
                    .collect(Collectors.toList());
    }

    @Override
    public Application findByNRIC(String nric) {
        return store.stream()
                    .filter(a -> a.getApplicant().getNric().equals(nric))
                    .findFirst()
                    .orElse(null);
    }

    @Override
    public List<Application> findByProject(String projectName) {
        return store.stream()
                    .filter(a -> a.getProject().getProjectName().equals(projectName))
                    .collect(Collectors.toList());
    }

    @Override
    public List<Application> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public void persist() {
        try (PrintWriter writer = new PrintWriter(CSV_PATH)) {
            writer.println("Applicant_Name,Applicant_NRIC,Applicant_Age,Applicant_Marital_Status,Project_Name,Flat_Type,Application_Status,Request_Withdrawal");
            for (Application a : store) {
                writer.printf("%s,%s,%d,%s,%s,%s,%s,%b%n",
                    a.getApplicant().getName(),
                    a.getApplicant().getNric(),
                    a.getApplicant().getAge(),
                    a.getApplicant().getMaritalStatus(),
                    a.getProject().getProjectName(),
                    a.getFlatType(),
                    a.getStatus(),
                    a.isRequestWithdrawal()
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist applications to CSV", e);
        }
    }

    private void loadFromCsv() {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH))) {
            String[] row;
            boolean first = true;
            while ((row = reader.readNext()) != null) {
                if (first) {
                    first = false;
                    continue; // skip header
                }

                if (row.length < 8) {
                    System.out.println("⚠️ Skipping malformed row: " + Arrays.toString(row));
                    continue;
                }

                try {
                    String nric       = row[1].trim();
                    String projName   = row[4].trim();
                    FlatType ft       = FlatType.valueOf(row[5].trim().toUpperCase());
                    ApplicationStatus st = ApplicationStatus.valueOf(row[6].trim().toUpperCase());
                    boolean reqWd     = Boolean.parseBoolean(row[7].trim());

                    Applicant applicant = (Applicant) userRepository.findByNric(nric);
                    BTOProject project  = projectRepository.findByName(projName);

                    if (applicant == null || project == null) {
                        System.out.printf("⚠️ Skipping row: user or project not found for NRIC=%s, project=%s%n", nric, projName);
                        continue;
                    }

                    Application app = new Application(applicant, project, ft, st, reqWd);
                    store.add(app);

                } catch (Exception e) {
                    System.out.println("⚠️ Failed to parse row: " + Arrays.toString(row));
                    e.printStackTrace(); // Optional: comment this out in production
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to load applications from CSV", e);
        }
    }

    // Find applications that want to withdraw
    @Override
    public List<Application> findWithdrawalRequests() {
        return store.stream()
                    .filter(Application::isRequestWithdrawal)
                    .collect(Collectors.toList());
    }

    @Override
    public List<Application> findByBooked() {
        return store.stream()
                    .filter(a -> a.getStatus() == ApplicationStatus.BOOKED)
                    .collect(Collectors.toList());
    }

}
