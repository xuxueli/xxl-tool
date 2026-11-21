package com.xxl.tool.freemarker;

import com.xxl.tool.error.BizException;
import freemarker.core.TemplateClassResolver;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * FreeMarker 模板工具：从模板目录加载模板并渲染为字符串。
 *
 * <p>使用前需通过 {@link #init(String)} 指定模板根目录，或通过
 * {@link #init(Configuration)} 注入自定义配置；未初始化时调用
 * {@link #processString(String, Map)} 会因全局配置为 {@code null} 抛出
 * {@link NullPointerException}。
 *
 * <p>{@link #init(String)} 的默认配置：UTF-8、数字格式
 * {@code "0.##########"}、{@code TemplateClassResolver.SAFER_RESOLVER}、
 * classic compatible、{@code Locale.CHINA}、异常处理器
 * {@code RETHROW_HANDLER}。模板名相对于模板根目录解析，渲染结果始终是内存中的
 * 字符串，不写文件。
 *
 * <p>配置保存在静态字段中，属于进程级全局状态：应在启动阶段初始化一次，
 * 初始化与渲染不做额外同步，重新初始化会影响所有调用方；并发渲染依赖
 * FreeMarker {@link Configuration} 自身的线程安全保证。FreeMarker 为可选依赖
 * （provided，需使用方自行引入）。
 *
 * @author xuxueli 2018-05-02 19:56:00
 */
public class FtlTool {
    private static final Logger logger = LoggerFactory.getLogger(FtlTool.class);

    /**
     * freemarker config
     */
    private static Configuration freemarkerConfig = null;

    /**
     * 使用模板根目录初始化全局 FreeMarker 配置。
     *
     * <p>会覆盖之前的配置并重置为默认项（见类说明）。模板根目录应是已存在的
     * 目录，后续 {@code templateName} 相对该目录解析。目录不可用时仅记录 error
     * 日志、不抛出异常，此时全局配置已创建但模板加载可能失败。全局状态，
     * 非线程安全，建议启动阶段调用一次。
     *
     * @param templatePath 模板根目录路径，不能为 {@code null}
     * @throws NullPointerException {@code templatePath} 为 {@code null}
     */
    public static void init(String templatePath){
        try {
            freemarkerConfig = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

            freemarkerConfig.setDirectoryForTemplateLoading(new File(templatePath));
            freemarkerConfig.setDefaultEncoding("UTF-8");
            freemarkerConfig.setNumberFormat("0.##########");
            freemarkerConfig.setNewBuiltinClassResolver(TemplateClassResolver.SAFER_RESOLVER);
            freemarkerConfig.setClassicCompatible(true);
            freemarkerConfig.setLocale(Locale.CHINA);
            freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 使用外部 {@link Configuration} 替换全局配置。
     *
     * <p>直接赋值，不做校验或复制；模板加载目录、编码、异常处理等均由调用方
     * 负责配置。全局状态，非线程安全，建议启动阶段调用一次。
     *
     * @param freemarkerConfig FreeMarker 配置，不能为 {@code null}
     */
    public static void init(Configuration freemarkerConfig){
        FtlTool.freemarkerConfig = freemarkerConfig;
    }


    // ---------------------- process string data by template ----------------------

    /**
     * 使用给定模板渲染数据模型，返回渲染结果字符串。
     *
     * <p>{@code model} 可为 {@link Map}、JavaBean 或 FreeMarker 模板模型；
     * 渲染结果写入内存 {@link StringWriter}，不写文件。模板语法/取值异常原样抛出。
     *
     * @param template 已加载的模板，不能为 {@code null}
     * @param model 数据模型，可为 {@code null}（适用于无变量模板）
     * @return 渲染结果
     * @throws IOException 读取模板或写出结果失败
     * @throws TemplateException 模板解析/渲染失败
     */
    public static String processTemplateIntoString(Template template, Object model)
            throws IOException, TemplateException {

        StringWriter result = new StringWriter();
        template.process(model, result);
        return result.toString();
    }

    /**
     * 使用全局配置渲染指定模板，返回字符串。
     *
     * <p>需先调用 {@link #init(String)} 或 {@link #init(Configuration)}；
     * {@code templateName} 相对模板根目录解析（如 {@code "mail/test.ftl"}）。
     * 渲染结果保存在内存中，不写文件。
     *
     * @param templateName 模板名（相对模板根目录）
     * @param params 模板数据模型，可为 {@code null}
     * @return 渲染结果
     * @throws IOException 模板不存在或读取失败
     * @throws TemplateException 模板解析/渲染失败
     * @throws NullPointerException 未初始化全局配置
     */
    public static String processString(String templateName, Map<String, Object> params)
            throws IOException, TemplateException {

        Template template = freemarkerConfig.getTemplate(templateName);
        String htmlText = processTemplateIntoString(template, params);
        return htmlText;
    }

    /**
     * 使用指定配置渲染模板，返回字符串（不修改全局配置）。
     *
     * <p>{@code templateName} 相对该配置的模板加载目录解析；渲染结果保存在
     * 内存中，不写文件。
     *
     * @param freemarkerConfig FreeMarker 配置，不能为 {@code null}
     * @param templateName 模板名（相对该配置的模板根目录）
     * @param params 模板数据模型，可为 {@code null}
     * @return 渲染结果
     * @throws IOException 模板不存在或读取失败
     * @throws TemplateException 模板解析/渲染失败
     */
    public static String processString(Configuration freemarkerConfig, String templateName, Map<String, Object> params)
            throws IOException, TemplateException {

        Template template = freemarkerConfig.getTemplate(templateName);
        String htmlText = processTemplateIntoString(template, params);
        return htmlText;
    }

    // ---------------------- process string data by template ----------------------
    /**
     * 静态包装器
     * //BeansWrapper.getDefaultInstance();
     */
    private static BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();

    /**
     * 生成静态方法模型，供模板以 {@code 类名.方法名(...)} 调用 Java 静态方法。
     *
     * <p>参数实际是类的全限定名（历史参数名 {@code packageName}），例如
     * {@code I18nUtil.class.getName()}；返回的模型需以变量形式放入数据模型。
     * 仅应暴露可信类，避免模板调用敏感静态方法。
     *
     * <pre>
     *     TemplateHashModel model = FtlTool.generateStaticModel(I18nUtil.class.getName());
     *     params.put("I18nUtil", model);
     * </pre>
     *
     * @param packageName 类的全限定名（并非包名），不能为 {@code null}
     * @return 静态方法模型
     * @throws BizException 类无法加载或构建静态模型失败
     */
    public static TemplateHashModel generateStaticModel(String packageName) {
        try {
            TemplateHashModel staticModels = wrapper.getStaticModels();
            TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(packageName);
            return fileStatics;
        } catch (Exception e) {
            throw new BizException(e);
        }
    }

}
