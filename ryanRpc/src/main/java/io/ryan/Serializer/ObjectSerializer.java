package io.ryan.Serializer;

import io.ryan.common.constant.SerializerType;

import java.io.*;

public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(outputStream);
            oos.writeObject(obj);
            oos.flush();

            bytes = outputStream.toByteArray();
            oos.close();
            oos.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> messageType) {
        Object obj = null;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            obj = objectInputStream.readObject();
            objectInputStream.close();
            inputStream.close();

            // 检查类型兼容性
            if (messageType.isInstance(obj)) {
                return messageType.cast(obj);
            } else {
                throw new RuntimeException("反序列化的对象类型与期望类型不匹配");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ClassNotFoundException",e);
        }
    }

    @Override
    public Integer getType() {
        return SerializerType.Obj;
    }
}
