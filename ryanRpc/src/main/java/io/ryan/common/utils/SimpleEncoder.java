package io.ryan.common.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.ryan.Serializer.Serializer;
import io.ryan.common.Message.MessageType;
import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SimpleEncoder extends MessageToByteEncoder<Object> {

    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {

        if (msg instanceof RpcRequest)
            out.writeShort(MessageType.REQUEST.getCode());
        else if (msg instanceof RpcResponse)
            out.writeShort(MessageType.RESPONSE.getCode());
        else throw new RuntimeException("不支持的消息类型");

        //2.写入序列化方式
        out.writeShort(serializer.getType());
        //得到序列化数组
        byte[] serializeBytes = serializer.serialize(msg);
        //3.写入长度
        out.writeInt(serializeBytes.length);
        //4.写入序列化数组
        out.writeBytes(serializeBytes);

    }
}
