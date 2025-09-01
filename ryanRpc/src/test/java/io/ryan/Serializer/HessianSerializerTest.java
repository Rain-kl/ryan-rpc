package io.ryan.Serializer;

import io.ryan.common.utils.Serializer.HessianSerializer;
import io.ryan.common.utils.Serializer.Serializer;
import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.*;

public class HessianSerializerTest {

    @Test
    public void testHessianSerialization() {
        Serializer serializer = new HessianSerializer();

        // 创建测试对象
        TestObject original = new TestObject("张三", 25);

        // 序列化
        byte[] serialized = serializer.serialize(original);
        assertNotNull("序列化结果不能为空", serialized);
        assertTrue("序列化结果长度应该大于0", serialized.length > 0);

        // 反序列化
        TestObject deserialized = serializer.deserialize(serialized, TestObject.class);
        assertNotNull("反序列化结果不能为空", deserialized);
        assertEquals("反序列化对象应该与原对象相等", original, deserialized);
    }

    @Test
    public void testGetType() {
        Serializer serializer = new HessianSerializer();
        assertEquals("Hessian序列化器类型应该为2", Integer.valueOf(2), serializer.getType());
    }

    // 测试用的简单类
    @Setter
    @Getter
    public static class TestObject implements Serializable {
        private String name;
        private int age;

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            TestObject that = (TestObject) obj;
            return age == that.age && name.equals(that.name);
        }
    }
}
