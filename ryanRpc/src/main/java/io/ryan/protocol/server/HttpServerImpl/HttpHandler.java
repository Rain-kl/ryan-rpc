package io.ryan.protocol.server.HttpServerImpl;

import io.ryan.common.Invocation;
import io.ryan.register.LocalRegister;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class HttpHandler {

    public void handler(HttpServletRequest req, HttpServletResponse resp){
        try {
            // 反序列化请求体中的 Invocation 对象, 此处使用的是 Java 内置的序列化机制
            Invocation invocation = (Invocation) new ObjectInputStream(req.getInputStream()).readObject();
            String interfaceName = invocation.getInterfaceName();
            Class<?> classImpl = LocalRegister.get(interfaceName);
            Method method = classImpl.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
            Object result = method.invoke(classImpl.getDeclaredConstructor().newInstance(), invocation.getParameters());
            // 将结果写入响应
            IOUtils.write(result.toString(), resp.getOutputStream(), "UTF-8");

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
