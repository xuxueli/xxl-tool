package com.xxl.tool.test.httpjsonrpc;

import com.xxl.tool.httpjsonrpc.HttpJsonClientTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpJsonToolTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpJsonToolTest.class);

    @Test
    public void testJson() {
        DemoResponse resp = HttpJsonClientTool.postObject("http://localhost:8080/post", new DemoRequest("Jack"), DemoResponse.class);
        logger.info("result = " + resp);
    }


    public static class DemoRequest{
        private String name;

        public DemoRequest(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class DemoResponse{
        private String name;
        private Integer age;
        private String word;

        public DemoResponse(String name, Integer age, String word) {
            this.name = name;
            this.age = age;
            this.word = word;
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

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        @Override
        public String toString() {
            return "DemoResponse{" +
                    "name='" + name + '\'' +
                    ", age='" + age + '\'' +
                    ", word='" + word + '\'' +
                    '}';
        }
    }

}
