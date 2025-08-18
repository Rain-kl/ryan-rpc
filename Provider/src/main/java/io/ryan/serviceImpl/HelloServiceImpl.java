package io.ryan.serviceImpl;

import io.ryan.service.HelloService;

public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello world, " + name + "! This is a response from the provider.";
    }
}
