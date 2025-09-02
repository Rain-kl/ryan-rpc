package io.ryan.ratelimit;

public interface RateLimit {

    boolean getToken(String interfaceName);

}

