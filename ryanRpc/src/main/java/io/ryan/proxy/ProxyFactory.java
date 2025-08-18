package io.ryan.proxy;

import io.ryan.common.Invocation;
import io.ryan.protocol.HttpClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {

    public static <T> T getProxy(Class<T> interfaceClass) {
        Object proxyInstance = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation invocation = new Invocation(interfaceClass.getName(),method.getName(), method.getParameterTypes(), args);
                HttpClient httpClient = new HttpClient();
                Object result = httpClient.send("localhost", 8080, invocation);
                return  result;
            }
        });

        return  (T) proxyInstance;
    }
}
