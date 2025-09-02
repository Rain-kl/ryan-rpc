package io.ryan.ratelimit;

import io.ryan.ratelimit.impl.NoRateLimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum RateLimitRegistry {
    INSTANCE;

    private static final RateLimit defaultRateLimit = new NoRateLimit();

    private final ConcurrentMap<String, RateLimit> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimit get(String interfaceName) {
        if (!rateLimitMap.containsKey(interfaceName)) {
            return defaultRateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }

    public void put(String interfaceName, RateLimit rl) {
        rateLimitMap.put(interfaceName, rl);
    }

    public void remove(String interfaceName) {
        rateLimitMap.remove(interfaceName);
    }

    public ConcurrentMap<String, RateLimit> getAll() {
        return rateLimitMap;
    }
}
