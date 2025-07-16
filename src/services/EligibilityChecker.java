package services;

import entities.Applicant;
import entities.BTOProject;
import enums.FlatType;
import enums.MaritalStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class EligibilityChecker {
    public static boolean isEligible(Applicant applicant, BTOProject project) {
        Set<FlatType> availableTypes = project.getFlatsAvailable().keySet();

        if (applicant.getMaritalStatus() == MaritalStatus.MARRIED && applicant.getAge() >= 21) {
            // Married applicants 21+ can apply for both 2-Room and 3-Room
            return availableTypes.contains(FlatType.TWO_ROOM) || availableTypes.contains(FlatType.THREE_ROOM);
        }

        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && applicant.getAge() >= 35) {
            // Singles 35+ can only apply for 2-Room
            return availableTypes.contains(FlatType.TWO_ROOM);
        }

        return false; // Others are not eligible
    }

    public static boolean NRICValidator (String nric){
            return nric != null && nric.matches("^[ST]\\d{7}[A-Z]$");
    }

    public static FlatType chooseFlatType(Applicant applicant, BTOProject project, Scanner scanner) {
        Set<FlatType> availableTypes = project.getFlatsAvailable().keySet();

        if (applicant.getMaritalStatus() == MaritalStatus.SINGLE && applicant.getAge() >= 35) {
            if (availableTypes.contains(FlatType.TWO_ROOM)) {
                System.out.println("✅ You are eligible only for 2-Room. It has been selected automatically.");
                return FlatType.TWO_ROOM;
            }
        } else if (applicant.getMaritalStatus() == MaritalStatus.MARRIED && applicant.getAge() >= 21) {
            List<FlatType> eligible = new ArrayList<>(availableTypes);
            System.out.println("Select a flat type to apply:");
            for (int i = 0; i < eligible.size(); i++) {
                System.out.printf("%d. %s\n", i + 1, eligible.get(i));
            }

            System.out.print("Your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice > 0 && choice <= eligible.size()) {
                return eligible.get(choice - 1);
            } else {
                System.out.println("Invalid flat type choice.");
            }
        }

        return null;
    }

}
