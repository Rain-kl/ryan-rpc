package io.ryan.test;

import io.ryan.loadbalance.impl.RoundLoadBalance;
import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.impl.nacosImpl.NacosCenter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RPC框架压力测试主类
 */
public class RpcStressTest {
    
    private static final String NACOS_HOST = "localhost";
    private static final int NACOS_PORT = 8848;
    
    private HelloService helloService;
    private ExecutorService executorService;
    
    public static void main(String[] args) {
        RpcStressTest test = new RpcStressTest();

        try {
            test.initialize();

            // 根据命令行参数选择测试类型
            if (true) {
                String testType = "concurrent";

                switch (testType) {
                    case "single":
                        int requestCount = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
                        test.runSingleThreadTest(requestCount);
                        break;

                    case "concurrent":
                        int threadCount = args.length > 1 ? Integer.parseInt(args[1]) : 10;
                        int requestsPerThread = args.length > 2 ? Integer.parseInt(args[2]) : 100;
                        test.runConcurrentTest(threadCount, requestsPerThread);
                        break;

                    case "stability":
                        int stabThreadCount = args.length > 1 ? Integer.parseInt(args[1]) : 10;
                        long durationMinutes = args.length > 2 ? Long.parseLong(args[2]) : 5;
                        test.runStabilityTest(stabThreadCount, durationMinutes);
                        break;

                    case "gradual":
                        test.runGradualStressTest();
                        break;

                    default:
                        System.out.println("未知的测试类型: " + testType);
                        printUsage();
                }
            } else {
                // 默认运行简单的测试套件
                System.out.println("运行默认测试套件...");
                test.runSingleThreadTest(100);
                test.runConcurrentTest(10, 50);
            }

        } catch (Exception e) {
            System.err.println("测试执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            test.shutdown();
        }
    }
    
    private static void printUsage() {
        System.out.println("\n使用方法:");
        System.out.println("java io.ryan.test.RpcStressTest [测试类型] [参数...]");
        System.out.println("\n测试类型:");
        System.out.println("  single [请求数]              - 单线程基准测试 (默认1000)");
        System.out.println("  concurrent [线程数] [每线程请求数] - 多线程并发测试 (默认10, 100)");
        System.out.println("  stability [线程数] [测试分钟数]    - 长时间稳定性测试 (默认10, 5)");
        System.out.println("  gradual                      - 渐进式压力测试");
        System.out.println("\n示例:");
        System.out.println("  java io.ryan.test.RpcStressTest single 2000");
        System.out.println("  java io.ryan.test.RpcStressTest concurrent 20 200");
        System.out.println("  java io.ryan.test.RpcStressTest stability 5 10");
    }
    
    public void initialize() throws InterruptedException {
        // 初始化服务中心和代理
        ProxyFactory.setServiceCenter(new NacosCenter(NACOS_HOST, NACOS_PORT, new RoundLoadBalance<>()));
        helloService = ProxyFactory.getProxy(HelloService.class);
        System.out.println("RPC服务代理初始化完成");
    }
    
    /**
     * 单线程基准测试
     */
    public void runSingleThreadTest(int requestCount) {
        System.out.println("\n开始单线程基准测试...");
        System.out.println("请求数量: " + requestCount);

        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setStartTime(System.currentTimeMillis());

        for (int i = 0; i < requestCount; i++) {
            long startTime = System.currentTimeMillis();
            boolean success = false;

            try {
                String result = helloService.sayHello("User-" + i);
                success = result != null && !result.isEmpty();
                if (i % 100 == 0) { // 每100个请求打印一次进度
                    System.out.println("已完成: " + (i + 1) + "/" + requestCount);
                }
            } catch (Exception e) {
                System.err.println("请求失败: " + e.getMessage());
            }

            long responseTime = System.currentTimeMillis() - startTime;
            metrics.recordRequest(responseTime, success);
        }

        metrics.setEndTime(System.currentTimeMillis());
        System.out.println(metrics.generateReport());
    }
    
    /**
     * 多线程并发测试
     */
    public void runConcurrentTest(int threadCount, int requestsPerThread) {
        System.out.println("\n开始多线程并发测试...");
        System.out.println("线程数: " + threadCount + ", 每线程请求数: " + requestsPerThread);
        System.out.println("总请求数: " + (threadCount * requestsPerThread));

        executorService = Executors.newFixedThreadPool(threadCount);
        PerformanceMetrics metrics = new PerformanceMetrics();
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger completedThreads = new AtomicInteger(0);

        metrics.setStartTime(System.currentTimeMillis());

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        boolean success = false;

                        try {
                            String result = helloService.sayHello("Thread-" + threadId + "-Request-" + j);
                            success = result != null && !result.isEmpty();
                        } catch (Exception e) {
                            System.err.println("Thread-" + threadId + " 请求失败: " + e.getMessage());
                        }

                        long responseTime = System.currentTimeMillis() - startTime;
                        metrics.recordRequest(responseTime, success);
                    }

                    int completed = completedThreads.incrementAndGet();
                    System.out.println("线程 " + threadId + " 完成 (" + completed + "/" + threadCount + ")");
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(); // 等待所有线程完成
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        metrics.setEndTime(System.currentTimeMillis());
        executorService.shutdown();

        System.out.println(metrics.generateReport());
    }
    
