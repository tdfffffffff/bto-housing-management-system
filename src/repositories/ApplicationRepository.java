package repositories;

import entities.Application;

import java.util.List;

/**
 * Data-access contract for Application entities.
 */
public interface ApplicationRepository {
    /** In‐memory mutations: */
    void addApplication(Application application);

    /** Queries: */
    Application findByNRIC(String nric);
    List<Application> findByProject(String projectName);
    List<Application> findPendingApplications(String projectName);
    List<Application> findAll();
    List<Application> findWithdrawalRequests();
    List<Application> findByBooked();

    /** Flush the current in‐memory state back to the CSV file (overwrite). */
    void persist();


}
