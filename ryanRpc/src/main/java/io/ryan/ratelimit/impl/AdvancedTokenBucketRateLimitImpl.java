package io.ryan.ratelimit.impl;

import io.ryan.ratelimit.RateLimit;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

enum RateLimitWeight {
    INSTANCE;

    private static final ConcurrentHashMap<String, Integer> WeightMap = new ConcurrentHashMap<>();

    public void put(String interfaceName, int weight) {
        WeightMap.put(interfaceName, weight);
    }

    public int get(String interfaceName) {
        return WeightMap.getOrDefault(interfaceName, 1);
    }

    public void remove(String interfaceName) {
        WeightMap.remove(interfaceName);
    }

    public ConcurrentHashMap<String, Integer> getAll() {
        return WeightMap;
    }
}

/**
 * 标准 Token Bucket：
 * - tokensPerSecond：每秒补充多少令牌（可为小数）
 * - capacity：桶最大容量
 * - 使用 System.nanoTime()，避免系统时间回拨影响
 */
public class AdvancedTokenBucketRateLimitImpl implements RateLimit {

//    方法/参数维度的“成本权重”限流

    private final double tokensPerSecond;
    private final double capacity;

    // 当前令牌数量，可为小数，先补后扣
    private double tokens;

    // 上次补桶的时间（纳秒，单调时钟）
    private long lastRefillNanos;

    public AdvancedTokenBucketRateLimitImpl(double tokensPerSecond, double capacity) {
        if (tokensPerSecond <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("tokensPerSecond 和 capacity 必须为正数");
        }
        this.tokensPerSecond = tokensPerSecond;
        this.capacity = capacity;
        this.tokens = capacity;              // 刚启动视为满桶
        this.lastRefillNanos = System.nanoTime();
    }

    public void setWeight(String interfaceName, int weight) {
        RateLimitWeight.INSTANCE.put(interfaceName, weight);
    }

    public void setWeight(Class<?> clazz, int weight) {
        Arrays.stream(clazz.getInterfaces()).forEach(p -> RateLimitWeight.INSTANCE.put(p.getName(), weight));
    }

    public Integer getWeight(String interfaceName) {
        return RateLimitWeight.INSTANCE.get(interfaceName);
    }

    @Override
    public synchronized boolean getToken(String interfaceName) {
        refill();
        if (tokens >= 1.0) {
            System.out.println("TokenBucketRateLimit granted for " + interfaceName + ", tokens left: " + tokens);
            tokens -= getWeight(interfaceName);
            return true;
        }
        return false;
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillNanos;
        if (elapsedNanos <= 0) return;

        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;
        double newTokens = elapsedSeconds * tokensPerSecond;

        if (newTokens > 0) {
            tokens = Math.min(capacity, tokens + newTokens);
            lastRefillNanos = now; // 记为当前时刻（也可推进 lastRefillNanos += consumedIntervalNanos，更精细）
        }
    }
}