package repositories;

import entities.Enquiry;

import java.util.List;

/**
 * Data-access contract for Enquiry entities.
 */
public interface EnquiryRepository {
    /** Add a new enquiry. */
    void addEnquiry(Enquiry enquiry);

    /** Find all enquiries submitted by a given applicant NRIC. */
    List<Enquiry> findByApplicant(String applicantNric);

    /** Find all enquiries for a given project name. */
    List<Enquiry> findByProject(String projectName);

    /** List every enquiry (for manager views). */
    List<Enquiry> findAll();

    /** Overwrite the backing CSV with current in-memory data. */
    void persist();

    /** Find an enquiry by its ID. */
    Enquiry findById(int enquiryId);
}
