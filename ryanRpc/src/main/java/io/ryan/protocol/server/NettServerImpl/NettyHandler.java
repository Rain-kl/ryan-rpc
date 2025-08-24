package io.ryan.protocol.server.NettServerImpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.provider.ServiceProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NettyHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        RpcResponse result = getResponse(rpcRequest);
        // 发送响应并在发送完成后关闭连接
        channelHandlerContext.writeAndFlush(result).addListener(future -> {
            channelHandlerContext.close();
        });
    }

    private static RpcResponse getResponse(RpcRequest rpcRequest)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        String interfaceName = rpcRequest.getInterfaceName();
        Class<?> classImpl = ServiceProvider.getService(interfaceName);
        Method method = classImpl.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
        Object result = method.invoke(classImpl.getDeclaredConstructor().newInstance(), rpcRequest.getParameters());
        return RpcResponse.success(result);
    }

}
