package com.xxl.tool.test.core;

import com.xxl.tool.core.EnumTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumToolTest {
    private static final Logger logger = LoggerFactory.getLogger(EnumToolTest.class);

    @Test
    public void copyProperties() {
        EnumTool.getEnumItemList(EnumToolTest.TestEnum.class.getName())
                .forEach(enumItemVO -> {
                    logger.info("enumItemVO: code={}, title={}", enumItemVO.getCode(), enumItemVO.getTitle());
                });
    }


    public enum TestEnum implements EnumTool.IEnum {
        ITEM1(1, "Item 1"),
        ITEM2(2, "Item 2");

        private final int code;
        private final String title;

        TestEnum(int code, String title) {
            this.code = code;
            this.title = title;
        }

        @Override
        public int getCode() {
            return code;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

}
