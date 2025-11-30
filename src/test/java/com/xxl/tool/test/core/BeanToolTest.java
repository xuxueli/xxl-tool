package com.xxl.tool.test.core;

import com.xxl.tool.core.BeanTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BeanToolTest {
    private static final Logger logger = LoggerFactory.getLogger(BeanToolTest.class);

    @Test
    public void testBeanMapConvert() {
        UserDTO userDTO = new UserDTO("jack", 18);
        Object object = BeanTool.convertBeanFieldToMap(userDTO);
        logger.info("objectToPrimitive: {}", object);

        UserDTO userDTO1 = (UserDTO) BeanTool.convertMapFieldToBean(object, UserDTO.class);
        logger.info("primitiveToObject: {}", userDTO1);

    }

    @Test
    public void beanToMap() {

        UserDTO userDTO = new UserDTO("jack", 18);
        Map<String, Object> map = BeanTool.beanToMap(userDTO);
        logger.info("beanToMap: {}", map);

        UserDTO userDTO1 = BeanTool.mapToBean(map, UserDTO.class);
        logger.info("mapToBean: {}", userDTO1);
    }


    public static class UserDTO {
        private String name;
        private int age;

        public UserDTO() {
        }

        public UserDTO(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "UserDTO{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

}
