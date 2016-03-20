package com.peregrine.quotes;

import com.peregrine.search.Quote;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Helper class to holds the quotes
 */

public class QuotesHolderImpl implements QuotesHolder {

    private Map<UUID, Quote> quotes;

    public QuotesHolderImpl() {
        this.quotes = new HashMap<UUID, Quote>();
    }

    public boolean containsQuotes(UUID id) {
        return quotes.containsKey(id);
    }


    public Quote getQuote(UUID id) {
        return quotes.get(id);
    }

    public void addQuote(Quote quote) {
        this.quotes.put(quote.offer.id, quote);
    }


}
