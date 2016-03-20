package com.peregrine;

import com.peregrine.search.Offer;

import java.util.List;

public interface HotelAvailability {
    List<Offer> searchFor(String destination, String checkinDate, int numberOfNights);
}
