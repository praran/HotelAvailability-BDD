package acceptancetests;

import com.peregrine.BookingService;
import com.peregrine.booking.Booking;
import com.peregrine.constants.HolidayBookingSystemConstants;
import com.peregrine.pricecalculator.BookingPriceCalculatorImpl;
import com.peregrine.quotes.QuotesHolderImpl;
import com.peregrine.search.Offer;
import com.peregrine.search.ProductionHotelDatabase;
import com.peregrine.search.Quote;
import com.peregrine.travelagent.PeregrineOnlineTravelAgent;
import com.peregrine.travelagent.TravelAgent;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Assert;
import org.junit.Rule;
import unittests.utils.ControllableClock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static java.math.BigDecimal.valueOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static unittests.utils.TestUtils.bookingMatcher;


public class PeregrineHolidayBookingFeatureTests {

    private TravelAgent peregrineOnlineTravelAgent;
    private List<Offer> offers;
    private UUID uuid;
    private String authToken;

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private BookingService bookingService = context.mock(BookingService.class);
    private ControllableClock clock = ControllableClock.getControllableClock();


    private Offer offer;
    private Quote quote;

    @Given("^Peregrine holiday booking system is ready and available$")
    public void Peregrine_holiday_booking_system_is_ready_and_available() throws Throwable {
        clock.setCurrentTimeInMs(System.currentTimeMillis());
        peregrineOnlineTravelAgent = new PeregrineOnlineTravelAgent(ProductionHotelDatabase.getInstance(), new QuotesHolderImpl(), clock, bookingService, new BookingPriceCalculatorImpl());
    }

    @When("^client search for holiday in \"([^\"]*)\" on date \"([^\"]*)\" for (\\d+) nights$")
    public void client_search_for_holiday_in_on_date_for_nights(String destination, String date, int noOfNights) throws Throwable {
        offers = peregrineOnlineTravelAgent.searchForHotels(destination, date, noOfNights);
    }

    @Then("^client will be presented with a list of offers$")
    public void client_will_be_presented_with_a_list_of_offers() throws Throwable {
        assertThat(offers, notNullValue());
    }


    @When("^Client passes invalid UUID of the offer \"([^\"]*)\" and \"([^\"]*)\"$")
    public void Client_passes_invalid_UUID_of_the_offer_and(String uuid, String token) throws Throwable {
        this.uuid = UUID.fromString(uuid);
        this.authToken = token;
    }

    @Then("^Peregrine holiday booking system will throw Nosuch element exception$")
    public void Peregrine_holiday_booking_system_will_throw_no_such_element_exception() throws Throwable {
        try {
            this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
            throw new AssertionError("Exception expected");
        } catch (NoSuchElementException ex) {
            Assert.assertThat(ex.getMessage(), hasToString("Offer ID is invalid"));
        }
    }


    @When("^Client passes UUID of expired offer and token \"([^\"]*)\"$")
    public void Client_passes_UUID_of_expired_offer_and_token(String authToken) throws Throwable {
        this.authToken = authToken;
        this.uuid = offers.get(0).id;
    }

    @Then("^Peregrine holiday booking system will throw illegal state exception$")
    public void Peregrine_holiday_booking_system_will_throw_illegal_state_exception() throws Throwable {
        try {
            // refactoring done here made the method static constant public
            this.clock.setCurrentTimeInMs(System.currentTimeMillis() + (HolidayBookingSystemConstants.MAX_QUOTE_AGE_MILLIS));
            this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
            throw new AssertionError("illegal state Exception expected");
        } catch (IllegalStateException ex) {
            Assert.assertThat(ex.getMessage(), hasToString("Quote expired, please get a new price"));
        }
    }


    @When("^Client passes UUID of valid offer and token \"([^\"]*)\"$")
    public void Client_passes_UUID_of_valid_offer_and_token(String token) throws Throwable {
        this.uuid = offers.get(0).id;
        this.authToken = token;
    }

    @Then("^Peregrine holiday booking system will confirm booking$")
    public void Peregrine_holiday_booking_system_will_confirm_booking() throws Throwable {
      //  at this point add booking service mock
        Offer offer = offers.get(0);
        Quote quote = new Quote(offer, System.currentTimeMillis());
        BigDecimal expectedPrice = offer.price.add(HolidayBookingSystemConstants.STANDARD_PROCESSING_CHARGE);
        // Refactored here to + 2 minues
        clock.setCurrentTimeInMs(System.currentTimeMillis() + (HolidayBookingSystemConstants.TWO_MINUTES + 1));
        final Booking booking = new Booking(expectedPrice,quote, 1l, authToken );
        context.checking(new Expectations(){{
            exactly(1).of(bookingService).process(with(bookingMatcher(booking)));
        }});
        this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
    }

