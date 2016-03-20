package unittests;

import com.peregrine.pricecalculator.BookingPriceCalculator;
import com.peregrine.pricecalculator.BookingPriceCalculatorImpl;
import com.peregrine.search.Offer;
import com.peregrine.search.Quote;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.peregrine.constants.HolidayBookingSystemConstants.MAX_QUOTE_AGE_MILLIS;
import static com.peregrine.constants.HolidayBookingSystemConstants.STANDARD_PROCESSING_CHARGE_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BookingPriceCalculatorImplTest {

    private BookingPriceCalculator bookingPriceCalculator;
    private Offer offer;
    private Quote quote;
    private long timeInMs;

    @Before
    public void setUp(){
        bookingPriceCalculator = new BookingPriceCalculatorImpl();
        timeInMs = System.currentTimeMillis();
        offer = new Offer("test offer", getTodayDate(), 4, new BigDecimal(100));

    }

    @Test(expected =  IllegalStateException.class)
    public void throwsIllegalStateExceptionWhenOfferIsExpired(){
        quote = new Quote(offer, timeInMs - (MAX_QUOTE_AGE_MILLIS +1));
        bookingPriceCalculator.getBookingPrice(quote, timeInMs);
    }

    @Test
    public void noProcessingChargeIfBookedWithin2Minutes(){
        quote = new Quote(offer, timeInMs - (1 * 60 * 1000));
        BigDecimal bookingPrice = bookingPriceCalculator.getBookingPrice(quote, timeInMs);
        assertThat(bookingPrice, equalTo(offer.price));
    }

    @Test
    public void applyProcessingChargeForConfirmationWithin10Minutes(){
        quote = new Quote(offer, timeInMs - (9 * 60 * 1000));
        BigDecimal pricePerNight = offer.price.divide(new BigDecimal(offer.numberOfNights));
        BigDecimal pOfferPrice = pricePerNight.multiply(BigDecimal.valueOf(new Double(0.05)));
        BigDecimal processingCharge = (pOfferPrice.compareTo(new BigDecimal(10)) <=0 )
                ? pOfferPrice : new BigDecimal(10);

        BigDecimal bookingPrice = bookingPriceCalculator.getBookingPrice(quote, timeInMs);
        assertThat(bookingPrice, equalTo(offer.price.add(processingCharge)));
    }

    @Test
    public void applyProcessingChargeForConfirmationBetween11and20Minutes(){
        quote = new Quote(offer, timeInMs - (13 * 60 * 1000));

        BigDecimal bookingPrice = bookingPriceCalculator.getBookingPrice(quote, timeInMs);
        assertThat(bookingPrice, equalTo(offer.price.add(STANDARD_PROCESSING_CHARGE_2)));
    }

    @Test
    public void discountedPriceWhenBookedInAdvanceOf3Months(){
        offer = new Offer("booking 3 months in advance", getDateThreeMonthsInAdvance(), 5, new BigDecimal(500));
        quote = new Quote(offer, timeInMs - (13 * 60 * 1000));
        BigDecimal totalPrice = offer.price.add(STANDARD_PROCESSING_CHARGE_2);
        BigDecimal discountedPrice = totalPrice.multiply(BigDecimal.valueOf(0.90));

        BigDecimal bookingPrice = bookingPriceCalculator.getBookingPrice(quote, timeInMs);
        assertThat(bookingPrice, equalTo(discountedPrice));
    }

    private String getTodayDate(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.now().format(formatter);
    }

    private String getDateThreeMonthsInAdvance(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.now().plusMonths(3).plusDays(1).format(formatter);
    }

}