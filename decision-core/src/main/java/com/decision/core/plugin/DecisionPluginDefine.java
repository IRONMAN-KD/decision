package com.decision.core.plugin;

/**
 * 插件接口
 * 要求插件必须要服务SPI规范
 *
 * @Author KD
 * @Date 2021/1/22 10:56
 */
public interface DecisionPluginDefine {

    /**
     * 拦截点
     *
     * @return
     */
    PluginInterceptPoint[] getInterceptPoint();

    /**
     * 拦截器名称
     *
     * @return
     */
    Class interceptorAdviceClass();

}
