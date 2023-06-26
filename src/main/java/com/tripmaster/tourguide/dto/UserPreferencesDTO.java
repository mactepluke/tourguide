package com.tripmaster.tourguide.dto;

public class UserPreferencesDTO {
    private final int numberOfAdults;
    private final int numberOfChildren;
    private final int tripDuration;

    public UserPreferencesDTO(int numberOfAdults, int numberOfChildren, int tripDuration) {
        this.numberOfAdults = numberOfAdults;
        this.numberOfChildren = numberOfChildren;
        this.tripDuration = tripDuration;
    }

    public int getNumberOfAdults() {
        return numberOfAdults;
    }

    public int getNumberOfChildren() {
        return numberOfChildren;
    }

    public int getTripDuration() {
        return tripDuration;
    }
}
