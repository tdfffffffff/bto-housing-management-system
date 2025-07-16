package services;

import entities.User;
import repositories.UserRepository;
import enums.MaritalStatus;
import entities.Applicant;
import java.util.Optional;

/**
 * Handles authentication, password changes, and session state.
 */
public class AuthService {
    private final UserRepository userRepo;
    private User currentUser;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Attempt to log in with the given NRIC and password.
     * @return the authenticated User, or empty if credentials are invalid.
     */
    public User authenticateUser(String nric, String password) {
        for (User user : userRepo.findAll()) {
            if (user.getNric().equals(nric) && user.validatePassword(password)) {
                currentUser = user;
                return currentUser;
            }
        }
        return null;  // Authentication failed
    }

    /**
     * Change the current user's password.
     * @param currentPassword  the user's existing password
     * @param newPassword      the new password to set
     * @return true if changed successfully; false if the current password was wrong
     * @throws IllegalStateException if no user is logged in
     */
    public boolean changePassword(String nric, String currentPassword, String newPassword) {
        // Verify old credentials
        User user = authenticateUser(nric, currentPassword);
        if (user == null) {
            return false;   // current password was wrong
        }
        // Update password on the real User instance (not the temporary one from authenticateUser)
        // Since authenticateUser() resets currentUser only on success, we can use that:
        currentUser.setPassword(newPassword);
        userRepo.persist();  // save to CSV
        return true;
    }

    /** Log out the current user. */
    public void logout() {
        currentUser = null;
    }

    /** @return the currently authenticated user, or empty if none. */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public User createNewUser(String nric, String name, int age, MaritalStatus maritalStatus, String password) {
        if (userRepo.findByNric(nric) != null) {
            return null;  // NRIC already exists
        }
        User newUser = new Applicant(name, nric, age, maritalStatus, password);
        userRepo.addUser(newUser);
        currentUser = newUser;
        userRepo.persist();  
        return newUser;
    }
}
