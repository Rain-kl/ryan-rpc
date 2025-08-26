package io.ryan.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.ryan.common.Message.MessageType;
import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.common.utils.Serializer.Serializer;

import java.util.List;

public class SimpleDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        //1.读取消息类型
        short messageType = in.readShort();
        //2.读取序列化的方式&类型
        short serializerType = in.readShort();
        Serializer serializer = Serializer.getSerializerByCode(serializerType);
        //3.读取序列化数组长度
        int length = in.readInt();
        //4.读取序列化数组
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object deserialize;

        if (messageType == MessageType.REQUEST.getCode()) {
            deserialize = serializer.deserialize(bytes, RpcRequest.class);
        } else if (messageType == MessageType.RESPONSE.getCode()) {
            deserialize = serializer.deserialize(bytes, RpcResponse.class);
        } else {
            throw new RuntimeException("不支持的消息类型");
        }
        out.add(deserialize);
    }
}
