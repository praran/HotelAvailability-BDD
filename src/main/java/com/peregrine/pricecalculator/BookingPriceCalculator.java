package com.peregrine.pricecalculator;

import com.peregrine.search.Quote;

import java.math.BigDecimal;


public interface BookingPriceCalculator {
    BigDecimal getBookingPrice(Quote quote, long timeNow);
}
