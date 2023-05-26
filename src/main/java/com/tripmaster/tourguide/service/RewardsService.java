package com.tripmaster.tourguide.service;

import com.tripmaster.tourguide.model.user.User;
import com.tripmaster.tourguide.model.user.UserReward;
import com.tripmaster.tourguide.util.Futures;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private static final int DEFAULT_PROXIMITY_BUFFER = 10;
    private int proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    private static final int ATTRACTION_PROXIMITY_RANGE = 200;
    private final GpsUtil gpsUtil;
    private final RewardCentral rewardsCentral;

    public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
        this.gpsUtil = gpsUtil;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = DEFAULT_PROXIMITY_BUFFER;
    }

    public void calculateUsersRewards(List<User> users) {

        ExecutorService service = Executors.newFixedThreadPool(50);

        try {
            Futures.waitCompleted(
                    users.stream().map(user -> CompletableFuture.runAsync(
                            () -> calculateRewards(user), service
                    )).collect(Collectors.toList())
            );
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions =  new CopyOnWriteArrayList<>(gpsUtil.getAttractions());
        List<UserReward> userRewards = new CopyOnWriteArrayList<>(user.getUserRewards());

        userLocations.forEach(
                visitedLocation -> attractions.forEach(
                        attraction -> {
                            if (userRewards.stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))
                                    && (nearAttraction(visitedLocation, attraction))) {
                                userRewards.add(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
                            }
                        }
                )
        );
        user.setUserRewards(new ArrayList<>(userRewards));
    }

    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return getDistance(attraction, location) <= ATTRACTION_PROXIMITY_RANGE;
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return getDistance(attraction, visitedLocation.location) <= proximityBuffer;
    }

    public int getRewardPoints(Attraction attraction, User user) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
    }

    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);

        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
