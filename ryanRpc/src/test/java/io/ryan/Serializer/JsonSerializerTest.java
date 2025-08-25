package io.ryan.Serializer;

import io.ryan.common.Message.RpcRequest;
import org.junit.Test;

import java.util.Arrays;

public class JsonSerializerTest {

    JsonSerializer jsonSerializer = new JsonSerializer();
    byte[] serializeBytes;

    @Test
    public void serialize() {
        RpcRequest rpcRequest = new RpcRequest(
                "io.ryan.HelloService",
                "sayHello",
                new Class<?>[]{String.class},
                new Object[]{"Hello!"}
        );
        serializeBytes = jsonSerializer.serialize(rpcRequest);
        System.out.println(Arrays.toString(serializeBytes));
    }

    @Test
    public void deserialize() {
        serialize();
        RpcRequest deserialize = jsonSerializer.deserialize(serializeBytes, RpcRequest.class);
        System.out.println(deserialize);


    }

}