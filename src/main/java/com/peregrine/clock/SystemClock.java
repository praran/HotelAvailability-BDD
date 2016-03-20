package com.peregrine.clock;

public class SystemClock implements Clock {

    public long getCurrentTimeInMs() {
        return System.currentTimeMillis();
    }
}
