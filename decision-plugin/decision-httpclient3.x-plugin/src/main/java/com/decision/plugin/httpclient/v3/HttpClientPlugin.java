package com.decision.plugin.httpclient.v3;

import com.decision.core.plugin.DecisionPlugin;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.kohsuke.MetaInfServices;

/**
 * @Author KD
 * @Date 2021/2/8 14:23
 */
@MetaInfServices(DecisionPluginDefine.class)
@DecisionPlugin(id = "httpClientV3", version = "1.0.0", author = "KD")
public class HttpClientPlugin implements DecisionPluginDefine {
    private static final String ENHANCE_CLASS = "org.apache.commons.httpclient.HttpClient";
    private static final String INTERCEPT_CLASS = "com.decision.plugin.httpclient.v3.interceptor.HttpClientInterceptor";

    @Override
    public PluginInterceptPoint[] getInterceptPoint() {
        return new PluginInterceptPoint[]{
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_CLASS))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                .and(ElementMatchers.takesArguments(3))
                                .and(ElementMatchers.takesArgument(0, ElementMatchers.named("org.apache.commons.httpclient.HostConfiguration")))
                                .and(ElementMatchers.takesArgument(1, ElementMatchers.named("org.apache.commons.httpclient.HttpMethod")))
                                .and(ElementMatchers.takesArgument(2, ElementMatchers.named("org.apache.commons.httpclient.HttpState")))
                                .and(ElementMatchers.<MethodDescription>named("executeMethod"));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_CLASS;
                    }
                }
        };
    }
}
