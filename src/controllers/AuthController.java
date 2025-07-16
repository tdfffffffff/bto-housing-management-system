package controllers;

import entities.User;
import services.AuthService;
import enums.MaritalStatus;

/**
 * Controller that mediates between the UI layer and AuthService.
 */
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Attempt to authenticate a user.
     *
     * @param nric     the user's NRIC
     * @param password the user's password
     * @return the authenticated User, or null if credentials are invalid
     */
    public User authenticate(String nric, String password) {
        return authService.authenticateUser(nric, password);
    }

    /**
     * Change the password for the currently logged-in user.
     *
     * @param nric            the User object from the current session
     * @param newPassword     the new password to set
     * @param currentPassword the user's current password for verification
     * @return true if the password was changed successfully; false if the current password was wrong
     * @throws IllegalArgumentException if the provided user does not match the session user
     */
    public boolean changePassword(String nric, String newPassword, String currentPassword) {
        return authService.changePassword(nric, currentPassword, newPassword);
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        authService.logout();
    }

    /**
     * Get the currently authenticated user.
     *
     * @return the User from the current session, or null if none
     */
    public User getCurrentUser() {
        return authService.getCurrentUser().orElse(null);
    }

    public User createNewUser(String nric, String name, int age, MaritalStatus maritalStatus, String password) {
        return authService.createNewUser(nric, name, age, maritalStatus, password);
    }
}
