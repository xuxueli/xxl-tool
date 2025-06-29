package com.xxl.tool.test.id;

import com.xxl.tool.id.RandomIdTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomIdToolTest {
    private static Logger logger = LoggerFactory.getLogger(RandomIdToolTest.class);;

    @Test
    public void test() {
        logger.info("getDigitId = " + RandomIdTool.getDigitId());
        logger.info("getDigitId = " + RandomIdTool.getDigitId(10));

        logger.info("getLowercaseId = " + RandomIdTool.getLowercaseId());
        logger.info("getLowercaseId = " + RandomIdTool.getLowercaseId(10));

        logger.info("getUppercaseId = " + RandomIdTool.getUppercaseId());
        logger.info("getUppercaseId = " + RandomIdTool.getUppercaseId(10));

        logger.info("getAlphaNumeric = " + RandomIdTool.getAlphaNumeric());
        logger.info("getAlphaNumeric = " + RandomIdTool.getAlphaNumeric(10));

        logger.info("getAlphaNumericWithSpecial = " + RandomIdTool.getAlphaNumericWithSpecial());
        logger.info("getAlphaNumericWithSpecial = " + RandomIdTool.getAlphaNumericWithSpecial(10));

    }

}
