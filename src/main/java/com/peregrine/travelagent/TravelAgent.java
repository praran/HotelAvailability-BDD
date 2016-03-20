package com.peregrine.travelagent;

import com.peregrine.search.Offer;

import java.util.List;
import java.util.UUID;

public interface TravelAgent {

    List<Offer> searchForHotels(String destination, String checkinDate, int numberOfNights);

    void confirmBooking(UUID id, String userAuthToken);
}
