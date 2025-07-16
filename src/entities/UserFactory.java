package entities;

import enums.MaritalStatus;
import enums.UserRole;

public class UserFactory {

    public static User create(String name, String nric, int age, MaritalStatus maritalStatus, String password, UserRole userType) {
        //String hashedPassword = PasswordHashManager.hashPassword(password);
        return switch (userType) {
            case APPLICANT -> new Applicant(nric, name, age, maritalStatus, password);
            case HDB_OFFICER -> new HDBOfficer(nric, name, age, maritalStatus, password);
            case HDB_MANAGER -> new HDBManager(nric, name, age, maritalStatus, password);
        };
    }
}
