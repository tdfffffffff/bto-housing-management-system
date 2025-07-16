package repositories;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import entities.Applicant;
import entities.BTOProject;
import entities.Enquiry;
import entities.User;
import enums.EnquiryStatus;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CSV-backed implementation of EnquiryRepository.
 */
public class CsvEnquiryRepository implements EnquiryRepository {
    private static final String CSV = "data/EnquiryList.csv";
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<Enquiry> store;
    private final UserRepository userRepo;
    private final ProjectRepository projRepo;

    public CsvEnquiryRepository(UserRepository userRepo, ProjectRepository projRepo) {
        this.userRepo = userRepo;
        this.projRepo = projRepo;
        this.store    = new java.util.ArrayList<>();
        loadFromCsv();
        // sync Enquiry.counter
        Enquiry.setCounter(store.stream()
                            .mapToInt(Enquiry::getEnquiryId)
                            .max().orElse(0) + 1);
    }

    @Override
    public void addEnquiry(Enquiry e) {
        store.add(e);
        persist();
    }

    @Override
    public List<Enquiry> findByApplicant(String applicantNric) {
        return store.stream()
                    .filter(e -> e.getApplicant().getNric().equalsIgnoreCase(applicantNric))
                    .collect(Collectors.toList());
    }

    @Override
    public List<Enquiry> findByProject(String projectName) {
        return store.stream()
                    .filter(e -> e.getProject().getProjectName().equalsIgnoreCase(projectName))
                    .collect(Collectors.toList());
    }

    @Override
    public List<Enquiry> findAll() {
        return List.copyOf(store);
    }

    @Override
    public void persist() {
        try (PrintWriter w = new PrintWriter(CSV)) {
            w.println("Enquiry_ID,Applicant_Name,Applicant_NRIC,Project_Name,Content,Response,Status,Created_At,Last_Modified,Responded_At,Responded_By");
            for (Enquiry e : store) {
                w.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    e.getEnquiryId(),
                    e.getApplicant().getName(),
                    e.getApplicant().getNric(),
                    e.getProject().getProjectName(),
                    escape(e.getContent()),
                    e.getResponse() == null ? "" : escape(e.getResponse()),
                    e.getStatus(),
                    e.getCreatedAt().format(TS_FMT),
                    e.getLastModified().format(TS_FMT),
                    e.getRespondedAt() == null ? "" : e.getRespondedAt().format(TS_FMT),
                    e.getRespondedBy() == null ? "" : e.getRespondedBy().getNric()
                );
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to persist enquiries", ex);
        }
    }

    private void loadFromCsv() {
        try (CSVReader r = new CSVReader(new FileReader(CSV))) {
            String[] row;
            boolean skip = true;
            while ((row = r.readNext()) != null) {
                if (skip) { skip = false; continue; }
                int    id    = Integer.parseInt(row[0].trim());
                String nric  = row[2].trim();
                String proj  = row[3].trim();
                String content = row[4];
                String response= row[5].isEmpty() ? null : row[5].trim();
                EnquiryStatus status = EnquiryStatus.valueOf(row[6].trim().toUpperCase());
                LocalDateTime created    = LocalDateTime.parse(row[7].trim(), TS_FMT);
                LocalDateTime modified   = LocalDateTime.parse(row[8].trim(), TS_FMT);
                LocalDateTime responded  = row[9].isEmpty() ? null : LocalDateTime.parse(row[8].trim(), TS_FMT);
                String respByNric        = row[10].trim();

                Applicant applicant = (Applicant) userRepo.findByNric(nric);
                if (applicant == null) {
                    throw new RuntimeException("Applicant not found: " + nric);
                }
                BTOProject project  = projRepo.findByName(proj);
                if (project == null) {
                    throw new RuntimeException("Project not found: " + proj);
                }
                User responder = respByNric.isEmpty()
                    ? null
                    : (userRepo.findOfficerByNric(respByNric) != null) ? userRepo.findOfficerByNric(respByNric)
                    : userRepo.findManagerByNric(respByNric);

                Enquiry e = new Enquiry(id, applicant, project, content, response, status, created, modified, responded, responder);
                store.add(e);
            }
        } catch (IOException | CsvValidationException ex) {
            throw new RuntimeException("Failed to load enquiries", ex);
        }
    }

    private String escape(String s) {
        return s == null ? "" : "\"" + s.replace("\"", "\"\"") + "\"";
    }

    @Override
    public Enquiry findById(int enquiryId) {
        return store.stream()
                    .filter(e -> e.getEnquiryId() == enquiryId)
                    .findFirst()
                    .orElse(null);
    }
}
