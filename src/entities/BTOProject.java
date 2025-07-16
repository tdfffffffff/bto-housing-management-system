package entities;

import enums.FlatType;
import enums.VisibilityStatus;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BTOProject {
    private static final int MAX_OFFICER_SLOTS = 10; // Maximum number of officer slots that can be assigned for a project
    private String projectName;
    private String neighborhood;
    private Map<FlatType, Integer> flatsAvailable; // Decreases every time an application is booked, increases when withdrawal is successul after booking
    private Map<FlatType, Integer> sellingPrice;
    private VisibilityStatus visibilityStatus;
    private LocalDate openDate;
    private LocalDate closeDate;
    private int availableOfficerSlots; // Decreases every time a registration is approved
    private HDBManager manager;

    public BTOProject(String projectName,
                      String neighborhood,
                      Map<FlatType, Integer> flatsAvailable,
                      Map<FlatType, Integer> sellingPrice,
                      VisibilityStatus visibilityStatus,
                      LocalDate openDate,
                      LocalDate closeDate,
                      int availableOfficerSlots,
                      HDBManager manager) {
        this.projectName           = projectName;
        this.neighborhood          = neighborhood;
        // wrap the passed-in maps in a mutable HashMap:
        this.flatsAvailable        = new java.util.HashMap<>(flatsAvailable);
        this.sellingPrice          = new java.util.HashMap<>(sellingPrice);
        this.visibilityStatus      = visibilityStatus;
        this.openDate              = openDate;
        this.closeDate             = closeDate;
        this.availableOfficerSlots = availableOfficerSlots;
        this.manager               = manager;
    }

    // Getter Methods
    public String getProjectName() {
        return projectName;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public Map<FlatType, Integer> getFlatsAvailable() {
        return Collections.unmodifiableMap(flatsAvailable);
    }

    public Map<FlatType, Integer> getSellingPrice() {
        return Collections.unmodifiableMap(sellingPrice);
    }

    public int getFlatsAvailableFor(FlatType flatType) {
        return flatsAvailable.getOrDefault(flatType, 0);
    }

    public int getSellingPriceFor(FlatType flatType) {
        return sellingPrice.getOrDefault(flatType, 0);
    }

    public VisibilityStatus getVisibilityStatus() {
        return visibilityStatus;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public int getAvailableOfficerSlots() {
        return availableOfficerSlots;
    }

    public HDBManager getManager() {
        return manager;
    }

    // Setter Methods
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void setSellingPrice(Map<FlatType, Integer> sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public void setAvailableOfficerSlots(int availableOfficerSlots) {
        if (availableOfficerSlots < 0 || availableOfficerSlots > MAX_OFFICER_SLOTS) {
            throw new IllegalArgumentException("Invalid number of officer slots.");
        }
        this.availableOfficerSlots = availableOfficerSlots;
    }
    
    public void setSellingPriceFor(FlatType flatType, int price) {
        sellingPrice.put(flatType, price);
    }

    public void setVisibilityStatus(VisibilityStatus visibilityStatus) {
        this.visibilityStatus = visibilityStatus;
    }

    public void setOpenDate(LocalDate openDate) {
        this.openDate = openDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    // Other Methods
    public boolean hasFlatsAvailable(FlatType flatType) {
        return flatsAvailable.containsKey(flatType) && flatsAvailable.get(flatType) > 0;
    }

    public void addFlats(FlatType type, int count) {
        if (count < 0) throw new IllegalArgumentException("Cannot add negative number of flats.");
        flatsAvailable.put(type, flatsAvailable.getOrDefault(type, 0) + count);
    }    

    public void removeFlats(FlatType flatType, int count) {
        if (count < 0) throw new IllegalArgumentException("Cannot remove negative number of flats.");
        if (flatsAvailable.containsKey(flatType)) {
            int currentCount = flatsAvailable.get(flatType);
            if (currentCount >= count) {
                flatsAvailable.put(flatType, currentCount - count);
            } else {
                throw new IllegalArgumentException("Not enough flats of type " + flatType + ". Requested: " + count + ", Available: " + currentCount);
            }
        } else {
            throw new IllegalArgumentException("Flat type not found.");
        }
    }

    public boolean hasAvailableOfficerSlots() {
        return availableOfficerSlots > 0;
    }

    public void decreaseOfficerSlot() {
        if (availableOfficerSlots > 0) {
            availableOfficerSlots--;
        } else {
            throw new IllegalStateException("No available officer slots.");
        }
    }

    public void increaseOfficerSlot() {
        if (availableOfficerSlots < MAX_OFFICER_SLOTS) {
            availableOfficerSlots++;
        } else {
            throw new IllegalStateException("Maximum officer slots reached.");
        }
    }
    
    public boolean isOpenForApplication() {
        LocalDate today = LocalDate.now();
        return (today.isAfter(openDate) || today.isEqual(openDate)) && (today.isBefore(closeDate) || today.isEqual(closeDate));
    }

    public void makeVisible() {
        this.setVisibilityStatus(VisibilityStatus.VISIBLE);
    }

    public void makeHidden() {
        this.setVisibilityStatus(VisibilityStatus.HIDDEN);
    }

    public String getSummary() {
        return String.format("Project Name: %s, Neighborhood: %s, Open Date: %s, Close Date: %s, Available Room Types: %s, Selling Price: %s",
                projectName, neighborhood, openDate, closeDate, flatsAvailable.keySet(), sellingPrice.values());
    }

    public void setFlatsAvailable(Map<FlatType, Integer> flatsAvailable) {
        this.flatsAvailable = new HashMap<>(flatsAvailable);
    }


}
