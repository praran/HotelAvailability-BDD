package com.peregrine.quotes;

import com.peregrine.search.Quote;

import java.util.UUID;


public interface QuotesHolder {
    boolean containsQuotes(UUID id);

    Quote getQuote(UUID id);

    void addQuote(Quote quote);
}
