package io.ryan.test;

import io.ryan.loadbalance.impl.RoundLoadBalance;
import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.impl.nacosImpl.NacosCenter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 快速压力测试类 - 简化版本
 */
public class QuickStressTest {
    
    public static void main(String[] args) {
        System.out.println("开始快速压力测试...");
        
        try {
            // 初始化RPC服务
            ProxyFactory.setServiceCenter(new NacosCenter("localhost", 8848, new RoundLoadBalance<>()));
            HelloService helloService = ProxyFactory.getProxy(HelloService.class);
            
            // 配置参数
            int threadCount = 50;      // 并发线程数
            int requestsPerThread = 1000; // 每个线程的请求数
            int totalRequests = threadCount * requestsPerThread;
            
            System.out.println("测试配置:");
            System.out.println("- 并发线程数: " + threadCount);
            System.out.println("- 每线程请求数: " + requestsPerThread);
            System.out.println("- 总请求数: " + totalRequests);
            System.out.println();
            
            // 创建线程池
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            // 性能统计
            final long[] responseTimes = new long[totalRequests];
            final boolean[] results = new boolean[totalRequests];
            final int[] requestIndex = {0};
            
            long startTime = System.currentTimeMillis();
            
            // 启动所有线程
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < requestsPerThread; j++) {
                            long reqStart = System.currentTimeMillis();
                            boolean success = false;
                            
                            try {
                                String result = helloService.sayHello("Thread-" + threadId + "-Request-" + j);
                                success = (result != null && !result.isEmpty());
                                if (!success) {
                                    System.err.println("Thread-" + threadId + " 收到空结果");
                                }
                            } catch (Exception e) {
                                System.err.println("Thread-" + threadId + " 请求失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                                if (j < 5) { // 只打印前几个详细堆栈
                                    e.printStackTrace();
                                }
                            }
                            
                            long reqTime = System.currentTimeMillis() - reqStart;
                            
                            // 记录结果
                            synchronized (requestIndex) {
                                int idx = requestIndex[0]++;
                                if (idx < totalRequests) {
                                    responseTimes[idx] = reqTime;
                                    results[idx] = success;
                                }
                            }
                        }
                        System.out.println("线程 " + threadId + " 完成");
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            // 等待所有线程完成
            latch.await();
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // 计算统计数据
            int successCount = 0;
            long totalResponseTime = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = 0;
            
            for (int i = 0; i < requestIndex[0]; i++) {
                if (results[i]) {
                    successCount++;
                    totalResponseTime += responseTimes[i];
                    minTime = Math.min(minTime, responseTimes[i]);
                    maxTime = Math.max(maxTime, responseTimes[i]);
                }
            }
            
            // 输出结果
            System.out.println("\n============= 测试结果 =============");
            System.out.println("总测试时间: " + totalTime + " ms (" + String.format("%.2f", totalTime/1000.0) + " 秒)");
            System.out.println("总请求数: " + requestIndex[0]);
            System.out.println("成功请求数: " + successCount);
            System.out.println("失败请求数: " + (requestIndex[0] - successCount));
            System.out.println("成功率: " + String.format("%.2f%%", (double)successCount / requestIndex[0] * 100));
            
            if (successCount > 0) {
                double avgResponseTime = (double)totalResponseTime / successCount;
                double qps = (double)successCount / totalTime * 1000;
                
                System.out.println("平均响应时间: " + String.format("%.2f ms", avgResponseTime));
                System.out.println("最小响应时间: " + minTime + " ms");
                System.out.println("最大响应时间: " + maxTime + " ms");
                System.out.println("吞吐量(QPS): " + String.format("%.2f", qps));
            }
            
            System.out.println("=====================================");
            
            executor.shutdown();
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}