    /**
     * 长时间稳定性测试
     */
    public void runStabilityTest(int threadCount, long durationMinutes) {
        System.out.println("\n开始长时间稳定性测试...");
        System.out.println("线程数: " + threadCount + ", 测试时长: " + durationMinutes + " 分钟");

        executorService = Executors.newFixedThreadPool(threadCount);
        PerformanceMetrics metrics = new PerformanceMetrics();
        AtomicInteger requestCounter = new AtomicInteger(0);

        long endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        metrics.setStartTime(System.currentTimeMillis());

        // 启动统计线程，定期输出当前状态
        ScheduledExecutorService statsExecutor = Executors.newSingleThreadScheduledExecutor();
        statsExecutor.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime < endTime) {
                long elapsed = (currentTime - metrics.getStartTime()) / 1000;
                System.out.printf("运行时间: %d秒, 已处理请求: %d, 当前QPS: %.2f\n",
                    elapsed, metrics.getTotalRequests(),
                    elapsed > 0 ? (double) metrics.getSuccessfulRequests() / elapsed : 0);
            }
        }, 10, 10, TimeUnit.SECONDS);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executorService.submit(() -> {
                while (System.currentTimeMillis() < endTime) {
                    int requestId = requestCounter.incrementAndGet();
                    long startTime = System.currentTimeMillis();
                    boolean success = false;

                    try {
                        String result = helloService.sayHello("StabilityTest-Thread-" + threadId + "-Request-" + requestId);
                        success = result != null && !result.isEmpty();
                    } catch (Exception e) {
                        System.err.println("Thread-" + threadId + " 请求失败: " + e.getMessage());
                    }

                    long responseTime = System.currentTimeMillis() - startTime;
                    metrics.recordRequest(responseTime, success);

                    // 小延迟避免过度压力
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }

        // 等待测试完成
        try {
            Thread.sleep(durationMinutes * 60 * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        metrics.setEndTime(System.currentTimeMillis());
        executorService.shutdown();
        statsExecutor.shutdown();

        System.out.println(metrics.generateReport());
    }
    
    /**
     * 渐进式压力测试 - 逐步增加并发数
     */
    public void runGradualStressTest() {
        System.out.println("\n开始渐进式压力测试...");

        int[] threadCounts = {1, 5, 10, 20, 50, 100};
        int requestsPerThread = 100;

        for (int threadCount : threadCounts) {
            System.out.println("\n=== 并发数: " + threadCount + " ===");
            runConcurrentTest(threadCount, requestsPerThread);

            // 测试间隔休息
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }
}