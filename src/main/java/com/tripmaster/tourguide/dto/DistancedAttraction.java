package com.tripmaster.tourguide.dto;

import gpsUtil.location.Attraction;

public class DistancedAttraction {
    private final Attraction attraction;
    private final double distance;

    public DistancedAttraction(Attraction attraction, double distance) {
        this.attraction = attraction;
        this.distance = distance;
    }

    public Attraction getAttraction() {
        return attraction;
    }

    public double getDistance() {
        return distance;
    }
}
