package unittests;


import com.peregrine.BookingService;
import com.peregrine.HotelAvailability;
import com.peregrine.booking.Booking;
import com.peregrine.pricecalculator.BookingPriceCalculator;
import com.peregrine.quotes.QuotesHolder;
import com.peregrine.search.Offer;
import com.peregrine.search.Quote;
import com.peregrine.travelagent.PeregrineOnlineTravelAgent;
import com.peregrine.travelagent.TravelAgent;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import unittests.utils.ControllableClock;
import unittests.utils.TestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.peregrine.constants.HolidayBookingSystemConstants.TWO_MINUTES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static unittests.utils.ControllableClock.getControllableClock;

public class PeregrineOnlineTravelAgentTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private HotelAvailability productionHotelDB = context.mock(HotelAvailability.class);
    private QuotesHolder quotesHolder = context.mock(QuotesHolder.class);
    private BookingService bookingService = context.mock(BookingService.class);
    private BookingPriceCalculator bookingPriceCalculator = context.mock(BookingPriceCalculator.class);
    private ControllableClock clock = getControllableClock();

    // SUT
    private TravelAgent peregrineOnlineTravelAgent;

    private UUID id = UUID.randomUUID();
    final Offer offer = new Offer("offer for test hotel", "30/01/2016", 4, new BigDecimal(10));
    final List<Offer> offerList = new ArrayList<Offer>();
    final Quote quote = new Quote(offer, System.currentTimeMillis());

    @Before
    public void setUp() {
        clock.setCurrentTimeInMs(System.currentTimeMillis());
        peregrineOnlineTravelAgent = new PeregrineOnlineTravelAgent(productionHotelDB,
                quotesHolder, clock, bookingService, bookingPriceCalculator);
    }


    @Test
    public void returnsAListOfOffersForDestinationAndCheckingDateAndNoOfNights() {
        offerList.add(offer);

        context.checking(new Expectations() {{
            exactly(1).of(productionHotelDB).searchFor("london", "30/01/2016", 4);
            will(returnValue(offerList));
            Quote quote = new Quote(offer, 1l);
            exactly(1).of(quotesHolder).addQuote(with(TestUtils.quotesMatcher(quote)));
        }});
        List<Offer> returnOffers = peregrineOnlineTravelAgent.searchForHotels("london", "30/01/2016", 4);
        assertThat(returnOffers, notNullValue());

    }

    @Test(expected = NoSuchElementException.class)
    public void throwsNoSuchElementExceptionForInvalidOfferIDs() {
        context.checking(new Expectations(){{
            exactly(1).of(quotesHolder).containsQuotes(id);
            will(returnValue(false));
        }});
        peregrineOnlineTravelAgent.confirmBooking(id, "authToken");
    }

    @Test(expected = IllegalStateException.class)
    public void throwsIllegalStateExceptionForExpiredOfferIDs() {

        context.checking(new Expectations(){{
            exactly(1).of(quotesHolder).containsQuotes(id);will(returnValue(true));
            exactly(1).of(quotesHolder).getQuote(id);will(returnValue(quote));
            exactly(1).of(bookingPriceCalculator).getBookingPrice(quote, clock.getCurrentTimeInMs());
            will(throwException(new IllegalStateException("offer expired")));
        }});
        peregrineOnlineTravelAgent.confirmBooking(id, "authToken");
    }


    @Test
    public void confirmBookingForValidQuotesAndApplyAppropriateBookingCharges() {
        clock.setCurrentTimeInMs(System.currentTimeMillis() + (TWO_MINUTES + 1));
        offerList.add(offer);

        final Booking booking = new Booking(new BigDecimal(10),quote, 1l, "authToken");
        context.checking(new Expectations(){{
            exactly(1).of(quotesHolder).containsQuotes(id);will(returnValue(true));
            exactly(1).of(quotesHolder).getQuote(id);will(returnValue(quote));
            exactly(1).of(bookingPriceCalculator).getBookingPrice(quote, clock.getCurrentTimeInMs());
            will(returnValue(new BigDecimal(10)));
            exactly(1).of(bookingService).process(with(TestUtils.bookingMatcher(booking)));
        }});

        peregrineOnlineTravelAgent.confirmBooking(id, "authToken");
    }

}

