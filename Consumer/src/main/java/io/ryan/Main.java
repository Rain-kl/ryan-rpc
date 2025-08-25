package io.ryan;

import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        String result;
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println("开始休眠10秒");
        Thread.sleep(10000);
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