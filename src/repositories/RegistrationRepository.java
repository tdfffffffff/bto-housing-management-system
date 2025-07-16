package repositories;

import entities.Registration;
import entities.HDBManager;

import java.util.List;

/**
 * Data-access contract for Registration entities.
 */
public interface RegistrationRepository {
    /** Add a new officer-to-project registration. */
    void addRegistration(Registration registration);

    /** Find all registrations for a given officer NRIC. */
    List<Registration> findByOfficer(String officerNric);

    /** Find all registrations for a given project name. */
    List<Registration> findByProject(String projectName);

    Registration findSpecificRegistration(String officerNric, String projectName);
    /** Overwrite the backing CSV with current in-memory data. */
    void persist();

    /** List every registration (for manager views). */
    List<Registration> findAll();

    List<Registration> findByManager(HDBManager manager);
}
