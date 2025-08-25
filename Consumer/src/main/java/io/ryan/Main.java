package io.ryan;

import io.ryan.proxy.ProxyFactory;
import io.ryan.service.HelloService;

public class Main {
    public static void main(String[] args) {
        HelloService helloService = ProxyFactory.getProxy(HelloService.class);
        String result;
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
        System.out.println(result);
        result = helloService.sayHello("Ryan");
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