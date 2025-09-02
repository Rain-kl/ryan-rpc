package io.ryan.protocol.server.HttpServerImpl;

import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.provider.ServiceProvider;
import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.RateLimitRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class HttpHandler {

    private static boolean rateLimitHandler(String interfaceName) {
        RateLimit rateLimiter = RateLimitRegistry.INSTANCE.get(interfaceName);
        return rateLimiter.getToken(interfaceName);
    }

    public void handler(HttpServletRequest req, HttpServletResponse resp) {

        try {
            RpcRequest rpcRequest = (RpcRequest) new ObjectInputStream(req.getInputStream()).readObject();

            RpcResponse rpcResponse = handler(rpcRequest);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
            objectOutputStream.writeObject(rpcResponse);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public RpcResponse handler(RpcRequest rpcRequest) {
        try {
            // 反序列化请求体中的 Invocation 对象, 此处使用的是 Java 内置的序列化机制
            String interfaceName = rpcRequest.getInterfaceName();

            if (!rateLimitHandler(interfaceName)) {
                return RpcResponse.fail(429, "请求过于频繁，请稍后再试");
            }

            Class<?> classImpl = ServiceProvider.getService(interfaceName);
            Method method = classImpl.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(classImpl.getDeclaredConstructor().newInstance(), rpcRequest.getParameters());
            return RpcResponse.success(result);


        } catch (InvocationTargetException | InstantiationException | NoSuchMethodException |
                 IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
