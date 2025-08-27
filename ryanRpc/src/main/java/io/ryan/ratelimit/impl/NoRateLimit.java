package io.ryan.ratelimit.impl;

import io.ryan.ratelimit.RateLimit;

public class NoRateLimit implements RateLimit {

    @Override
    public boolean getToken() {
        return true;
    }
}
