package repositories;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import entities.Applicant;
import entities.HDBManager;
import entities.HDBOfficer;
import entities.User;
import enums.MaritalStatus;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV-backed implementation of UserRepository.
 */
public class CsvUserRepository implements UserRepository {
    private static final String APPLICANT_CSV = "data/ApplicantList.csv";
    private static final String OFFICER_CSV   = "data/OfficerList.csv";
    private static final String MANAGER_CSV   = "data/ManagerList.csv";

    private final List<User> store = new ArrayList<>();

    public CsvUserRepository() {
        loadApplicants();
        loadOfficers();
        loadManagers();
    }

    @Override
    public void addUser(User user) {
        store.add(user);
        persist(); // immediately save to CSV
    }

    @Override
    public User findByNric(String nric) {
        return store.stream()
                .filter(u -> u.getNric().equalsIgnoreCase(nric))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return store;
    }

    @Override
    public List<Applicant> findAllApplicants() {
        return store.stream()
                .filter(u -> u instanceof Applicant)
                .map(u -> (Applicant) u)
                .collect(Collectors.toList());
    }

    @Override
    public List<HDBOfficer> findAllOfficers() {
        return store.stream()
                .filter(u -> u instanceof HDBOfficer)
                .map(u -> (HDBOfficer) u)
                .collect(Collectors.toList());
    }

    @Override
    public List<HDBManager> findAllManagers() {
        return store.stream()
                .filter(u -> u instanceof HDBManager)
                .map(u -> (HDBManager) u)
                .collect(Collectors.toList());
    }

    @Override
    public HDBManager findManagerByName(String name) {
        return store.stream()
                .filter(u -> u instanceof HDBManager && u.getName().equalsIgnoreCase(name))
                .map(u -> (HDBManager) u)
                .findFirst()
                .orElse(null);
    }

    @Override
    public HDBOfficer findOfficerByName(String name) {
        return store.stream()
                .filter(u -> u instanceof HDBOfficer && u.getName().equalsIgnoreCase(name))
                .map(u -> (HDBOfficer) u)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void persist() {
        try (
                PrintWriter aw = new PrintWriter(APPLICANT_CSV);
                PrintWriter ow = new PrintWriter(OFFICER_CSV);
                PrintWriter mw = new PrintWriter(MANAGER_CSV)
        ) {
            aw.println("Name,NRIC,Age,Marital_Status,Password");
            ow.println("Name,NRIC,Age,Marital_Status,Password");
            mw.println("Name,NRIC,Age,Marital_Status,Password");

            for (User u : store) {
                String line = String.format(
                        "%s,%s,%d,%s,%s",
                        u.getName(), u.getNric(),
                        u.getAge(), u.getMaritalStatus(),
                        u.getPassword()
                );
                if (u instanceof HDBManager) {
                    mw.println(line);
                } else if (u instanceof HDBOfficer) {
                    ow.println(line);
                } else if (u instanceof Applicant) {
                    aw.println(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist users", e);
        }
    }

    @Override
    public User findOfficerByNric(String nric) {
        return store.stream()
                .filter(u -> u instanceof HDBOfficer && u.getNric().equalsIgnoreCase(nric))
                .map(u -> (HDBOfficer) u)
                .findFirst()
                .orElse(null);
    }

    @Override
    public User findManagerByNric(String nric) {
        return store.stream()
                .filter(u -> u instanceof HDBManager && u.getName().equalsIgnoreCase(nric))
                .map(u -> (HDBManager) u)
                .findFirst()
                .orElse(null);
    }

    // -- private CSV loading helpers --

    private void loadApplicants() {
        loadCsv(APPLICANT_CSV, row -> {
            String name = row[0].trim();
            String nric = row[1].trim();
            int age     = Integer.parseInt(row[2].trim());
            MaritalStatus ms = MaritalStatus.valueOf(row[3].trim().toUpperCase());
            String pwd  = row[4].trim();
            store.add(new Applicant(name, nric, age, ms, pwd));
        });
    }

    private void loadOfficers() {
        loadCsv(OFFICER_CSV, row -> {
            String name = row[0].trim();
            String nric = row[1].trim();
            int age     = Integer.parseInt(row[2].trim());
            MaritalStatus ms = MaritalStatus.valueOf(row[3].trim().toUpperCase());
            String pwd  = row[4].trim();
            HDBOfficer obj = new HDBOfficer(name, nric, age, ms, pwd);
            // System.out.println(obj.getRole());
            store.add(obj);
        });
    }

    private void loadManagers() {
        loadCsv(MANAGER_CSV, row -> {
            String name = row[0].trim();
            String nric = row[1].trim();
            int age     = Integer.parseInt(row[2].trim());
            MaritalStatus ms = MaritalStatus.valueOf(row[3].trim().toUpperCase());
            String pwd  = row[4].trim();
            store.add(new HDBManager(name, nric, age, ms, pwd));
        });
    }

    private void loadCsv(String path, CsvRowConsumer consumer) {
        try (CSVReader reader = new CSVReader(new FileReader(path))) {
            String[] row;
            boolean skip = true;
            while ((row = reader.readNext()) != null) {
                if (skip) { skip = false; continue; }
                consumer.accept(row);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to load " + path, e);
        }
    }

    @FunctionalInterface
    private interface CsvRowConsumer {
        void accept(String[] row);
    }


}
