package io.ryan.Serializer;

import com.alibaba.fastjson.JSONObject;
import io.ryan.common.constant.SerializerType;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return JSONObject.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> messageType) {
        return JSONObject.parseObject(bytes, messageType);
    }


    @Override
    public Integer getType() {
        return SerializerType.JSON;
    }
}
