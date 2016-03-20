package unittests.utils;

import com.peregrine.booking.Booking;
import com.peregrine.search.Quote;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;


public class TestUtils {


    /**
     * Factory method for quotes matcher
     * @param quote
     * @return
     */
    public static Matcher<Quote> quotesMatcher( Quote quote ) {
        return new QuotesMatcher(quote);
    }

    /**
     * Factory method for booking matcher
     * @param booking
     * @return
     */
    public static Matcher<Booking> bookingMatcher(Booking booking){
        return new BookingMatcher(booking);
    }


    /**
     * Matcher for Quotes
     */
    static class QuotesMatcher extends TypeSafeMatcher<Quote> {

        private Quote expectedQuote;

        QuotesMatcher(Quote quote) {
            this.expectedQuote = quote;
        }


        @Override
        protected boolean matchesSafely(Quote quote) {
            return this.expectedQuote.offer.id.equals(quote.offer.id);
        }

        public void describeTo(Description description) {
            description.appendText("Expected expectedQuote with offer: "+this.expectedQuote.offer.id);
        }
    }


    /**
     * Matcher for booking
     */
    static class BookingMatcher extends TypeSafeMatcher<Booking>{

        private final Booking expectedBooking;

        public BookingMatcher(Booking booking){
            this.expectedBooking = booking;
        }

        @Override
        protected boolean matchesSafely(Booking booking) {
            return expectedBooking.totalPrice.equals(booking.totalPrice)
                    && expectedBooking.quote.offer.id.equals(booking.quote.offer.id)
                    && expectedBooking.userAuthToken.equalsIgnoreCase(booking.userAuthToken);
        }

        public void describeTo(Description description) {
            description.appendText("expected booking price : "+expectedBooking.quote.offer.price +
                    " expected offer id : " + expectedBooking.quote.offer.id
            +       " expected token : "+ expectedBooking.userAuthToken);
        }
    }
}
