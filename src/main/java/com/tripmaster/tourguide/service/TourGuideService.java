package com.tripmaster.tourguide.service;

import com.tripmaster.tourguide.helper.InternalTestHelper;
import com.tripmaster.tourguide.model.tracker.Tracker;
import com.tripmaster.tourguide.model.user.User;
import com.tripmaster.tourguide.model.user.UserReward;
import com.tripmaster.tourguide.util.Futures;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
    private final Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty()) ?
                user.getLastVisitedLocation() :
                trackUserLocation(user);
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = tripPricer.getPrice(TEST_SERVER_API_KEY, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {

        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        rewardsService.calculateRewards(user);
        user.addToVisitedLocations(visitedLocation);

        return visitedLocation;
    }

    public List<VisitedLocation> trackUsersLocations(List<User> users)  {

        ExecutorService service = Executors.newFixedThreadPool(50);

        try {
            return Futures.listCompleted(
                    users.stream().map(user -> CompletableFuture.supplyAsync(
                                    () -> trackUserLocation(user), service
                            ))
                            .collect(Collectors.toList()
            ));

        } catch (ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
    }

    public List<Attraction> getNearbyAttractions(VisitedLocation visitedLocation) {
        List<Attraction> nearbyAttractions = new CopyOnWriteArrayList<>();

        for (Attraction attraction : gpsUtil.getAttractions()) {

            if (nearbyAttractions.size() < 5)   {
                nearbyAttractions.add(attraction);
            } else {
                for (Attraction nearbyAttraction : nearbyAttractions)   {
                    if (rewardsService.getDistance(attraction, visitedLocation.location)
                    < rewardsService.getDistance(nearbyAttraction, visitedLocation.location))   {
                        nearbyAttractions.remove(nearbyAttraction);
                        nearbyAttractions.add(attraction);
                    }
                }
            }
        }

        return nearbyAttractions;
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    /**********************************************************************************
     *
     * Methods Below: For Internal Testing
     *
     **********************************************************************************/
    private static final String TEST_SERVER_API_KEY = "test-server-api-key";
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    private void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created {} internal test users.", InternalTestHelper.getInternalUserNumber());
    }

    private void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i -> user.addToVisitedLocations(
                new VisitedLocation(user.getUserId(),
                        new Location(generateRandomLatitude(),
                                generateRandomLongitude()),
                        getRandomTime())
        ));
    }

    private double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    private Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
