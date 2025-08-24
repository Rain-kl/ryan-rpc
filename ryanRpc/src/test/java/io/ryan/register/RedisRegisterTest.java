package io.ryan.register;

import io.ryan.common.dto.URI;
import io.ryan.common.constant.RpcProtocol;
import org.junit.Test;

import java.util.List;

public class RedisRegisterTest {

    @Test
    public void register() {

        RedisRegister.register("io.ryan.service.HelloService", new URI("localhost", 8080, RpcProtocol.HTTP));
        List<URI> URIS = RedisRegister.get("io.ryan.service.HelloService");
        System.out.println("Registered URLs: " + URIS);

    }

    @Test
    public void get() {
    }

    @Test
    public void close() {
    }
}