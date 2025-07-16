package repositories;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import entities.BTOProject;
import entities.HDBManager;
import enums.FlatType;
import enums.VisibilityStatus;
import filters.ProjectFilter;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CSV-backed implementation of ProjectRepository.
 */

 public class CsvProjectRepository implements ProjectRepository {
    private static final String CSV_PATH = "data/ProjectList.csv";
    private final List<BTOProject> store = new ArrayList<>();
    private final UserRepository userRepository;
    private RegistrationRepository registrationRepository;


    public CsvProjectRepository(UserRepository userRepository) {
        this.userRepository        = userRepository;
        loadFromCsv();
    }

    // Set registrationRepository after instantiation
    public void setRegistrationRepository(RegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    @Override
    public void addProject(BTOProject project) {
        store.add(project);
        persist();
    }

    @Override
    public void removeProject(BTOProject project) {
        store.remove(project);
        persist();
    }

    @Override
    public BTOProject findByName(String projectName) {
        return store.stream()
                    .filter(p -> p.getProjectName().equalsIgnoreCase(projectName))
                    .findFirst()
                    .orElse(null);
    }

    @Override
    public List<BTOProject> findAll() {
        return Collections.unmodifiableList(store);
    }

    @Override
    public List<BTOProject> findVisible() {
        return store.stream()
                    .filter(p -> p.getVisibilityStatus() == VisibilityStatus.VISIBLE)
                    .collect(Collectors.toList());
    }

    @Override
    public List<BTOProject> findByManager(String managerNric) {
        return store.stream()
                    .filter(p -> p.getManager().getNric().equals(managerNric))
                    .collect(Collectors.toList());
    }

    @Override
    public void persist() {
        try (PrintWriter writer = new PrintWriter(CSV_PATH)) {
            // header including “Officers” column
            writer.println(
                "Project_Name,Neighborhood," +
                "Flat_Type_1,Flats_Available_1,Selling_Price_1," +
                "Flat_Type_2,Flats_Available_2,Selling_Price_2," +
                "Opening_Date,Closing_Date," +
                "Manager,Officer_Slots,Officers"
            );

            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

            for (BTOProject p : store) {
                // build pipe-delimited list of *approved* officer names
                String officers = registrationRepository
                    .findByProject(p.getProjectName())   // returns only APPROVED regs
                    .stream()
                    .map(r -> r.getOfficer().getName())
                    .collect(Collectors.joining("|"));

                // assume exactly two flat types
                Iterator<Map.Entry<FlatType,Integer>> itQ = p.getFlatsAvailable().entrySet().iterator();
                FlatType ft1 = itQ.next().getKey(); int avail1 = p.getFlatsAvailable().get(ft1);
                FlatType ft2 = itQ.next().getKey(); int avail2 = p.getFlatsAvailable().get(ft2);

                int price1 = p.getSellingPrice().get(ft1);
                int price2 = p.getSellingPrice().get(ft2);

                writer.printf(
                    "%s,%s,%s,%d,%d,%s,%d,%d,%s,%s,%s,%d,%s%n",
                    p.getProjectName(),
                    p.getNeighborhood(),
                    ft1, avail1, price1,
                    ft2, avail2, price2,
                    p.getOpenDate().format(fmt),
                    p.getCloseDate().format(fmt),
                    p.getManager().getName(),
                    p.getAvailableOfficerSlots(),
                    officers
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to persist projects", e);
        }
    }


    private void loadFromCsv() {
        try (CSVReader reader = new CSVReader(new FileReader(CSV_PATH))) {
            String[] row;
            boolean skip = true;
            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
    
            while ((row = reader.readNext()) != null) {
                if (skip) { skip = false; continue; }
    
                String name         = row[0].trim();
                String neighborhood = row[1].trim();
    
                FlatType ft1        = FlatType.valueOf(row[2].trim().toUpperCase());
                int avail1          = Integer.parseInt(row[3].trim());
                int price1          = Integer.parseInt(row[4].trim());
    
                FlatType ft2        = FlatType.valueOf(row[5].trim().toUpperCase());
                int avail2          = Integer.parseInt(row[6].trim());
                int price2          = Integer.parseInt(row[7].trim());
    
                LocalDate open      = LocalDate.parse(row[8].trim(), fmt);
                LocalDate close     = LocalDate.parse(row[9].trim(), fmt);
    
                String mgrName      = row[10].trim();
                int slots           = Integer.parseInt(row[11].trim());
    
                VisibilityStatus vs = (!LocalDate.now().isBefore(open) && !LocalDate.now().isAfter(close))
                                      ? VisibilityStatus.VISIBLE
                                      : VisibilityStatus.HIDDEN;
    
                Map<FlatType,Integer> quota = Map.of(ft1, avail1, ft2, avail2);
                Map<FlatType,Integer> price = Map.of(ft1, price1, ft2, price2);
    
                HDBManager mgr = (HDBManager) userRepository.findManagerByName(mgrName);
                if (mgr == null) {
                    throw new IllegalStateException("Manager not found: " + mgrName);
                }
    
                BTOProject proj = new BTOProject(
                    name, neighborhood,
                    quota, price,
                    vs, open, close,
                    slots, mgr
                );
    
                // projects do *not* store officers in-memory; they're derived when needed
                store.add(proj);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Failed to load projects", e);
        }
    }

    @Override
    public List<BTOProject> findFilteredProjects(ProjectFilter filter) {
        // 1) Filter by projectName, location, flatType
        List<BTOProject> filtered = store.stream()
            .filter(filter::matches)
            .collect(Collectors.toList());

        // 2) Sort according to filter.sortBy
        Comparator<BTOProject> comparator;
        switch (filter.getSortBy()) {
            case PROJECT_DESC:
                comparator = Comparator.comparing(BTOProject::getProjectName).reversed();
                break;
            case LOCATION_ASC:
                comparator = Comparator.comparing(BTOProject::getNeighborhood);
                break;
            case LOCATION_DESC:
                comparator = Comparator.comparing(BTOProject::getNeighborhood).reversed();
                break;
            case PROJECT_ASC:
            default:
                comparator = Comparator.comparing(BTOProject::getProjectName);
        }

        return filtered.stream()
                       .sorted(comparator)
                       .collect(Collectors.toList());
    }

}
