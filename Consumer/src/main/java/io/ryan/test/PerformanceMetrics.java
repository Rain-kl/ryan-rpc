package io.ryan.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 性能指标收集器
 */
public class PerformanceMetrics {
    
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    // 存储所有响应时间用于计算百分位数
    private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
    
    private long startTime;
    private long endTime;
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    public void recordRequest(long responseTime, boolean success) {
        totalRequests.increment();
        if (success) {
            successfulRequests.increment();
            totalResponseTime.addAndGet(responseTime);
            responseTimes.add(responseTime);
        } else {
            failedRequests.increment();
        }
    }
    
    public long getTotalRequests() {
        return totalRequests.sum();
    }
    
    public long getSuccessfulRequests() {
        return successfulRequests.sum();
    }
    
    public long getFailedRequests() {
        return failedRequests.sum();
    }
    
    public double getSuccessRate() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) getSuccessfulRequests() / total * 100;
    }
    
    public double getAverageResponseTime() {
        long successful = getSuccessfulRequests();
        return successful == 0 ? 0.0 : (double) totalResponseTime.get() / successful;
    }
    
    public double getThroughput() {
        long duration = endTime - startTime;
        return duration == 0 ? 0.0 : (double) getSuccessfulRequests() / duration * 1000; // QPS
    }
    
    public long getTestDuration() {
        return endTime - startTime;
    }
    
    /**
     * 计算响应时间的百分位数
     */
    public long getPercentile(double percentile) {
        if (responseTimes.isEmpty()) {
            return 0;
        }
        
        List<Long> sortedTimes = new ArrayList<>(responseTimes);
        Collections.sort(sortedTimes);
        
        int index = (int) Math.ceil(sortedTimes.size() * percentile / 100.0) - 1;
        index = Math.max(0, Math.min(index, sortedTimes.size() - 1));
        
        return sortedTimes.get(index);
    }
    
    public long getMinResponseTime() {
        return responseTimes.isEmpty() ? 0 : Collections.min(responseTimes);
    }
    
    public long getMaxResponseTime() {
        return responseTimes.isEmpty() ? 0 : Collections.max(responseTimes);
    }
    
    /**
     * 生成详细的测试报告
     */
    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("\n==================== 压力测试报告 ====================\n");
        report.append(String.format("测试时长: %d ms (%.2f 秒)\n", 
            getTestDuration(), getTestDuration() / 1000.0));
        report.append(String.format("总请求数: %d\n", getTotalRequests()));
        report.append(String.format("成功请求数: %d\n", getSuccessfulRequests()));
        report.append(String.format("失败请求数: %d\n", getFailedRequests()));
        report.append(String.format("成功率: %.2f%%\n", getSuccessRate()));
        report.append(String.format("平均响应时间: %.2f ms\n", getAverageResponseTime()));
        report.append(String.format("吞吐量(QPS): %.2f 请求/秒\n", getThroughput()));
        
        if (!responseTimes.isEmpty()) {
            report.append("\n响应时间分布:\n");
            report.append(String.format("最小响应时间: %d ms\n", getMinResponseTime()));
            report.append(String.format("最大响应时间: %d ms\n", getMaxResponseTime()));
            report.append(String.format("50%% 响应时间: %d ms\n", getPercentile(50)));
            report.append(String.format("75%% 响应时间: %d ms\n", getPercentile(75)));
            report.append(String.format("90%% 响应时间: %d ms\n", getPercentile(90)));
            report.append(String.format("95%% 响应时间: %d ms\n", getPercentile(95)));
            report.append(String.format("99%% 响应时间: %d ms\n", getPercentile(99)));
        }
        
        report.append("===================================================\n");
        return report.toString();
    }
}