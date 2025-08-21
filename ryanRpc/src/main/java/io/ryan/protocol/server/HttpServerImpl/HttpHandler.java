package io.ryan.protocol.server.HttpServerImpl;

import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.provider.ServiceProvider;
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

    public void handler(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // 反序列化请求体中的 Invocation 对象, 此处使用的是 Java 内置的序列化机制
            RpcRequest rpcRequest = (RpcRequest) new ObjectInputStream(req.getInputStream()).readObject();
            String interfaceName = rpcRequest.getInterfaceName();
            Class<?> classImpl = ServiceProvider.getService(interfaceName);
            Method method = classImpl.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(classImpl.getDeclaredConstructor().newInstance(), rpcRequest.getParameters());

            // 创建 RpcResponse 对象并序列化写入响应
            RpcResponse rpcResponse = RpcResponse.success(result);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
            objectOutputStream.writeObject(rpcResponse);
            objectOutputStream.flush();
            objectOutputStream.close();

        } catch (IOException e) {
            log.error("IO Exception occurred while handling request: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            log.error("Class not found during request handling: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            log.error("Invocation target exception occurred: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            log.error("No such method found: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            log.error("Illegal access exception: {}", e.getMessage());
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            log.error("Instantiation exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
