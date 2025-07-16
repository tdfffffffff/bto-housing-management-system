package entities;

import enums.UserRole;
import enums.MaritalStatus;

public class HDBOfficer extends Applicant{

    public HDBOfficer(String name, String nric, int age, MaritalStatus maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public UserRole getRole() {
        return UserRole.HDB_OFFICER;
    }
}
