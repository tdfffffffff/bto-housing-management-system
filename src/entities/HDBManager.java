package entities;

import enums.MaritalStatus;
import enums.UserRole;

public class HDBManager extends User {

    public HDBManager(String name, String nric, int age, MaritalStatus maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public UserRole getRole() {
        return UserRole.HDB_MANAGER;
    }
}
