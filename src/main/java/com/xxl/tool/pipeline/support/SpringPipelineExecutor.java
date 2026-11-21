package com.xxl.tool.pipeline.support;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.pipeline.Pipeline;
import com.xxl.tool.pipeline.PipelineExecutor;
import com.xxl.tool.pipeline.PipelineHandler;
import com.xxl.tool.pipeline.PipelineStatus;
import com.xxl.tool.pipeline.config.PipelineConfig;
import com.xxl.tool.pipeline.config.PipelineConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

/**
 * 集成 Spring 的 Pipeline 执行器
 *
 * <p>在 {@link PipelineExecutor} 基础上，从 Spring 容器收集 {@link PipelineHandler} Bean，
 * 按 {@link PipelineConfigLoader} 提供的配置装配 Pipeline，并在容器单例初始化完成后自动注册。</p>
 *
 * <p>生命周期：由 Spring 回调 {@link #afterSingletonsInstantiated()} 触发初始化，
 * {@link #destroy()} 在容器关闭时回调（当前仅记录日志，不清理注册表）。
 * 未提供 {@link ApplicationContext} 或 {@link PipelineConfigLoader}、配置为空、
 * 容器中无 Handler 时初始化直接跳过并返回，不抛异常。</p>
 *
 * <p>Spring 为 provided 可选依赖；未引入 Spring 时仅在使用本类时报错，不影响其他 Pipeline 类加载。
 * 初始化完成后注册表并发安全，请勿在运行期重复初始化。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class SpringPipelineExecutor extends PipelineExecutor
        implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(SpringPipelineExecutor.class);

    private ApplicationContext applicationContext;
    private PipelineConfigLoader pipelineConfigLoader;

    /**
     * 设置 Pipeline 配置加载器
     *
     * <p>需在 {@link #afterSingletonsInstantiated()} 之前通过 Spring 注入，否则初始化会跳过。</p>
     *
     * @param pipelineConfigLoader   Pipeline 配置加载器
     */
    public void setPipelineConfigLoader(PipelineConfigLoader pipelineConfigLoader) {
        this.pipelineConfigLoader = pipelineConfigLoader;
    }

    /**
     * Spring 回调：注入应用上下文
     *
     * @param applicationContext    Spring 应用上下文
     * @throws BeansException   注入失败时抛出
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Spring 回调：全部单例初始化完成后装配并注册 Pipeline
     */
    @Override
    public void afterSingletonsInstantiated() {
        logger.info("SpringPipelineExecutor init.");
        init();
    }

    /**
     * 执行初始化：加载全部配置并按名称查找 Handler Bean 装配 Pipeline
     *
     * @return 初始化成功返回 {@code true}；缺少上下文、配置加载器、配置或 Handler 时返回 {@code false}
     */
    private boolean init() {
        if (applicationContext == null) {
            logger.info("SpringPipelineExecutor init fail. applicationContext is null.");
            return false;
        }
        if (pipelineConfigLoader == null) {
            logger.info("SpringPipelineExecutor init fail. pipelineConfigLoader is null.");
            return false;
        }
        // load config
        List<PipelineConfig> configList = pipelineConfigLoader.loadAll();
        if (CollectionTool.isEmpty(configList)) {
            logger.info("SpringPipelineExecutor init fail. configList is null.");
            return false;
        }
        // find handlers
        Map<String, PipelineHandler> handlerMap = applicationContext.getBeansOfType(PipelineHandler.class);
        if (MapTool.isEmpty(handlerMap)) {
            logger.info("SpringPipelineExecutor init fail. handlerMap is null.");
            return false;
        }
        // registry pipeline
        for (PipelineConfig config : configList) {
            boolean ret = registryByConfig(config, handlerMap);
            logger.info("SpringPipelineExecutor registryByConfig [{}], config:{}", ret, config);
        }
        logger.info("SpringPipelineExecutor init success.");
        return true;
    }

    /**
     * 按配置装配并注册单个 Pipeline
     *
     * <p>配置中的 Handler 名称按 Spring Bean 名称查找，顺序即执行顺序；任一 Handler
     * 不存在则放弃注册并返回 {@code false}。装配出的 Pipeline 状态固定为运行中。</p>
     *
     * @param config        Pipeline 配置
     * @param handlerMap    Spring 容器中的 Handler Bean（Bean 名称 → 实例）
     * @return              注册成功返回 {@code true}；名称空白、Handler 缺失或注册被拒绝时返回 {@code false}
     */
    private boolean registryByConfig(PipelineConfig config, Map<String, PipelineHandler> handlerMap) {
        if (StringTool.isBlank(config.getName())) {
            logger.info("SpringPipelineExecutor registryByConfig[{}] fail. name is null.", config);
            return false;
        }
        if (CollectionTool.isEmpty(config.getHandlerList())) {
            logger.info("SpringPipelineExecutor registryByConfig[{}] fail. handlerList is null.");
        }

        // pipeline
        Pipeline pipeline = new Pipeline()
                .name(config.getName())
                .status(PipelineStatus.RUNTIME.getStatus());

        // add handler
        for (String handlerName : config.getHandlerList()) {
            PipelineHandler handler = handlerMap.get(handlerName);
            if (handler == null) {
                logger.info("SpringPipelineExecutor registryByConfig[{}] fail. handlerName[{}] not exists.", config, handlerName);
                return false;
            }
            pipeline.addLast(handler);
        }

        // registry
        return this.registry(pipeline);
    }

    /**
     * Spring 回调：容器销毁
     *
     * <p>当前仅记录日志，不清理已注册的 Pipeline。</p>
     *
     * @throws Exception 销毁失败时抛出
     */
    @Override
    public void destroy() throws Exception {
        logger.info("SpringPipelineExecutor destroy.");
    }

}

