package io.ryan.register;

import io.ryan.common.URL;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class RedisRegisterTest {

    @Test
    public void register() {

        RedisRegister.register("io.ryan.service.HelloService", new URL("localhost", 8080));
        List<URL> urls = RedisRegister.get("io.ryan.service.HelloService");
        System.out.println("Registered URLs: " + urls);

    }

    @Test
    public void get() {
    }

    @Test
    public void close() {
    }
}