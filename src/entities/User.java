package entities;

import enums.MaritalStatus;
import enums.UserRole;

public abstract class User {
    private String name;
    private String nric;
    private int age;
    private MaritalStatus maritalStatus;
    private String password;

    public User(String name, String nric, int age, MaritalStatus maritalStatus, String password) {
        this.nric = nric;
        this.name = name;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.password = password;
    }

    // Getter Methods
    public String getNric() {
        return nric;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public String getPassword() {
        return password;
    }

    // Setter Methods
    public void setNric(String nric) {
        this.nric = nric;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Other Methods
    public boolean validatePassword(String password) {
        return this.password.equals(password);
    }

    public boolean changePassword(String newPassword, String oldPassword) {
        if (validatePassword(oldPassword)) {
            this.password = newPassword;
            return true;
        } else {
            return false;
        }
    }
    
    // Abstract method to be implemented by subclasses
    public abstract UserRole getRole();
}
