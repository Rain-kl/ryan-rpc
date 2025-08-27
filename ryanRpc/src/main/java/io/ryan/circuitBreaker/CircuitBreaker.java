package io.ryan.circuitBreaker;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class CircuitBreaker {

    // ---- 配置 ----
    private final int failureThreshold;          // 连续失败阈值（CLOSED -> OPEN）
    private final Duration retryDuration;        // OPEN 状态下等待多久进入 HALF_OPEN
    private final int halfOpenMaxCalls;          // HALF_OPEN 允许的最大探针请求数
    private final double halfOpenSuccessRate;    // HALF_OPEN 成功率阈值（0~1），达到样本上限后再判定
    // ---- 状态 ---- (全部由 synchronized 保护，可用普通类型)
    private State state = State.CLOSED;
    private long openSinceMs = 0L;               // 进入 OPEN 的时间点
    // CLOSED 下只统计连续失败
    private int consecutiveFailures = 0;
    // HALF_OPEN 下的采样计数
    private int halfOpenCalls = 0;
    private int halfOpenSuccesses = 0;
    public CircuitBreaker(int failureThreshold,
                          double halfOpenSuccessRate,
                          long retryTimePeriodMillis) {
        this(failureThreshold, halfOpenSuccessRate, retryTimePeriodMillis, 5);
    }

    public CircuitBreaker(int failureThreshold,
                          double halfOpenSuccessRate,
                          long retryTimePeriodMillis,
                          int halfOpenMaxCalls) {
        if (failureThreshold <= 0) throw new IllegalArgumentException("failureThreshold must be > 0");
        if (halfOpenSuccessRate <= 0 || halfOpenSuccessRate > 1.0)
            throw new IllegalArgumentException("halfOpenSuccessRate must be in (0,1]");
        if (halfOpenMaxCalls <= 0) throw new IllegalArgumentException("halfOpenMaxCalls must be > 0");

        this.failureThreshold = failureThreshold;
        this.retryDuration = Duration.ofMillis(retryTimePeriodMillis);
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.halfOpenMaxCalls = halfOpenMaxCalls;
    }

    // 是否允许通过
    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        return switch (state) {
            case OPEN -> {
                // 到时了 -> 进入 HALF_OPEN 并重置采样
                if (now - openSinceMs >= retryDuration.toMillis()) {
                    transitionToHalfOpen();
                    // 放行 1 个探针
                    halfOpenCalls++; // 计入本次
                    yield true;
                }
                log.error("Circuit Breaker is OPEN! reject request!");
                yield false;
            }
            case HALF_OPEN -> {
                // 仅允许有限个探针请求
                if (halfOpenCalls < halfOpenMaxCalls) {
                    halfOpenCalls++;
                    yield true;
                }
                yield false;
            }
            default -> true;
        };
    }

    // 调用成功
    public synchronized void recordSuccess() {
        switch (state) {
            case CLOSED:
                // 连续失败被打断
                consecutiveFailures = 0;
                break;

            case HALF_OPEN:
                halfOpenSuccesses++;
                if (halfOpenCalls >= halfOpenMaxCalls) {
                    double rate = (halfOpenCalls == 0) ? 0.0 : (halfOpenSuccesses * 1.0 / halfOpenCalls);
                    if (rate >= halfOpenSuccessRate) {
                        transitionToClosed();
                    } else {
                        transitionToOpen(); // 采样结束且成功率不达标
                    }
                }
                break;

            case OPEN:
                // OPEN 下不应有调用成功（因为一般不放行），忽略
                break;
        }
    }

    // 调用失败
    public synchronized void recordFailure() {
        switch (state) {
            case CLOSED:
                consecutiveFailures++;
                if (consecutiveFailures >= failureThreshold) {
                    transitionToOpen();
                }
                break;

            case HALF_OPEN:
                // HALF_OPEN 有失败 -> 立即回到 OPEN
                transitionToOpen();
                break;

            case OPEN:
                // 仍在冷却，忽略
                break;
        }
    }

    public synchronized State getState() {
        return state;
    }

    // ---- 状态迁移 ----
    private void transitionToOpen() {
        state = State.OPEN;
        openSinceMs = System.currentTimeMillis();
        // 清理计数
        consecutiveFailures = 0;
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
    }

    private void transitionToHalfOpen() {
        state = State.HALF_OPEN;
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
    }

    private void transitionToClosed() {
        state = State.CLOSED;
        consecutiveFailures = 0;
        halfOpenCalls = 0;
        halfOpenSuccesses = 0;
    }

    public enum State { CLOSED, OPEN, HALF_OPEN }
}