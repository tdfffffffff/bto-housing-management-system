package entities;

import enums.UserRole;
import enums.MaritalStatus;

public class Applicant extends User{

    public Applicant(String name, String nric, int age, MaritalStatus maritalStatus, String password) {
        super(name, nric, age, maritalStatus, password);
    }

    @Override
    public UserRole getRole() {
        return UserRole.APPLICANT;
    }
}
