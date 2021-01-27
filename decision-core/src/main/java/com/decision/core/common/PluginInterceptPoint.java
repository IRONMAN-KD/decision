package com.decision.core.common;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 拦截点接口
 *
 * @Author linkedong@vv.cn
 * @Date 2021/1/27 11:32
 */
public interface PluginInterceptPoint {
    /**
     * 类匹配规则
     *
     * @return
     */
    ElementMatcher<TypeDescription> buildTypesMatcher();

    /**
     * 方法匹配规则
     *
     * @return
     */
    ElementMatcher<MethodDescription> buildMethodsMatcher();
}