    @And("^Client confirms booking within two minutes$")
    public void Client_confirms_booking_within_two_minutes() throws Throwable {
        this.offer = offers.get(0);
        this.quote = new Quote(offer, System.currentTimeMillis());
        this.clock.setCurrentTimeInMs(System.currentTimeMillis() + 1 * 60 * 1000);
    }

    @Then("^Peregrine holiday booking system will confirm booking with no booking charges$")
    public void Peregrine_holiday_booking_system_will_confirm_booking_with_no_booking_charges() throws Throwable {
        BigDecimal expectedPrice = offer.price;
        final Booking booking = new Booking(expectedPrice,quote, 1l, authToken );
        context.checking(new Expectations(){{
            exactly(1).of(bookingService).process(with(bookingMatcher(booking)));
        }});
        this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
    }


    @And("^Client confirms booking greater than two minutes but less than (\\d+) minutes$")
    public void Client_confirms_booking_greater_than_two_minutes_but_less_than_minutes(int minutes) throws Throwable {
        this.offer = offers.get(0);
        this.quote = new Quote(offer, System.currentTimeMillis());
        this.clock.setCurrentTimeInMs(System.currentTimeMillis() + (minutes * 60 * 1000 - 60000));
    }

    @Then("^Confirm booking with processing charge (\\d+)% of room rate or £(\\d+) which ever is less$")
    public void Confirm_booking_with_processing_charge_of_room_rate_or_10£_which_ever_is_less(int percentage, int standardProcessingCharge) throws Throwable {
        BigDecimal pPCharge = getPercentOfPricePerNight(quote, percentage);
        BigDecimal processingCharge = (pPCharge.compareTo(valueOf(standardProcessingCharge)) <= 0) ? pPCharge : HolidayBookingSystemConstants.STANDARD_PROCESSING_CHARGE;
        BigDecimal expectedPrice = offer.price.add(processingCharge);
        final Booking booking = new Booking(expectedPrice,quote, 1l, authToken );

        context.checking(new Expectations(){{
            exactly(1).of(bookingService).process(with(bookingMatcher(booking)));
        }});

        this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
    }

    @And("^Client confirms booking between (\\d+) and (\\d+) minutes$")
    public void Client_confirms_booking_between_and_minutes(int lMinutes, int hMinutes) throws Throwable {
        this.offer = offers.get(0);
        this.quote = new Quote(offer, System.currentTimeMillis());
        this.clock.setCurrentTimeInMs(System.currentTimeMillis() + ((lMinutes + hMinutes) / 2) * 60 * 1000);
    }

    @Then("^Confirm booking with processing charge (\\d+)£$")
    public void Confirm_booking_with_processing_charge_£(int processingCharge) throws Throwable {
        BigDecimal expectedPrice = offer.price.add(valueOf(processingCharge));
        final Booking booking = new Booking(expectedPrice,quote, 1l, authToken );
        context.checking(new Expectations(){{
            exactly(1).of(bookingService).process(with(bookingMatcher(booking)));
        }});
        this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
    }

    @And("^Client books a holiday three months in advance$")
    public void Client_books_a_holiday_three_months_in_advance() throws Throwable {
        LocalDate toDate = LocalDate.now().plusMonths(3).plusDays(1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.offers = peregrineOnlineTravelAgent.searchForHotels("london",toDate.format(dateTimeFormatter),3);
    }

    @Then("^Confirm booking with (\\d+)% discount on total booking price$")
    public void Confirm_booking_with_discount_on_total_booking_price(int percentage) throws Throwable {
        BigDecimal expectedPrice = offer.price.add(HolidayBookingSystemConstants.STANDARD_PROCESSING_CHARGE_2);
        BigDecimal percent = valueOf(100).subtract(valueOf(percentage)).divide(valueOf(100));
        BigDecimal discountedPrice = expectedPrice.multiply(percent);
        final Booking booking = new Booking(discountedPrice,quote, 1l, authToken );

        context.checking(new Expectations(){{
            exactly(1).of(bookingService).process(with(bookingMatcher(booking)));
        }});

        this.peregrineOnlineTravelAgent.confirmBooking(uuid, authToken);
    }


    private BigDecimal getPercentOfPricePerNight(Quote quote, int percentage) {
        BigDecimal pricePerNight = quote.offer.price.divide(new BigDecimal(quote.offer.numberOfNights));
        BigDecimal percent = valueOf(percentage).divide(new BigDecimal(100));
        return pricePerNight.multiply(percent);
    }
}
