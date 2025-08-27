package io.ryan.serviceCenter;

import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.impl.NoRateLimit;


public abstract class AbstractServiceCenter implements ServiceCenter {

    protected RateLimit globalRateLimit = new NoRateLimit();

    public void setGlobalRateLimit(RateLimit globalRateLimit) {
        this.globalRateLimit = globalRateLimit;
    }
}
