package repositories;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import entities.BTOProject;
import entities.HDBOfficer;
import entities.Registration;
import entities.HDBManager;
import enums.RegistrationStatus;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-backed implementation of RegistrationRepository.
 */
public class CsvRegistrationRepository implements RegistrationRepository {
    private static final String CSV_PATH = "data/RegistrationList.csv";

    private final List<Registration> store = new ArrayList<>();
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;

    public CsvRegistrationRepository(UserRepository userRepo, ProjectRepository projectRepo) {
        this.userRepo    = userRepo;
        this.projectRepo = projectRepo;
        loadFromCsv();
    }

    @Override
    public void addRegistration(Registration registration) {
        store.add(registration);
        persist();
    }

    @Override
    public List<Registration> findByOfficer(String officerNric) {
        return store.stream()
                    .filter(r -> r.getOfficer().getNric().equalsIgnoreCase(officerNric))
                    .collect(Collectors.toList());
    }

    @Override
    public List<Registration> findByProject(String projectName) {
        return store.stream()
                    .filter(r -> r.getProject().getProjectName().equalsIgnoreCase(projectName)
                              && r.getStatus() == RegistrationStatus.APPROVED)
                    .collect(Collectors.toList());
    }

    @Override
    public void persist() {
        try (PrintWriter writer = new PrintWriter(CSV_PATH)) {
            writer.println("Officer_NRIC,Officer_Name,Project_Name,Registration_Status,SubmittedAt,ReviewedAt");
            for (Registration r : store) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                    r.getOfficer().getNric(),
                    r.getOfficer().getName(),
                    r.getProject().getProjectName(),
                    r.getStatus(),
                    r.getSubmittedAt().toString(),
                    r.getReviewedAt() != null ? r.getReviewedAt().toString() : ""
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist registrations", e);
        }
    }

    private void loadFromCsv() {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH))) {
            String[] row;
            boolean skip = true;
            while ((row = reader.readNext()) != null) {
                if (skip) { skip = false; continue; }
                String officerNric = row[0].trim();
                String projectName = row[2].trim();
                RegistrationStatus status    = RegistrationStatus.valueOf(row[3].trim().toUpperCase());
                LocalDate submittedAt    = LocalDate.parse(row[4].trim());
                LocalDate reviewedAt     = row[5].isEmpty() ? null : LocalDate.parse(row[5].trim());

                HDBOfficer officer = (HDBOfficer) userRepo.findByNric(officerNric);
                if (officer == null) {
                    throw new RuntimeException("Officer not found: " + officerNric);
                }
                BTOProject project = projectRepo.findByName(projectName);
                if (project == null) {
                    throw new RuntimeException("Project not found: " + projectName);
                }

                // Create a new Registration object and add it to the store
                Registration reg = new Registration(officer, project, status, submittedAt, reviewedAt);
                store.add(reg);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to load registrations", e);
        }
    }

    @Override
    public Registration findSpecificRegistration(String officerNric, String projectName) {
        return store.stream()
                    .filter(r -> r.getOfficer().getNric().equalsIgnoreCase(officerNric)
                              && r.getProject().getProjectName().equalsIgnoreCase(projectName))
                    .findFirst()
                    .orElse(null);
    }

    @Override
    public List<Registration> findAll() {
        return List.copyOf(store);
    }

    @Override
    public List<Registration> findByManager(HDBManager manager) {
        return store.stream()
                    .filter(r -> r.getProject().getManager().equals(manager))
                    .collect(Collectors.toList());
    }
}
