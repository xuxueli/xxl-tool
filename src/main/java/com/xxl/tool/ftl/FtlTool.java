package com.xxl.tool.ftl;

import com.xxl.tool.exception.BizException;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;

/**
 * ftl tool
 *
 * @author xuxueli 2018-01-17 20:37:48
 */
public class FtlTool {

    // 静态包装器
    private static BeansWrapper wrapper = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();     //BeansWrapper.getDefaultInstance();

    /**
     * 生成 freemarker 自定义方法
     *
     * <pre>
     *     // 自定义方法包装；
     *     TemplateHashModel model = FtlTool.generateStaticModel(I18nUtil.class.getName())
     *     // 注入SpringMVC响应对象，逻辑运营在 “AsyncHandlerInterceptor.postHandle” 中；
     *     modelAndView.addObject("I18nUtil", model);
     * </pre>
     *
     * @param packageName
     * @return
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
