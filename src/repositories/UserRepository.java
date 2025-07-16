package repositories;

import entities.User;
import entities.Applicant;
import entities.HDBOfficer;
import entities.HDBManager;

import java.util.List;

/**
 * Data–access contract for User entities (Applicants, Officers, Managers).
 */
public interface UserRepository {

    /** Add a new user (applicant, officer, or manager). */
    void addUser(User user);

    /** Lookup any user by their NRIC. */
    User findByNric(String nric);

    /** List all users, regardless of role. */
    List<User> findAll();

    /** List only those users who are Applicants. */
    List<Applicant> findAllApplicants();

    /** List only those users who are Officers. */
    List<HDBOfficer> findAllOfficers();

    /** List only those users who are HDB Managers. */
    List<HDBManager> findAllManagers();

    /** Find specific manager by name */
    HDBManager findManagerByName(String name);

    /** Find specific officer by name */
    HDBOfficer findOfficerByName(String name);
    
    /** Overwrite the backing CSV (or other store) with current in-memory data. */
    void persist();

    User findOfficerByNric(String respByNric);

    User findManagerByNric(String respByNric);
}
