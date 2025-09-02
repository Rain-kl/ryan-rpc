package io.ryan.protocol.server.NettServerImpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.provider.ServiceProvider;
import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.RateLimitRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NettyHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse result = getResponse(rpcRequest);
        // 发送响应并在发送完成后关闭连接
        channelHandlerContext.writeAndFlush(result).addListener(future -> channelHandlerContext.close());
    }

    private static RpcResponse getResponse(RpcRequest rpcRequest)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String interfaceName = rpcRequest.getInterfaceName();
        if (!rateLimitHandler(interfaceName)) {
            return RpcResponse.fail(429, "请求过于频繁，请稍后再试");
        }
        Class<?> classImpl = ServiceProvider.getService(interfaceName);
        Method method = classImpl.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        Object result = method.invoke(classImpl.getDeclaredConstructor().newInstance(), rpcRequest.getParameters());
        return RpcResponse.success(result);
    }

    private static boolean rateLimitHandler(String interfaceName) {
        RateLimit rateLimiter = RateLimitRegistry.INSTANCE.get(interfaceName);
        return rateLimiter.getToken(interfaceName);
    }

}
