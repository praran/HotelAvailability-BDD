package com.peregrine.travelagent;

import com.peregrine.BookingService;
import com.peregrine.HotelAvailability;
import com.peregrine.booking.Booking;
import com.peregrine.booking.ProductionBookingSystem;
import com.peregrine.clock.Clock;
import com.peregrine.clock.SystemClock;
import com.peregrine.pricecalculator.BookingPriceCalculator;
import com.peregrine.pricecalculator.BookingPriceCalculatorImpl;
import com.peregrine.quotes.QuotesHolder;
import com.peregrine.quotes.QuotesHolderImpl;
import com.peregrine.search.Offer;
import com.peregrine.search.ProductionHotelDatabase;
import com.peregrine.search.Quote;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class PeregrineOnlineTravelAgent implements TravelAgent {

    // Collaborators with the travel agent
    private final BookingService productionBookingSystem;
    private final HotelAvailability productionHotelDB;
    private final Clock clock;
    private final BookingPriceCalculator bookingPriceCalculator;
    private final QuotesHolder quotesHolder;


    public PeregrineOnlineTravelAgent(){
        this(ProductionHotelDatabase.getInstance(),
                new QuotesHolderImpl(),
                new SystemClock(),
                ProductionBookingSystem.getInstance(),
                new BookingPriceCalculatorImpl());
    }

    public PeregrineOnlineTravelAgent(HotelAvailability productionHotelDB,
                                      QuotesHolder quotesHolder,
                                      Clock clock,
                                      BookingService bookingService,
                                      BookingPriceCalculator bookingPriceCalculator) {
        this.productionHotelDB = productionHotelDB;
        this.quotesHolder = quotesHolder;
        this.clock = clock;
        productionBookingSystem = bookingService;
        this.bookingPriceCalculator = bookingPriceCalculator;
    }


    /**
     * Search for hotels based on destination , check-in date and number of nights
     * @param destination
     * @param checkinDate
     * @param numberOfNights
     * @return
     */
    public List<Offer> searchForHotels(String destination, String checkinDate, int numberOfNights) {
        List<Offer> searchResults = productionHotelDB.searchFor(destination, checkinDate, numberOfNights);
        for (Offer offer : searchResults) {
            quotesHolder.addQuote(new Quote(offer, System.currentTimeMillis()));
        }
        return searchResults;
    }

    /**
     * Confirm the booking based on UUID and auth token
     * @param id
     * @param userAuthToken
     */
    public void confirmBooking(UUID id, String userAuthToken) {
        if (!quotesHolder.containsQuotes(id)) {
            throw new NoSuchElementException("Offer ID is invalid");
        }

        Quote quote = quotesHolder.getQuote(id);
        long timeNow = clock.getCurrentTimeInMs();
        BigDecimal totalPrice = bookingPriceCalculator.getBookingPrice(quote, timeNow);
        productionBookingSystem.process(new Booking(totalPrice, quote, timeNow, userAuthToken));
    }




}