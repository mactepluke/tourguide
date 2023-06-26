package com.tripmaster.tourguide.controller;

import java.util.List;

import com.tripmaster.tourguide.dto.UserPreferencesDTO;
import com.tripmaster.tourguide.service.TourGuideService;
import com.tripmaster.tourguide.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tripPricer.Provider;

@RestController
public class TourGuideController {

	@Autowired
    TourGuideService tourGuideService;
	
    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }

    /**
     * Refactored this method
     * @param userName
     * @return
     */
    @RequestMapping("/getNearbyAttractions") 
    public String getNearbyAttractions(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getNearbyAttractions(getUser(userName)));
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }

    /**
     * Created this method
     * @return
     */
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {

    	return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }
    
    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
    	List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	return JsonStream.serialize(providers);
    }

    /**
     * Added this method
     * @param userName
     */
    @RequestMapping(path = "/updatePreferences", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updatePreferences(@RequestParam String userName, @RequestBody UserPreferencesDTO userPreferencesDTO) {
        tourGuideService.updatePreferences(getUser(userName), userPreferencesDTO);
    }

    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }

}