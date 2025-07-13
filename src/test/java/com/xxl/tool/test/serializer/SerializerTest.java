package com.xxl.tool.test.serializer;

import com.xxl.tool.serializer.Serializer;
import com.xxl.tool.serializer.SerializerEnum;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class SerializerTest {
    private static final Logger logger = LoggerFactory.getLogger(SerializerTest.class);

    @Test
    public void test() {
        Serializer serializer = SerializerEnum.JAVA.getSerializer();

        DemoUser demoUser = new DemoUser("jack", 18);
        byte[] bytes = serializer.serialize(demoUser);
        logger.info("demoUser: {}", demoUser);

        DemoUser demoUser2 = serializer.deserialize(bytes);
        logger.info("demoUser2: {}", demoUser2);
    }

    public static class DemoUser implements Serializable {
        private static final long serialVersionUID = 42L;

        private String name;
        private Integer age;

        public DemoUser(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "DemoUser{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }
}
