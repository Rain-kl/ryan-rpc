package io.ryan.ratelimit;

import io.ryan.ratelimit.impl.NoRateLimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public enum RateLimitRegistry {
    INSTANCE;

    private static final RateLimit defaultRateLimit = new NoRateLimit();

    private final ConcurrentMap<String, RateLimit> map =
            new ConcurrentHashMap<>();

    public RateLimit get(String interfaceName) {
        if (!map.containsKey(interfaceName)) {
            return defaultRateLimit;
        }
        return map.get(interfaceName);
    }

    public void put(String interfaceName, RateLimit rl) {
        map.put(interfaceName, rl);
    }

    public void remove(String interfaceName) {
        map.remove(interfaceName);
    }

    public ConcurrentMap<String, RateLimit> getAll() {
        return map;
    }
}
