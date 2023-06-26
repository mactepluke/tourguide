package com.tripmaster.tourguide.dto;

public class TouristAttractionDTO {

    private final String attractionName;
    private final double attractionLongitude;
    private final double attractionLatitude;
    private final double userLongitude;
    private final double userLatitude;
    private final double distanceFromUser;
    private final int rewardPointsForVisiting;

    public TouristAttractionDTO(String attractionName,
                                double attractionLongitude,
                                double attractionLatitude,
                                double userLongitude,
                                double userLatitude,
                                double distanceFromUser,
                                int rewardPointsForVisiting) {
        this.attractionName = attractionName;
        this.attractionLongitude = attractionLongitude;
        this.attractionLatitude = attractionLatitude;
        this.userLongitude = userLongitude;
        this.userLatitude = userLatitude;
        this.distanceFromUser = distanceFromUser;
        this.rewardPointsForVisiting = rewardPointsForVisiting;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public double getAttractionLongitude() {
        return attractionLongitude;
    }

    public double getAttractionLatitude() {
        return attractionLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

    public int getRewardPointsForVisiting() {
        return rewardPointsForVisiting;
    }
}
