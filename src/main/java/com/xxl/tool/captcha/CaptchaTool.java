package com.xxl.tool.captcha;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Kernel;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 图形验证码工具，支持链式配置画布尺寸、字体颜色、干扰线与扭曲效果
 *
 * <p>默认参数：图片 {@code 180 x 60}、字号 {@code 40}、字符间距 {@code 8}；
 * 默认文本生成器为 {@link DefaultTextCreator}，生成 4 位「数字 + 小写字母」验证码。
 * 文本生成后可先调用 {@link #createText()} 取得 {@link TextResult}（含展示文本与校验答案），
 * 再调用 {@link #createImage(TextResult)} 生成图片。</p>
 *
 * <p><b>过期与校验：</b>本工具只负责生成文本与图片，<b>不保存也不校验答案、不提供过期时间</b>。
 * 调用方需自行按业务 key 缓存 {@link TextResult#getResult()} 并设置 TTL，校验时按业务约定
 * （默认区分大小写，可先 {@code trim}）比对 {@link TextResult#getResult()}。</p>
 *
 * <p><b>线程安全：</b>本类为可变配置对象（setter 返回 {@code this}），<b>非线程安全</b>；
 * 请在单线程内完成配置并生成验证码，或为每次请求创建独立实例。生成过程中部分扭曲实现会就地读写
 * 内部文字图层像素，但 {@link #createImage(TextResult)} 不会修改入参 {@link TextResult}。</p>
 *
 * @author xuxueli 2025-09-13
 */
public class CaptchaTool {

    // ---------------------- param ----------------------

    // for text
    private TextCreator textCreator = new DefaultTextCreator();            // text creator

    // for captcha image
    private Integer width = 180;                                            // image width
    private Integer height = 60;                                            // image height
    private List<Color> colors = Arrays.asList(
            new Color(0xb83b5e),
            new Color(0xf08a5d),
            new Color(0xff9a00),
            new Color(0x00b8a9),
            new Color(0x004a7c),
            new Color(0x3d84a8),
            new Color(0x521262)
    );                                                                      // captcha text color
    private Integer fontSize = 40;                                          // captcha text font size
    private List<Font> fonts = Arrays.asList(
            new Font("Arial", Font.BOLD, 40),
            new Font("Courier", Font.BOLD, 40)
    );                                                                      // captcha text font
    private Integer charSpace = 8;                                          // captcha text char space
    private Color backgroundColorFrom = Color.LIGHT_GRAY;                   // background color from
    private Color backgroundColorTo = Color.WHITE;                          // background color to
    private Boolean isBorderDrawn = false;                                  // if draw border, true means draw border
    private Color borderColor = Color.WHITE;                                // border color
    private Integer borderThickness = 1;                                    // border thickness
    private Color noiseColor = Color.WHITE;                                 // noise color

    // for distorted engine
    private List<DistortedEngine> distortedEngines = Arrays.asList(
            new NoneDistorted(),
            new ShadowDistorted(),
            new WaterRippleDistorted(),
            new FishEyeDistorted(),
            new RippleDistorted()
    );                                                                      // distorted engine

    // ---------------------- getter and setter (support build) ----------------------

    /**
     * 获取文本生成器
     *
     * @return 当前文本生成器，默认 {@link DefaultTextCreator}
     */
    public TextCreator getTextCreator() {
        return textCreator;
    }

    /**
     * 设置文本生成器
     *
     * @param textCreator 文本生成器，不可为 {@code null}，否则 {@link #createText()} 抛异常
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setTextCreator(TextCreator textCreator) {
        this.textCreator = textCreator;
        return this;
    }

    /**
     * 获取图片宽度
     *
     * @return 图片宽度（像素），默认 180
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * 设置图片宽度
     *
     * @param width 图片宽度（像素），需大于 0
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setWidth(Integer width) {
        this.width = width;
        return this;
    }

    /**
     * 获取图片高度
     *
     * @return 图片高度（像素），默认 60
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * 设置图片高度
     *
     * @param height 图片高度（像素），需大于 0
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setHeight(Integer height) {
        this.height = height;
        return this;
    }

    /**
     * 获取候选文字颜色列表
     *
     * @return 文字颜色列表，生成图片时随机取色
     */
    public List<Color> getColors() {
        return colors;
    }

    /**
     * 设置候选文字颜色列表
     *
     * @param colors 文字颜色列表，不可为空集合；单元素时固定使用该颜色
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setColors(List<Color> colors) {
        this.colors = colors;
        return this;
    }

    /**
     * 获取字号
     *
     * @return 字号（像素），默认 40
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * 设置字号
     *
     * @param fontSize 字号（像素），需大于 0
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * 获取候选字体列表
     *
     * @return 字体列表，生成图片时随机取字体
     */
    public List<Font> getFonts() {
        return fonts;
    }

    /**
     * 设置候选字体列表
     *
     * @param fonts 字体列表，不可为空集合；单元素时固定使用该字体
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setFonts(List<Font> fonts) {
        this.fonts = fonts;
        return this;
    }

    /**
     * 获取字符间距
     *
     * @return 字符间距（像素），默认 8
     */
    public Integer getCharSpace() {
        return charSpace;
    }

    /**
     * 设置字符间距
     *
     * @param charSpace 字符间距（像素），可为 0 或负值（负值会使字符重叠）
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setCharSpace(Integer charSpace) {
        this.charSpace = charSpace;
        return this;
    }

    /**
     * 获取背景渐变起始颜色
     *
     * @return 背景渐变起始颜色，默认 {@link Color#LIGHT_GRAY}
     */
    public Color getBackgroundColorFrom() {
        return backgroundColorFrom;
    }

    /**
     * 设置背景渐变起始颜色
     *
     * @param backgroundColorFrom 背景渐变起始颜色
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setBackgroundColorFrom(Color backgroundColorFrom) {
        this.backgroundColorFrom = backgroundColorFrom;
        return this;
    }

    /**
     * 获取背景渐变结束颜色
     *
     * @return 背景渐变结束颜色，默认 {@link Color#WHITE}
     */
    public Color getBackgroundColorTo() {
        return backgroundColorTo;
    }

    /**
     * 设置背景渐变结束颜色
     *
     * @param backgroundColorTo 背景渐变结束颜色
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setBackgroundColorTo(Color backgroundColorTo) {
        this.backgroundColorTo = backgroundColorTo;
        return this;
    }

    /**
     * 是否绘制边框
     *
     * @return {@code true} 绘制边框，默认 {@code false}
     */
    public Boolean getIsBorderDrawn() {
        return isBorderDrawn;
    }

    /**
     * 设置是否绘制边框
     *
     * @param isBorderDrawn {@code true} 绘制边框；{@code null} 会在生成图片时抛异常
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setIsBorderDrawn(Boolean isBorderDrawn) {
        this.isBorderDrawn = isBorderDrawn;
        return this;
    }

    /**
     * 获取边框颜色
     *
     * @return 边框颜色，默认 {@link Color#WHITE}
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * 设置边框颜色
     *
     * @param borderColor 边框颜色
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    /**
     * 获取边框粗细
     *
     * @return 边框粗细（像素），默认 1
     */
    public Integer getBorderThickness() {
        return borderThickness;
    }

    /**
     * 设置边框粗细
     *
     * @param borderThickness 边框粗细（像素）
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setBorderThickness(Integer borderThickness) {
        this.borderThickness = borderThickness;
        return this;
    }

    /**
     * 获取干扰线颜色
     *
     * @return 干扰线颜色，默认 {@link Color#WHITE}
     */
    public Color getNoiseColor() {
        return noiseColor;
    }

    /**
     * 设置干扰线颜色
     *
     * @param noiseColor 干扰线颜色
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setNoiseColor(Color noiseColor) {
        this.noiseColor = noiseColor;
        return this;
    }

    /**
     * 获取扭曲效果列表
     *
     * @return 扭曲效果列表，生成图片时随机取一种
     */
    public List<DistortedEngine> getDistortedEngines() {
        return distortedEngines;
    }

    /**
     * 设置扭曲效果列表
     *
     * @param distortedEngines 扭曲效果列表，不可为空集合；单元素时固定使用该效果
     * @return 当前实例，便于链式调用
     */
    public CaptchaTool setDistortedEngines(List<DistortedEngine> distortedEngines) {
        this.distortedEngines = distortedEngines;
        return this;
    }


    // ---------------------- build api ----------------------

    /**
     * 创建验证码工具实例（默认配置）
     *
     * @return 新的 {@link CaptchaTool} 实例
     */
    public static CaptchaTool build() {
        return new CaptchaTool();
    }


    // ---------------------- text create ----------------------

    /**
     * 生成验证码文本
     *
     * <p>调用当前 {@link TextCreator} 生成展示文本与校验答案。返回的 {@link TextResult}
     * 需由调用方保存，用于后续用户输入校验。</p>
     *
     * @return 验证码文本结果，包含展示文本与校验答案
     * @throws IllegalArgumentException {@code textCreator} 为 {@code null} 时抛出
     */
    public TextResult createText() {
        // text
        assertNotNull(textCreator,"textCreator is null" );
        return textCreator.create();
    }

    /**
     * 断言对象非空
     *
     * @param object       待校验对象
     * @param errorMessage 对象为空时写入异常的信息
     * @throws IllegalArgumentException {@code object} 为 {@code null} 时抛出
     */
    public static void assertNotNull(Object object, String errorMessage) {
        if (object == null) {
            throw new  IllegalArgumentException(errorMessage);
        }
    }

    // ---------------------- image create ----------------------

    /**
     * 根据验证码文本生成图片
     *
     * <p>依次完成文字绘制、随机扭曲、干扰线、渐变背景与可选边框。生成过程中会就地修改
     * 文字图层，但不会修改入参 {@link TextResult}。</p>
     *
     * @param textResult 验证码文本结果，通常来自 {@link #createText()}
     * @return 生成的验证码图片（ARGB/RGB {@link BufferedImage}）
     * @throws IllegalArgumentException 入参为 {@code null}、宽高/颜色/字体/噪声色等必要配置为 {@code null}，
     *                                  或颜色、字体、扭曲效果集合为空导致无法随机取值时抛出
     */
    public BufferedImage createImage(TextResult textResult) {

        // valid
        assertNotNull(textResult,"textResult is null" );


        // size
        assertNotNull(width,"width is null" );
        assertNotNull(height,"height is null" );

        // background
        assertNotNull(backgroundColorFrom,"backgroundColorFrom is null" );
        assertNotNull(backgroundColorTo,"backgroundColorTo is null" );

        // text
        assertNotNull(colors,"color is null" );
        assertNotNull(fontSize,"fontSize is null" );
        assertNotNull(charSpace,"charSpace is null" );
        assertNotNull(fonts,"fonts is null" );

        // border
        assertNotNull(isBorderDrawn,"isBorderDrawn is null" );
        assertNotNull(borderColor,"borderColor is null" );
        assertNotNull(borderThickness,"borderThickness is null" );

        // distortedEngine
        assertNotNull(distortedEngines,"distortedEngine is null" );

        // noiseColor
        assertNotNull(noiseColor,"noiseColor is null" );

        // 绘制文字
        String text = textResult.getText();
        Color color = colors.size()==1?colors.get(0):colors.get(new Random().nextInt(colors.size()));
        Font font = fonts.size()==1?fonts.get(0):fonts.get(new Random().nextInt(fonts.size()));
        BufferedImage bi = renderWord(text, width, height,fontSize, font, color, charSpace);

        // 文字扭曲
        DistortedEngine distortedEngine = distortedEngines.size()==1?distortedEngines.get(0):distortedEngines.get(new Random().nextInt(distortedEngines.size()));
        bi = distortedEngine.getDistortedImage(bi);

        // 绘制干扰线 // 透明色: new Color(0, 0, 0, 0);
        makeNoise(bi, noiseColor, .1f, .1f, .25f, .25f);
        makeNoise(bi, noiseColor, .1f, .25f, .5f, .9f);

        // 绘制背景
        bi = addBackground(bi, backgroundColorFrom, backgroundColorTo);

        // 绘制边框
        if (isBorderDrawn) {
            drawBox(bi, borderColor, borderThickness, width, height);
        }
        return bi;
    }

    /**
     * 绘制文字
     *
     * @param word          word to draw
     * @param width         image width
     * @param height        image height
     * @param fontSize      font size
     * @param font          font
     * @param color         font color
     * @param charSpace     char space
     * @return BufferedImage
     */
    private BufferedImage renderWord(String word,
                                            int width,
                                            int height,
                                            int fontSize,
                                            Font font,
                                            Color color,
                                            int charSpace) {

        // 1、创建一个指定宽度和高度和像素格式的BufferedImage对象。
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // 2. 创建一个Graphics2D对象，用于在BufferedImage上绘制图形和文本。
        Graphics2D g2D = image.createGraphics();
        g2D.setColor(color);

        // 3. 设置抗锯齿和渲染质量。
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2D.setRenderingHints(hints);

        // 4. 获取字体渲染上下文，用于创建字形向量。
        FontRenderContext frc = g2D.getFontRenderContext();
        Random random = new Random();//new SecureRandom();

        // 5. 计算文字起始Y坐标位置，为
        int startPosY = (height - fontSize) / 5 + fontSize;

        // 6. 计算每个字符的字体和宽度。
        char[] wordChars = word.toCharArray();
        Font[] chosenFonts = new Font[wordChars.length];
        int [] charWidths = new int[wordChars.length];
        int widthNeeded = 0;
        for (int i = 0; i < wordChars.length; i++) {
            // 6.1 随机选择字体
            chosenFonts[i] = font;

            // 6.2 计算字符宽度
            char[] charToDraw = new char[]{wordChars[i]};
            GlyphVector gv = chosenFonts[i].createGlyphVector(frc, charToDraw);
            charWidths[i] = (int)gv.getVisualBounds().getWidth();
            if (i > 0) {
                widthNeeded = widthNeeded + charSpace;
            }
            // 6.3 计算总宽度
            widthNeeded = widthNeeded + charWidths[i];
        }

        // 7. 计算文字起始X坐标位置。
        int startPosX = (width - widthNeeded) / 2;
        for (int i = 0; i < wordChars.length; i++) {
            // 7.1 设置字体
            g2D.setFont(chosenFonts[i]);
            char[] charToDraw = new char[] {
                    wordChars[i]
            };
            // 7.2 绘制字符
            g2D.drawChars(charToDraw, 0, charToDraw.length, startPosX, startPosY);
            // 7.3 计算下一个字符的起始X坐标位置。
            startPosX = startPosX + (int) charWidths[i] + charSpace;
        }

        return image;
    }

    /**
     * 在图片上绘制一条随机三次贝塞尔噪声曲线
     *
     * @param image       目标图片，会就地绘制
     * @param noiseColor  噪声颜色
     * @param factorOne   曲线起点 X 坐标比例
     * @param factorTwo   第一个控制点 X 坐标比例
     * @param factorThree 第二个控制点 X 坐标比例
     * @param factorFour  曲线终点 X 坐标比例
     */
    private void makeNoise(BufferedImage image,
                                 Color noiseColor,
                                 float factorOne,
                                 float factorTwo,
                                 float factorThree,
                                 float factorFour) {

        // image size
        int width = image.getWidth();
        int height = image.getHeight();

        // the points where the line changes the stroke and direction
        Point2D[] pts = null;
        Random rand = new SecureRandom();

        // the curve from where the points are taken
        CubicCurve2D cc = new CubicCurve2D.Float(width * factorOne, height
                * rand.nextFloat(), width * factorTwo, height
                * rand.nextFloat(), width * factorThree, height
                * rand.nextFloat(), width * factorFour, height
                * rand.nextFloat());

        // creates an iterator to define the boundary of the flattened curve
        PathIterator pi = cc.getPathIterator(null, 2);
        Point2D tmp[] = new Point2D[200];
        int i = 0;

        // while pi is iterating the curve, adds points to tmp array
        while (!pi.isDone())
        {
            float[] coords = new float[6];
            switch (pi.currentSegment(coords))
            {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    tmp[i] = new Point2D.Float(coords[0], coords[1]);
            }
            i++;
            pi.next();
        }

        pts = new Point2D[i];
        System.arraycopy(tmp, 0, pts, 0, i);

        Graphics2D graph = (Graphics2D) image.getGraphics();
        graph.setRenderingHints(new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON));

        graph.setColor(noiseColor);

        // for the maximum 3 point change the stroke and direction
        for (i = 0; i < pts.length - 1; i++)
        {
            if (i < 3)
                graph.setStroke(new BasicStroke(0.9f * (4 - i)));
            graph.drawLine((int) pts[i].getX(), (int) pts[i].getY(),
                    (int) pts[i + 1].getX(), (int) pts[i + 1].getY());
        }

        graph.dispose();
    }

    /**
     * 绘制背景
     *
     * @param baseImage     原始图片
     * @param colorFrom     渐变开始颜色
     * @param colorTo       渐变结束颜色
     * @return  BufferedImage
     */
    private BufferedImage addBackground(BufferedImage baseImage, Color colorFrom, Color colorTo) {


        // 1、创建一个指定宽度和高度和像素格式的BufferedImage对象。
        int width = baseImage.getWidth();
        int height = baseImage.getHeight();
        BufferedImage imageWithBackground = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // 2. 创建一个Graphics2D对象，用于在BufferedImage上绘制图形和文本。
        Graphics2D graph = (Graphics2D) imageWithBackground.getGraphics();

        // 3. 设置抗锯齿
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        // 4. 设置颜色渲染质量。
        hints.add(new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        // 5. 设置透明度插值质量。
        hints.add(new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY));
        // 6. 设置渲染质量。
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        graph.setRenderingHints(hints);

        // 7. 创建一个渐变画笔对象，用于绘制渐变效果。
        GradientPaint paint = new GradientPaint(0, 0, colorFrom, width, height, colorTo);
        // 8. 设置画笔颜色。
        graph.setPaint(paint);
        // 9. 绘制一个矩形区域。
        graph.fill(new Rectangle2D.Double(0, 0, width, height));

        // 10. 绘制原始图片。
        graph.drawImage(baseImage, 0, 0, null);

        return imageWithBackground;
    }

    /**
     * 绘制矩形边框
     *
     * <p>注意：实现中左侧竖线的终点使用 {@code width} 而非 {@code height}，
     * 当宽高不相等时左侧竖线会超出画布。此处仅说明现状，未修改实现。</p>
     *
     * @param baseImage         目标图片，会就地绘制
     * @param borderColor       边框颜色
     * @param borderThickness   边框宽度（像素），为 1 时使用默认笔画
     * @param width             图片宽度
     * @param height            图片高度
     */
    private void drawBox(BufferedImage baseImage, Color borderColor, int borderThickness, int width, int height) {

        // 1、创建一个Graphics2D对象，用于在BufferedImage上绘制图形和文本。
        Graphics2D graphics = baseImage.createGraphics();

        // 2. 设置边框颜色
        graphics.setColor(borderColor);

        // 3. 设置边框宽度
        if (borderThickness != 1) {
            BasicStroke stroke = new BasicStroke((float) borderThickness);
            graphics.setStroke(stroke);
        }

        // 4. 绘制边框
        graphics.draw(new Line2D.Double(0, 0, 0, width));
        graphics.draw(new Line2D.Double(0, 0, width, 0));
        graphics.draw(new Line2D.Double(0, height - 1, width, height - 1));
        graphics.draw(new Line2D.Double(width - 1, height - 1, width - 1, 0));
    }


    // ---------------------- Text Creator ----------------------

    /**
     * 验证码文本生成器扩展接口
     *
     * <p>实现类需保证 {@link #create()} 返回非 {@code null} 的 {@link TextResult}，
     * 且 {@link TextResult#getResult()} 为可供调用方比对的校验答案。</p>
     *
     * <p>线程安全语义由具体实现决定，无状态实现可复用。</p>
     */
    public interface TextCreator {
        /**
         * 生成验证码文本
         *
         * @return 验证码文本结果，不应返回 {@code null}
         */
        TextResult create();
    }

    /**
     * 验证码文本结果
     *
     * <p>{@link #getText()} 为展示在图片上的文本（算术题形如 {@code 3+5=?}），
     * {@link #getResult()} 为校验答案（如 {@code 8}）。对象不可变、线程安全。</p>
     */
    public static class TextResult {

        private final String text;
        private final String result;

        /**
         * 构造文本结果
         *
         * @param text   展示文本
         * @param result 校验答案
         */
        public TextResult(String text, String result) {
            this.text = text;
            this.result = result;
        }

        /**
         * 获取展示文本
         *
         * @return 展示在图片上的文本
         */
        public String getText() {
            return text;
        }

        /**
         * 获取校验答案
         *
         * @return 用户输入需要匹配的答案
         */
        public String getResult() {
            return result;
        }

        /**
         * 返回文本与答案的调试字符串
         *
         * @return 包含 text 与 result 的字符串
         */
        @Override
        public String toString() {
            return "TextCreatorResult{" +
                    "text='" + text + '\'' +
                    ", result='" + result + '\'' +
                    '}';
        }
    }

    /** 数字字符集 */
    public static final String TEXT_NUMBER = "0123456789";
    /** 小写字母字符集 */
    public static final String TEXT_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    /** 数字 + 小写字母字符集 */
    public static final String TEXT_NUMBER_AND_LOWERCASE = "0123456789abcdefghijklmnopqrstuvwxyz";

    /**
     * 默认文本生成器：从指定字符集中随机取字符，展示文本与校验答案相同
     *
     * <p>默认生成长度 4、字符集 {@link #TEXT_NUMBER_AND_LOWERCASE}。使用
     * {@link Random} 生成随机数（非加密安全），不适用于需要高安全性的场景。
     * 对象不可变，可复用。</p>
     */
    public static class DefaultTextCreator implements TextCreator{

        private final int length;
        private final String str;

        /**
         * 使用默认配置：长度 4、数字 + 小写字母
         */
        public DefaultTextCreator() {
            this(4);
        }
        /**
         * 指定长度，使用默认字符集（数字 + 小写字母）
         *
         * @param length 验证码长度
         */
        public DefaultTextCreator(int length) {
            this(length, TEXT_NUMBER_AND_LOWERCASE);
        }
        /**
         * 指定字符集，使用默认长度 4
         *
         * @param str 候选字符集，不能为空
         */
        public DefaultTextCreator(String str) {
            this(4, str);
        }
        /**
         * 指定长度与字符集
         *
         * @param length 验证码长度
         * @param str    候选字符集，不能为空
         */
        public DefaultTextCreator(int length, String str) {
            this.length = length;
            this.str = str;
        }

        /**
         * 使用构造时指定的长度生成文本
         *
         * @return 验证码文本结果
         */
        @Override
        public TextResult create() {
            return create(this.length);
        }

        /**
         * 使用指定长度生成文本
         *
         * @param length 验证码长度，需大于 0
         * @return 验证码文本结果，展示文本与校验答案相同
         * @throws NullPointerException     构造时字符集为 {@code null} 时抛出
         * @throws IllegalArgumentException 字符集为空或长度为 0/负数时由 {@link Random#nextInt(int)} 抛出
         */
        public TextResult create(int length) {
            char[] chars = str.toCharArray();
            Random rand = new Random(); //SecureRandom();

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < length; i++) {
                text.append(chars[rand.nextInt(chars.length)]);
            }

            String result = text.toString();
            return new TextResult(result, result);
        }

    }

    /**
     * 算术文本生成器：随机生成 10 以内加减乘除算式，展示文本形如 {@code 3+5=?}
     *
     * <p>减法保证非负，除法保证整除；无法整除时退化为加法。校验答案为算式计算结果。
     * 无状态、线程安全。</p>
     */
    public static class ArithmeticTextCreator implements TextCreator{

        /**
         * the number of the arithmetic
         */
        private static final String[] Number = "0,1,2,3,4,5,6,7,8,9,10".split(",");

        /**
         * 生成随机算术题
         *
         * @return 验证码文本结果，展示文本为算式，校验答案为计算结果
         */
        @Override
        public TextResult create() {
            // param
            Random random = new Random();
            int x = random.nextInt(10);
            int y = random.nextInt(10);

            // result
            StringBuilder text = new StringBuilder();
            int result;

            // randomOperand [0, 3]
            int randomOperand = random.nextInt(4);
            if (randomOperand == 0) {
                text.append(Number[x]);
                text.append("+");
                text.append(Number[y]);

                result = x + y;
            } else if (randomOperand == 1) {
                if (x >= y) {
                    text.append(Number[x]);
                    text.append("-");
                    text.append(Number[y]);

                    result = x - y;
                } else {
                    text.append(Number[y]);
                    text.append("-");
                    text.append(Number[x]);

                    result = y - x;
                }
            } else if (randomOperand == 2) {
                text.append(Number[x]);
                text.append("*");
                text.append(Number[y]);

                result = x * y;
            } else {
                if (x != 0 && y % x == 0) {
                    text.append(Number[y]);
                    text.append("/");
                    text.append(Number[x]);

                    result = y / x;
                } else {
                    text.append(Number[x]);
                    text.append("+");
                    text.append(Number[y]);

                    result = x + y;
                }
            }
            text.append("=?");

            return new TextResult(text.toString(), String.valueOf(result));
        }
    }


    // ---------------------- Distorted Engine ----------------------

    /**
     * 图片扭曲效果扩展接口
     *
     * <p>实现类接收文字图层并返回扭曲后的图片。部分实现会就地修改入参图片，
     * 调用方不应在扭曲后继续依赖原图内容。</p>
     */
    public interface DistortedEngine {
        /**
         * 应用扭曲效果
         *
         * @param baseImage 原始文字图层，部分实现会就地修改
         * @return 扭曲后的图片，通常与入参尺寸一致
         */
        BufferedImage getDistortedImage(BufferedImage baseImage);
    }

    /**
     * 无扭曲效果：直接返回原图
     */
    public static class NoneDistorted implements CaptchaTool.DistortedEngine {
        /**
         * 返回原图，不做任何处理
         *
         * @param baseImage 原始图片
         * @return 入参图片本身
         */
        @Override
        public BufferedImage getDistortedImage(BufferedImage baseImage) {
            return baseImage;
        }
    }

    /**
     * 鱼眼效果：以图片中心为圆心，按距离对像素做径向放大/压缩
     *
     * <p>会就地修改入参图片的部分像素。</p>
     */
    public static class FishEyeDistorted implements CaptchaTool.DistortedEngine {

        /**
         * 应用鱼眼扭曲
         *
         * @param baseImage 原始图片，部分像素会被就地修改
         * @return 扭曲后的图片（即入参图片）
         */
        public BufferedImage getDistortedImage(BufferedImage baseImage) {

            int imageHeight = baseImage.getHeight();
            int imageWidth = baseImage.getWidth();

            /*
            // 计算横线和竖线数量
            int horizontalLines = imageHeight / 7;
            int verticalLines = imageWidth / 7;

            // 计算线之间的间距
            int horizontalGaps = imageHeight / (horizontalLines + 1);
            int verticalGaps = imageWidth / (verticalLines + 1);

            // color
            Color horizontalColor = new Color(0x00adb5);
            Color verticalColor = new Color(0xeeeeee);

            // 绘制横线
            Graphics2D graph = (Graphics2D) baseImage.getGraphics();
            for (int i = horizontalGaps; i < imageHeight; i = i + horizontalGaps) {
                graph.setColor(horizontalColor);
                graph.drawLine(0, i, imageWidth, i);
            }
            // 绘制竖线
            for (int i = verticalGaps; i < imageWidth; i = i + verticalGaps) {
                graph.setColor(verticalColor);
                graph.drawLine(i, 0, i, imageHeight);
            }*/

            // 复制像素
            int pix[] = new int[imageHeight * imageWidth];
            int j = 0;
            for (int j1 = 0; j1 < imageWidth; j1++) {
                for (int k1 = 0; k1 < imageHeight; k1++) {
                    pix[j] = baseImage.getRGB(j1, k1);
                    j++;
                }

            }

            // 计算扭曲距离
            double distance = randomInt(imageWidth / 4, imageWidth / 3);

            // 计算中间点
            int widthMiddle = baseImage.getWidth() / 2;
            int heightMiddle = baseImage.getHeight() / 2;

            // again iterate over all pixels
            for (int x = 0; x < baseImage.getWidth(); x++) {
                for (int y = 0; y < baseImage.getHeight(); y++) {

                    // 计算像素点与中间点的距离
                    int relX = x - widthMiddle;
                    int relY = y - heightMiddle;

                    // 计算扭曲距离
                    double d1 = Math.sqrt(relX * relX + relY * relY);
                    if (d1 < distance) {
                        // 计算扭曲后的像素点
                        int j2 = widthMiddle + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (double) (x - widthMiddle));
                        int k2 = heightMiddle + (int) (((fishEyeFormula(d1 / distance) * distance) / d1) * (double) (y - heightMiddle));
                        // 设置像素点
                        baseImage.setRGB(x, y, pix[j2 * imageHeight + k2]);
                    }
                }

            }

            return baseImage;
        }

        /**
         * 生成一个随机整数，该整数在 i 和 j 之间（包括 i 和 j）。
         *
         * @param i 下界（含）
         * @param j 上界（含）
         * @return [i, j] 区间内的随机整数
         */
        private int randomInt(int i, int j) {
            double d = Math.random();
            return (int) ((double) i + (double) ((j - i) + 1) * d);
        }

        /**
         * 鱼眼效果计算公式：g(s) = - (3/4)s3 + (3/2)s2 + (1/4)s，s 的范围为 0 到 1
         *
         * @param s 归一化距离
         * @return 扭曲系数；s 小于 0 返回 0，s 大于 1 时原样返回 s
         */
        private double fishEyeFormula(double s) {
            if (s < 0.0D){
                return 0.0D;
            }
            if (s > 1.0D){
                return s;
            } else {
                return -0.75D * s * s * s + 1.5D * s * s + 0.25D * s;
            }
        }

    }

    /**
     * 水波纹效果：以图片中心为圆心做正弦波纹扭曲，并在输出时使用双线性插值平滑像素
     */
    public static class WaterRippleDistorted implements CaptchaTool.DistortedEngine {

        /**
         * 应用水波纹扭曲
         *
         * @param baseImage 原始图片，不修改入参
         * @return 新生成的扭曲图片
         */
        public BufferedImage getDistortedImage(BufferedImage baseImage) {
            // 水波纹滤镜：对原始图像进行处理，创建从中心向外扩散的波纹效果
            BufferedImage effectImage = waterFilter(baseImage);

            // 图像合成
            BufferedImage distortedImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = (Graphics2D) distortedImage.getGraphics();
            graphics.drawImage(effectImage, 0, 0, null, null);
            graphics.dispose();
            return distortedImage;
        }


        /**
         * 水波纹滤镜
         *
         * @param src 原始图片，不修改入参
         * @return 应用水波纹后的新图片
         */
        public BufferedImage waterFilter( BufferedImage src) {

            // 波纹振幅，控制波纹强度(1.5f)
            float amplitude = 1.5f;
            // 相位，控制波纹的起始位置(10)
            float phase = 10;
            // 波长，控制波纹密度(2)
            float wavelength = 2;
            // 波纹中心点比例位置(0.5f，即图像中心)
            float centreX = 0.5f;
            float centreY = 0.5f;
            // 波纹半径(50)
            float radius = 50;

            // 计算图像中心点的实际坐标
            float icentreX = src.getWidth() * centreX;
            float icentreY = src.getHeight() * centreY;
            // 计算波纹半径的平方，用于后续计算
            float radius2 = radius*radius;

            // 矩形区域，表示转换后的图像大小
            int width = src.getWidth();
            int height = src.getHeight();
            Rectangle transformedSpace = new Rectangle(0, 0, width, height);

            // 获取源图像的像素数据
            ColorModel dstCM = src.getColorModel();
            // 创建转换后的图像
            BufferedImage dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height), dstCM.isAlphaPremultiplied(), null);

            // 获取源图像的像素数据
            int[] inPixels = getRGB( src, 0, 0, width, height, null );

            // 计算源图像的宽度和高度
            int srcWidth1 = width-1;
            int srcHeight1 = height-1;
            // 计算转换后的图像的宽度和高度
            int outWidth = transformedSpace.width;
            int outHeight = transformedSpace.height;
            // 创建一个整型数组，用于存储转换后的图像的像素数据
            int[] outPixels = new int[outWidth];

            // 计算转换后的图像的左上角坐标
            int outX = transformedSpace.x;
            int outY = transformedSpace.y;
            // 创建一个浮点型数组，用于存储转换后的坐标
            float[] out = new float[2];

            // 遍历转换后的图像的每个像素点
            for (int y = 0; y < outHeight; y++) {
                for (int x = 0; x < outWidth; x++) {
                    // 对每个输出点，调用transformInverse4water函数计算对应的输入图像坐标
                    transformInverse4water(outX+x, outY+y, out, icentreX, icentreY, radius2, amplitude, phase, wavelength, radius);

                    // 计算输入图像的坐标
                    int srcX = (int)Math.floor( out[0] );
                    int srcY = (int)Math.floor( out[1] );

                    // 计算输入图像坐标与输出点坐标的权重，用于插值计算
                    float xWeight = out[0]-srcX;
                    float yWeight = out[1]-srcY;

                    // 计算输入图像的四个像素点
                    int nw, ne, sw, se;

                    // 根据输入图像的坐标判断像素点是否在图像范围内
                    if ( srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
                        // 当计算出的源坐标在图像范围内时直接获取像素值
                        int i = width *srcY + srcX;
                        nw = inPixels[i];
                        ne = inPixels[i+1];
                        sw = inPixels[i+ width];
                        se = inPixels[i+ width +1];
                    } else {
                        // 当坐标超出图像范围时，通过getPixel函数处理边界情况
                        nw = getPixel( inPixels, srcX, srcY, width, height);
                        ne = getPixel( inPixels, srcX+1, srcY, width, height);
                        sw = getPixel( inPixels, srcX, srcY+1, width, height);
                        se = getPixel( inPixels, srcX+1, srcY+1, width, height);
                    }

                    // 进行双线性插值计算，将像素值赋值给输出像素数组
                    outPixels[x] = bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
                }
                // 将转换后的像素数据设置到转换后的图像中
                setRGB( dst, 0, y, transformedSpace.width, 1, outPixels );
            }
            return dst;
        }

        /**
         * 计算一个点 (x,y) 在水波纹扭曲后的新坐标
         *
         * @param x         原始 X 坐标
         * @param y         原始 Y 坐标
         * @param out       输出数组，长度至少为 2，写入新坐标 [x, y]
         * @param icentreX  波纹中心 X 坐标
         * @param icentreY  波纹中心 Y 坐标
         * @param radius2   波纹半径的平方
         * @param amplitude 波纹振幅
         * @param phase     相位
         * @param wavelength 波长
         * @param radius    波纹半径
         */
        protected void transformInverse4water(int x, int y,
                                              float[] out, float icentreX,
                                              float icentreY, float radius2,
                                              float amplitude, float phase,
                                              float wavelength, float radius) {

            // 计算点到波纹中心的距离
            float dx = x-icentreX;
            float dy = y-icentreY;
            float distance2 = dx*dx + dy*dy;

            if (distance2 > radius2) {
                // 如果超出波纹半径则保持原坐标不变
                out[0] = x;
                out[1] = y;
            } else {
                // 在波纹范围内，根据距离计算扭曲量，使用正弦函数模拟波纹效果，并根据距离中心远近调整扭曲强度
                // 计算距离
                float distance = (float)Math.sqrt(distance2);
                // 计算扭曲量
                float amount = amplitude * (float)Math.sin(distance / wavelength * TWO_PI - phase);
                amount *= (radius-distance)/radius;
                // 根据距离调整扭曲强度
                if ( distance != 0 ) {
                    amount *= wavelength/distance;
                }

                // 计算新坐标
                out[0] = x + dx*amount;
                out[1] = y + dy*amount;
            }
        }

        // ---------------------- BufferedImage tool ----------------------

        /**
         * 获取 image 的 RGB，从 x,y 开始，w,h 大小；返回结果 RGB 数组；
         *
         * @param image  源图片
         * @param x      起始 X 坐标
         * @param y      起始 Y 坐标
         * @param width  区域宽度
         * @param height 区域高度
         * @param pixels 复用的像素数组，可为 {@code null}
         * @return 像素数组（int 形式 ARGB）
         */
        public static int[] getRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
            int type = image.getType();
            if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
                return (int [])image.getRaster().getDataElements( x, y, width, height, pixels );
            return image.getRGB( x, y, width, height, pixels, 0, width );
        }

        /**
         * 设置 image 的 RGB，从 x,y 开始，w,h 大小；设置 RGB 数组；
         *
         * @param image  目标图片，会就地修改
         * @param x      起始 X 坐标
         * @param y      起始 Y 坐标
         * @param width  区域宽度
         * @param height 区域高度
         * @param pixels 待写入的像素数组（int 形式 ARGB）
         */
        public static void setRGB( BufferedImage image, int x, int y, int width, int height, int[] pixels ) {
            int type = image.getType();
            if ( type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB )
                image.getRaster().setDataElements( x, y, width, height, pixels );
            else
                image.setRGB( x, y, width, height, pixels, 0, width );
        }

        /**
         * 获取像素点，越界坐标会被钳制到图像边缘
         *
         * @param pixels 像素数组
         * @param x      X 坐标，可越界
         * @param y      Y 坐标，可越界
         * @param width  图片宽度
         * @param height 图片高度
         * @return 对应像素的 ARGB 值；越界时返回最近边缘像素
         */
        public static int getPixel( int[] pixels, int x, int y, int width, int height) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return pixels[(clamp(y, 0, height - 1) * width) + clamp(x, 0, width - 1)];
            }
            return pixels[ y*width+x ];
        }

        /**
         * 创建与源图片颜色模型、透明模式兼容的空图片
         *
         * @param src 源图片
         * @return 与源图片尺寸一致的新图片
         */
        public static BufferedImage createCompatibleDestImage(BufferedImage src) {
            ColorModel dstCM = src.getColorModel();
            return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
        }

        // ---------------------- ImageMath ----------------------

        /**
         * 2π，圆周率乘以2的值。
         */
        public final static float TWO_PI = (float)Math.PI*2.0f;

        /**
         * 将值 x 限制在区间 [a, b] 范围内。
         *
         * @param x 待限制的值
         * @param a 下界
         * @param b 上界
         * @return 限制后的值
         */
        public static int clamp(int x, int a, int b) {
            return (x < a) ? a : Math.min(x, b);
        }

        /**
         * 将值 x 限制在区间 [0, 255] 范围内。
         *
         * @param c 待限制的颜色通道值
         * @return 限制后的值，用于拼接 ARGB 通道
         */
        public static int clamp(int c) {
            if (c < 0){
                return 0;
            }
            return Math.min(c, 255);
        }

        /**
         * 双线性插值（bilinear interpolation）算法，用于计算图像处理中的像素值。
         * 1、从四个角点提取ARGB通道值（alpha透明度、红色、绿色、蓝色）
         * 2、计算插值权重：cx = 1-x和cy = 1-y
         * 3、对每个通道分别进行双线性插值：先在x方向上进行两次线性插值，再在y方向上对上一步结果进行线性插值
         * 4、最后将四个通道重新组合成一个32位整数像素值返回
         *
         * 常用于图像变形、纹理映射等需要平滑过渡的场景，如水波纹失真效果。
         *
         * @param x  水平方向插值权重，范围 [0, 1]
         * @param y  垂直方向插值权重，范围 [0, 1]
         * @param nw 左上像素 ARGB
         * @param ne 右上像素 ARGB
         * @param sw 左下像素 ARGB
         * @param se 右下像素 ARGB
         * @return 插值后的 ARGB 像素值
         */
        public static int bilinearInterpolate(float x, float y, int nw, int ne, int sw, int se) {
            float m0, m1;
            int a0 = (nw >> 24) & 0xff;
            int r0 = (nw >> 16) & 0xff;
            int g0 = (nw >> 8) & 0xff;
            int b0 = nw & 0xff;
            int a1 = (ne >> 24) & 0xff;
            int r1 = (ne >> 16) & 0xff;
            int g1 = (ne >> 8) & 0xff;
            int b1 = ne & 0xff;
            int a2 = (sw >> 24) & 0xff;
            int r2 = (sw >> 16) & 0xff;
            int g2 = (sw >> 8) & 0xff;
            int b2 = sw & 0xff;
            int a3 = (se >> 24) & 0xff;
            int r3 = (se >> 16) & 0xff;
            int g3 = (se >> 8) & 0xff;
            int b3 = se & 0xff;

            float cx = 1.0f-x;
            float cy = 1.0f-y;

            m0 = cx * a0 + x * a1;
            m1 = cx * a2 + x * a3;
            int a = (int)(cy * m0 + y * m1);

            m0 = cx * r0 + x * r1;
            m1 = cx * r2 + x * r3;
            int r = (int)(cy * m0 + y * m1);

            m0 = cx * g0 + x * g1;
            m1 = cx * g2 + x * g3;
            int g = (int)(cy * m0 + y * m1);

            m0 = cx * b0 + x * b1;
            m1 = cx * b2 + x * b3;
            int b = (int)(cy * m0 + y * m1);

            return (a << 24) | (r << 16) | (g << 8) | b;
        }

    }

    /**
     * 阴影效果：提取文字 alpha 通道并高斯模糊后，按偏移量叠加到原图形成投影
     */
    public static class ShadowDistorted implements CaptchaTool.DistortedEngine {

        /**
         * 应用阴影效果
         *
         * @param baseImage 原始图片，不修改入参
         * @return 新生成的带阴影图片
         */
        public BufferedImage getDistortedImage(BufferedImage baseImage) {
            BufferedImage effectImage = baseImage;
            // 添加阴影
            effectImage = shadowFilter(effectImage);

            // 图像合成
            BufferedImage distortedImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graph = (Graphics2D) distortedImage.getGraphics();
            graph.drawImage(effectImage, 0, 0, null, null);
            graph.dispose();
            return distortedImage;
        }

        /**
         * 添加阴影
         */
        private BufferedImage shadowFilter( BufferedImage src) {

            //  阴影的角度。
            float angle = (float)Math.PI*6/4;
            //  阴影的模糊半径。
            float radius = 10;
            //  阴影与图像之间的间距。
            float distance = 5;
            //  阴影的透明度。
            float opacity = 1;

            //  获取图像的宽度和高度。
            int width = src.getWidth();
            int height = src.getHeight();

            //  根据角度计算阴影的x和y方向的偏移量。
            float xOffset = distance*(float)Math.cos(angle);
            float yOffset = -distance*(float)Math.sin(angle);

            //  设置阴影的颜色为黑色。
            float shadowR = (0) / 255f;
            float shadowG = (0) / 255f;
            float shadowB = (0) / 255f;

            //  设置提取阴影的通道矩阵。
            float[][] extractAlpha = {
                    { 0, 0, 0, shadowR },
                    { 0, 0, 0, shadowG },
                    { 0, 0, 0, shadowB },
                    { 0, 0, 0, opacity }
            };
            //  创建一个与源图像大小相同的空图像，用于存储阴影效果。
            BufferedImage shadow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            //  创建一个BandCombineOp对象，用于将阴影效果与源图像进行混合。
            new BandCombineOp( extractAlpha, null ).filter( src.getRaster(), shadow.getRaster() );
            //  对阴影效果进行高斯模糊处理。
            shadow = gaussianFilter (shadow, radius);

            // 创建一个与源图像大小相同的空图像，用于存储阴影效果。
            BufferedImage dst = CaptchaTool.WaterRippleDistorted.createCompatibleDestImage( src );
            // 创建一个Graphics2D对象，用于在dst图像上绘制阴影效果。
            Graphics2D graphics2D = dst.createGraphics();
            //  设置透明度，将阴影效果与源图像进行混合。
            graphics2D.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity ) );
            // 绘制阴影效果到dst图像上。
            graphics2D.drawRenderedImage( shadow, AffineTransform.getTranslateInstance( xOffset, yOffset ) );
            // 绘制源图像到dst图像上。
            graphics2D.setComposite(AlphaComposite.SrcOver);
            graphics2D.drawRenderedImage( src, null );
            // 释放Graphics2D对象资源。
            graphics2D.dispose();
            return dst;
        }

        /**
         * 高斯模糊
         *
         * 1、radius越大，模糊效果越明显，耗时越长；
         * 2、范围，0～20
         *
         * @param src    原始图片，不修改入参
         * @param radius 模糊半径，{@code <= 0} 时不执行卷积
         * @return 高斯模糊后的新图片
         */
        public BufferedImage gaussianFilter( BufferedImage src, float radius ) {
            //  获取图像的宽度和高度。
            int width = src.getWidth();
            int height = src.getHeight();

            //  创建一个与源图像大小相同的像素数组，用于存储源图像的像素数据。
            int[] inPixels = new int[width*height];
            int[] outPixels = new int[width*height];

            //  获取源图像的像素数据，并将其存储到inPixels数组中。
            src.getRGB( 0, 0, width, height, inPixels, 0, width );

            //  对源图像进行高斯模糊处理。
            if ( radius > 0 ) {
                Kernel kernel = makeKernel(radius);
                convolveAndTranspose(kernel, inPixels, outPixels, width, height, true, false);
                convolveAndTranspose(kernel, outPixels, inPixels, height, width,  false, true);
            }

            // 创建一个与源图像大小相同的空图像，用于存储高斯模糊效果。
            BufferedImage dst = CaptchaTool.WaterRippleDistorted.createCompatibleDestImage( src );
            // 将高斯模糊效果绘制到dst图像上。
            dst.setRGB( 0, 0, width, height, inPixels, 0, width );
            return dst;
        }

        /**
         * 高斯矩阵
         *
         * 1、功能：图像模糊
         * 2、原理：高斯矩阵是一种用于图像模糊处理的矩阵，通过将高斯函数与图像进行卷积运算来对图像进行处理。
         * 3、注意：radius越大，模糊效果越明显，耗时越长。
         *
         * @param radius 模糊半径
         * @return 归一化后的一维高斯卷积核
         */
        public static Kernel makeKernel(float radius) {
            int r = (int)Math.ceil(radius);
            int rows = r*2+1;
            float[] matrix = new float[rows];
            float sigma = radius/3;
            float sigma22 = 2*sigma*sigma;
            float sigmaPi2 = 2* ((float)Math.PI)*sigma;
            float sqrtSigmaPi2 = (float)Math.sqrt(sigmaPi2);
            float radius2 = radius*radius;
            float total = 0;
            int index = 0;
            for (int row = -r; row <= r; row++) {
                float distance = row*row;
                if (distance > radius2){
                    matrix[index] = 0;
                } else{
                    matrix[index] = (float)Math.exp(-(distance)/sigma22) / sqrtSigmaPi2;
                }
                total += matrix[index];
                index++;
            }
            for (int i = 0; i < rows; i++){
                matrix[i] /= total;
            }
            return new Kernel(rows, 1, matrix);
        }

        /**
         * 图像卷积
         *
         * 1、功能：图像模糊、锐化、边缘检测等。
         * 2、原理：图像卷积是一种线性滤波操作，通过将滤波器与图像进行卷积运算来对图像进行处理。
         * 3、注意：premultiply和unpremultiply参数用于控制图像的预乘和反预乘操作。
         *
         * @param kernel       卷积核
         * @param inPixels     输入像素数组（ARGB）
         * @param outPixels    输出像素数组，结果会转置写入
         * @param width        输入区域宽度
         * @param height       输入区域高度
         * @param premultiply  是否在卷积前按 alpha 预乘 RGB
         * @param unpremultiply 是否在卷积后反预乘 RGB
         */
        public static void convolveAndTranspose(Kernel kernel, int[] inPixels, int[] outPixels, int width, int height, boolean premultiply, boolean unpremultiply) {

            float[] matrix = kernel.getKernelData( null );
            int cols = kernel.getWidth();
            int cols2 = cols/2;

            for (int y = 0; y < height; y++) {
                int index = y;
                int ioffset = y*width;
                for (int x = 0; x < width; x++) {
                    float r = 0, g = 0, b = 0, a = 0;
                    for (int col = -cols2; col <= cols2; col++) {
                        float f = matrix[cols2 +col];

                        if (f != 0) {
                            int ix = x+col;

                            if ( ix < 0 ) {
                                ix = 0;
                            } else if ( ix >= width) {
                                ix = width - 1;
                            }

                            int rgb = inPixels[ioffset+ix];
                            int pa = (rgb >> 24) & 0xff;
                            int pr = (rgb >> 16) & 0xff;
                            int pg = (rgb >> 8) & 0xff;
                            int pb = rgb & 0xff;
                            if ( premultiply ) {
                                float a255 = pa * (1.0f / 255.0f);
                                pr *= (int) a255;
                                pg *= (int) a255;
                                pb *= (int) a255;
                            }
                            a += f * pa;
                            r += f * pr;
                            g += f * pg;
                            b += f * pb;
                        }
                    }
                    if ( unpremultiply && a != 0 && a != 255 ) {
                        float f = 255.0f / a;
                        r *= f;
                        g *= f;
                        b *= f;
                    }
                    int ia = CaptchaTool.WaterRippleDistorted.clamp((int)(a+0.5));
                    int ir = CaptchaTool.WaterRippleDistorted.clamp((int)(r+0.5));
                    int ig = CaptchaTool.WaterRippleDistorted.clamp((int)(g+0.5));
                    int ib = CaptchaTool.WaterRippleDistorted.clamp((int)(b+0.5));
                    outPixels[index] = (ia << 24) | (ir << 16) | (ig << 8) | ib;
                    index += height;
                }
            }
        }

    }

    /**
     * 波纹效果：使用随机振幅与波长的正弦函数对 X/Y 方向做位移，形成波浪形扭曲
     */
    public static class RippleDistorted implements CaptchaTool.DistortedEngine {

        /**
         * 应用波纹扭曲
         *
         * @param baseImage 原始图片，不修改入参
         * @return 新生成的扭曲图片
         */
        public BufferedImage getDistortedImage(BufferedImage baseImage) {
            // 添加波纹
            BufferedImage effectImage = rippleFilter(baseImage);

            // 图像合成
            BufferedImage distortedImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graph = (Graphics2D) distortedImage.getGraphics();
            graph.drawImage(effectImage, 0, 0, null, null);
            graph.dispose();
            return distortedImage;
        }

        /**
         * 波纹
         *
         * 1、功能：添加波纹效果
         * 2、原理：通过正弦函数对每个像素的源坐标做 X/Y 方向位移，再用双线性插值取色。
         *
         * @param src 原始图片，不修改入参
         * @return 应用波纹后的新图片
         */
        public BufferedImage rippleFilter( BufferedImage src) {
            // 波纹参数
            Random rand = new Random(); //SecureRandom();
            // 波纹的宽度
            float xAmplitude = 7.6f;
            // 波纹的高度
            float yAmplitude = rand.nextFloat() + 1.0f;
            // x波长
            float xWavelength = rand.nextInt(7) + 8;
            // y波长
            float yWavelength = rand.nextInt(3) + 2;

            // 波纹效果的范围
            int width = src.getWidth();
            int height = src.getHeight();
            Rectangle transformedSpace = new Rectangle(0, 0, width, height);

            // 获取源图像的色模型
            ColorModel dstCM = src.getColorModel();
            // 创建一个新的BufferedImage对象，用于存储波纹效果后的图像
            BufferedImage dst  = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(transformedSpace.width, transformedSpace.height), dstCM.isAlphaPremultiplied(), null);

            // 获取源图像的像素数据
            int[] inPixels = CaptchaTool.WaterRippleDistorted.getRGB( src, 0, 0, width, height, null );

            // 计算源图像的宽度和高度
            int srcWidth1 = width-1;
            int srcHeight1 = height-1;

            // 计算波纹效果后的图像的宽度和高度
            int outWidth = transformedSpace.width;
            int outHeight = transformedSpace.height;

            // 创建一个整型数组，用于存储波纹效果后的图像的像素数据
            int[] outPixels = new int[outWidth];

            // 计算波纹效果后的图像的像素数据
            int outX = transformedSpace.x;
            int outY = transformedSpace.y;

            // 计算波纹效果后的图像的像素数据
            float[] out = new float[2];

            //  遍历波纹效果后的图像的每个像素点，计算每个像素点的坐标和颜色值
            for (int y = 0; y < outHeight; y++) {
                for (int x = 0; x < outWidth; x++) {
                    // 计算每个像素点的坐标和颜色值
                    transformInverseForRipple(outX+x, outY+y, out, xWavelength, yWavelength, xAmplitude, yAmplitude);

                    // 计算输入图像的坐标
                    int srcX = (int)Math.floor( out[0] );
                    int srcY = (int)Math.floor( out[1] );
                    float xWeight = out[0]-srcX;
                    float yWeight = out[1]-srcY;

                    // 计算输入图像的四个像素点
                    int nw, ne, sw, se;

                    // 判断输入图像的坐标是否越界
                    if ( srcX >= 0 && srcX < srcWidth1 && srcY >= 0 && srcY < srcHeight1) {
                        // 当计算出的源坐标在图像范围内时直接获取像素值
                        int i = width *srcY + srcX;
                        nw = inPixels[i];
                        ne = inPixels[i+1];
                        sw = inPixels[i+ width];
                        se = inPixels[i+ width +1];
                    } else {
                        // 当坐标超出图像范围时，通过getPixel函数处理边界情况
                        nw = CaptchaTool.WaterRippleDistorted.getPixel( inPixels, srcX, srcY, width, height );
                        ne = CaptchaTool.WaterRippleDistorted.getPixel( inPixels, srcX+1, srcY, width, height);
                        sw = CaptchaTool.WaterRippleDistorted.getPixel( inPixels, srcX, srcY+1, width, height );
                        se = CaptchaTool.WaterRippleDistorted.getPixel( inPixels, srcX+1, srcY+1, width, height);
                    }

                    // 进行双线性插值计算，将像素值赋值给输出像素数组
                    outPixels[x] = CaptchaTool.WaterRippleDistorted.bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se);
                }
                //  将波纹效果后的图像的像素数据设置到目标图像中
                CaptchaTool.WaterRippleDistorted.setRGB( dst, 0, y, transformedSpace.width, 1, outPixels );
            }
            return dst;
        }


        /**
         * 实现波纹扭曲效果
         * 1、接收一个坐标点(x,y)和一系列参数，然后计算出经过波纹扭曲后的新坐标
         *
         * @param x          原始 X 坐标
         * @param y          原始 Y 坐标
         * @param out        输出数组，长度至少为 2，写入新坐标 [x, y]
         * @param xWavelength X 方向波长
         * @param yWavelength Y 方向波长
         * @param xAmplitude  X 方向振幅
         * @param yAmplitude  Y 方向振幅
         */
        protected void transformInverseForRipple(int x, int y, float[] out,
                                                 float xWavelength, float yWavelength,
                                                 float xAmplitude, float yAmplitude) {

            // 计算扭曲
            float nx = (float)y / xWavelength;
            float ny = (float)x / yWavelength;
            float fx = (float) Math.sin(nx);
            float fy = (float)Math.sin(ny);

            // 叠加振幅
            out[0] = x + xAmplitude * fx;
            out[1] = y + yAmplitude * fy;
        }

    }


    // ---------------------- other ----------------------

}
