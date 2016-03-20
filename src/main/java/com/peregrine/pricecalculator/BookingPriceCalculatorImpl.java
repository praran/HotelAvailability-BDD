package com.peregrine.pricecalculator;

import com.peregrine.search.Quote;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import static com.peregrine.constants.HolidayBookingSystemConstants.*;


public class BookingPriceCalculatorImpl implements BookingPriceCalculator {


    /**
     * Gets the total final price for the quote and confirmation time
     * @param quote
     * @param confirmationTime
     * @return
     */
    public BigDecimal getBookingPrice(Quote quote, long confirmationTime) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        long elapsedTime = confirmationTime - quote.timestamp;
        BigDecimal originalOfferPrice = quote.offer.price;

        if(elapsedTime <= TWO_MINUTES){
            totalPrice = originalOfferPrice;
        }else if(elapsedTime <= TEN_MINUTES){
            BigDecimal pPricePerNight = get5percentOfPricePerNight(quote);
            BigDecimal processingCharge = (pPricePerNight.compareTo(STANDARD_PROCESSING_CHARGE) <= 0) ? pPricePerNight : STANDARD_PROCESSING_CHARGE;
            totalPrice = originalOfferPrice.add(processingCharge);
        }else if(elapsedTime >= ELEVEN_MINUTES && elapsedTime <= TWENTY_MINUTES){
            totalPrice = originalOfferPrice.add(STANDARD_PROCESSING_CHARGE_2);
        }else if (elapsedTime > MAX_QUOTE_AGE_MILLIS) {
            throw new IllegalStateException("Quote expired, please get a new price");
        }


        if(getNoOfMonthsFromCheckin(quote) >= THREE_MONTHS){
            totalPrice = totalPrice.multiply(BigDecimal.valueOf(0.90));
        }

        return totalPrice;
    }

    /**
     * Helper method to get the 5 % of the nightly price of the offer
     * @param quote
     * @return
     */
    private BigDecimal get5percentOfPricePerNight(Quote quote) {
        BigDecimal pricePerNight = quote.offer.price.divide(new BigDecimal(quote.offer.numberOfNights));
        return pricePerNight.multiply(BigDecimal.valueOf(0.05));
    }


    /**
     * Helper method to get the number of month in advance from now is the check-in date
     * @param quote
     * @return
     */
    private int getNoOfMonthsFromCheckin(Quote quote) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TemporalAccessor parsedDate = dateTimeFormatter.parse(quote.offer.checkinDate);
        LocalDate checkinDate = LocalDate.from(parsedDate);
        return Period.between(LocalDate.now(), checkinDate).getMonths();
    }
}
