package unittests.utils;

import com.peregrine.clock.Clock;

/**
 * Stub for clock implementation
 */
public class ControllableClock implements Clock {

    private long currentTimeInMs;

    public long getCurrentTimeInMs() {
        return currentTimeInMs;
    }

    public void setCurrentTimeInMs(long currentTimeInMs){
        this.currentTimeInMs = currentTimeInMs;
    }

    public static ControllableClock getControllableClock(){
        return new ControllableClock();
    }
}
