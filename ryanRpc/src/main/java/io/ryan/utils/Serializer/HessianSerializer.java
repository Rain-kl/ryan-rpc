package io.ryan.utils.Serializer;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.ryan.common.constant.SerializerType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {

    @Override
    public byte[] serialize(Object obj) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output hessian2Output = new Hessian2Output(outputStream);

        try {
            hessian2Output.writeObject(obj);
            hessian2Output.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Hessian序列化失败", e);
        } finally {

            try {
                hessian2Output.close();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> messageType) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Hessian2Input hessian2Input = new Hessian2Input(inputStream);

        try {
            Object obj = hessian2Input.readObject();

            // 检查类型兼容性
            if (messageType.isInstance(obj)) {
                return messageType.cast(obj);
            } else {
                throw new RuntimeException("反序列化的对象类型与期望类型不匹配");
            }
        } catch (IOException e) {
            throw new RuntimeException("Hessian反序列化失败", e);
        } finally {
            try {
                hessian2Input.close();
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("关闭流失败", e);
            }
        }
    }

    @Override
    public Integer getType() {
        return SerializerType.HESSIAN;
    }
}
