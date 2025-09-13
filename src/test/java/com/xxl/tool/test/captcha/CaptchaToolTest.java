package com.xxl.tool.test.captcha;

import com.xxl.tool.captcha.CaptchaTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class CaptchaToolTest {
    private static Logger logger = LoggerFactory.getLogger(CaptchaToolTest.class);

    /**
     * 默认验证码
     */
    @Test
    public void test1() throws IOException {

        // build tool
        CaptchaTool captchaTool = CaptchaTool.build();

        // create text
        CaptchaTool.TextResult textResult = captchaTool.createText();
        logger.info("text: {}", textResult.getText());

        // create image
        BufferedImage image = captchaTool.createImage(textResult);
        ImageIO.write(image, "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-1.png"));
    }

    /**
     * 自定义验证码属性
     */
    @Test
    public void test99() throws IOException {

        // build tool
        CaptchaTool captchaTool = CaptchaTool.build()
                .setTextCreator(new CaptchaTool.DefaultTextCreator(6))      // ArithmeticTextCreator、DefaultTextCreator
                .setWidth(180)
                .setHeight(60)
                .setColors(Arrays.asList(
                        new Color(0xb83b5e),
                        new Color(0xf08a5d),
                        new Color(0xff9a00),
                        new Color(0x00b8a9),
                        new Color(0x004a7c),
                        new Color(0x3d84a8),
                        new Color(0x521262)
                ))
                .setFontSize(40)
                .setFonts(Arrays.asList(
                        new Font("Arial", Font.BOLD, 40),
                        new Font("Courier", Font.BOLD, 40)
                ))
                .setCharSpace(8)
                .setBackgroundColorFrom(Color.LIGHT_GRAY)
                .setBackgroundColorTo(Color.WHITE)
                .setIsBorderDrawn(true)
                .setBorderColor(Color.WHITE)
                .setBorderThickness(1)
                .setNoiseColor(Color.WHITE)
                .setDistortedEngines(Arrays.asList(
                        new CaptchaTool.NoneDistorted(),
                        new CaptchaTool.ShadowDistorted(),
                        new CaptchaTool.WaterRippleDistorted(),
                        new CaptchaTool.FishEyeDistorted(),
                        new CaptchaTool.RippleDistorted()
                ));

        // create text
        CaptchaTool.TextResult textResult = captchaTool.createText();
        logger.info("text: {}", textResult.getText());
        logger.info("result: {}", textResult.getResult());

        // create image
        BufferedImage image = captchaTool.createImage(textResult);
        ImageIO.write(image, "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-2.png"));
    }

    /**
     * 算数验证码
     */
    @Test
    public void test3() throws IOException {

        // build tool
        CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.ArithmeticTextCreator());

        // create text
        CaptchaTool.TextResult textResult = captchaTool.createText();
        logger.info("text: {}", textResult.getText());
        logger.info("result: {}", textResult.getResult());

        // create image
        BufferedImage image = captchaTool.createImage(textResult);
        ImageIO.write(image, "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-3.png"));
    }

    /**
     * 批量随机
     *
     * @throws IOException
     */
    @Test
    public void test4() throws IOException {
        for (int i = 0; i < 10; i++) {
            // build tool
            CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.ArithmeticTextCreator());

            // create text
            CaptchaTool.TextResult textResult = captchaTool.createText();

            // create image
            ImageIO.write(captchaTool.createImage(textResult), "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-4-"+ i +".png"));
        }

    }

    /**
     * 自定义长度
     */
    @Test
    public void test5() throws IOException {

        // build tool
        CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.DefaultTextCreator(6));

        // create text
        CaptchaTool.TextResult textResult = captchaTool.createText();
        logger.info("text: {}", textResult.getText());
        logger.info("result: {}", textResult.getResult());

        // create image
        BufferedImage image = captchaTool.createImage(textResult);
        ImageIO.write(image, "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-5.png"));
    }

    @Test
    public void test6() throws IOException {

        // build tool
        CaptchaTool captchaTool = CaptchaTool.build().setTextCreator(new CaptchaTool.DefaultTextCreator("物华天宝人杰地灵山清水秀景色宜人"));

        // create text
        CaptchaTool.TextResult textResult = captchaTool.createText();
        logger.info("text: {}", textResult.getText());
        logger.info("result: {}", textResult.getResult());

        // create image
        BufferedImage image = captchaTool.createImage(textResult);
        ImageIO.write(image, "png", new FileOutputStream("/Users/admin/Downloads/captcha/captcha-6.png"));
    }

}
