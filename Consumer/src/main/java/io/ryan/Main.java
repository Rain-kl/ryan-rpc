package io.ryan;

import io.ryan.loadbalance.impl.RoundLoadBalance;
import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.impl.nacosImpl.NacosCenter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    private final static ExecutorService executorService = new ThreadPoolExecutor(
            10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>()
    );

    public static void main(String[] args) throws InterruptedException {
        ProxyFactory.setServiceCenter(new NacosCenter("localhost", 8848, new RoundLoadBalance<>()));
        HelloService helloService = ProxyFactory.getProxy(
                HelloService.class
        );
        for (int i = 0; i < 100; i++) {
            final int index = i;
            executorService.submit(() -> {
                String result = helloService.sayHello("Ryan " + index);
                System.out.println(result);
            });
        }


    }
}