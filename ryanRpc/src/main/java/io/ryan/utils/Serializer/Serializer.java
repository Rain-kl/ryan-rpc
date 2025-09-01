package io.ryan.utils.Serializer;

import io.ryan.common.constant.SerializerType;

public interface Serializer {

    static Serializer getSerializerByCode(int type) {
        if (type == SerializerType.Obj)
            return new ObjectSerializer();
        else if (type == SerializerType.JSON)
            return new JsonSerializer();
        else if (type == SerializerType.HESSIAN)
            return new HessianSerializer();
        throw new RuntimeException("不支持的序列化类型");
    }

    byte[] serialize(Object obj);

    <T> T deserialize(byte[] bytes, Class<T> messageType);

    Integer getType();
}
