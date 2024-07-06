package com.xxl.tool.freemarker;

import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

/**
 * freemarker tool
 *
 * @author xuxueli 2018-05-02 19:56:00
 */
public class FreemarkerTool {
    private static final Logger logger = LoggerFactory.getLogger(FreemarkerTool.class);

    /**
     * freemarker config
     */
    private static Configuration freemarkerConfig = null;

    /**
     * init with prop
     *
     * @param templatePath
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
     * init with specified freemarkerConfig
     *
     * @param freemarkerConfig
     */
    public static void init(Configuration freemarkerConfig){
        FreemarkerTool.freemarkerConfig = freemarkerConfig;
    }


    // ---------------------- process string data by template ----------------------

    /**
     * process Template Into String
     *
     * @param template
     * @param model
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String processTemplateIntoString(Template template, Object model)
            throws IOException, TemplateException {

        StringWriter result = new StringWriter();
        template.process(model, result);
        return result.toString();
    }

    /**
     * process and generate String with default freemarkerConfig
     *
     * @param templateName
     * @param params
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String processString(String templateName, Map<String, Object> params)
            throws IOException, TemplateException {

        Template template = freemarkerConfig.getTemplate(templateName);
        String htmlText = processTemplateIntoString(template, params);
        return htmlText;
    }

    /**
     * process and generate String with specified freemarkerConfig
     *
     * @param freemarkerConfig
     * @param templateName
     * @param params
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    public static String processString(Configuration freemarkerConfig, String templateName, Map<String, Object> params)
            throws IOException, TemplateException {

        Template template = freemarkerConfig.getTemplate(templateName);
        String htmlText = processTemplateIntoString(template, params);
        return htmlText;
    }

}
