package io.ryan;

import io.ryan.loadbalance.impl.RoundLoadBalance;
import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.impl.zooKeeperImpl.ZKCenter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        HelloService helloService = ProxyFactory.getProxy(
                HelloService.class,
                new ZKCenter("localhost", 2181, new RoundLoadBalance<>())
//                new LocalServiceCenter("localhost", 8080, RpcProtocol.TCP)
        );
        String result;
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
//        System.out.println("开始休眠10秒");
//        Thread.sleep(10000);
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);


    }
}