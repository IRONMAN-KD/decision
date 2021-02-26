package com.decision.plugin.thread;

import com.decision.core.plugin.DecisionPlugin;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.kohsuke.MetaInfServices;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;

/**
 * @Author KD
 * @Date 2021/2/3 16:58
 */
@MetaInfServices(DecisionPluginDefine.class)
@DecisionPlugin(id = "threadPool", version = "1.0.0", author = "KD")
public class ThreadPoolPlugin implements DecisionPluginDefine {
    private static final String ENHANCE_EXECUTOR_SERVICE_CLASS = "java.util.concurrent.ExecutorService";
    private static final String ENHANCE_COMPLETION_SERVICE_CLASS = "java.util.concurrent.CompletionService";
    private static final String ENHANCE_FORK_JOIN_CLASS = "java.util.concurrent.ForkJoinPool";
    private static final String INTERCEPT_THREAD_POOL_CLASS = "com.decision.plugin.thread.interceptor.ForkJoinPoolInterceptor";

    @Override
    public PluginInterceptPoint[] getInterceptPoint() {
        return new PluginInterceptPoint[]{
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.any()
                                .and(ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_EXECUTOR_SERVICE_CLASS))
                                        .or(ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_COMPLETION_SERVICE_CLASS))))
                                .and(ElementMatchers.not(ElementMatchers.isInterface()))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                .and(ElementMatchers.named("submit")
                                        .or(ElementMatchers.named("execute")))
                                .and(ElementMatchers.takesArgument(0, Runnable.class)
                                        .or(ElementMatchers.takesArgument(0, Callable.class)));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_THREAD_POOL_CLASS;
                    }
                },
                new PluginInterceptPoint() {
                    @Override
                    public ElementMatcher<TypeDescription> buildTypesMatcher() {
                        ElementMatcher.Junction<TypeDescription> matcher = ElementMatchers.hasSuperType(ElementMatchers.named(ENHANCE_FORK_JOIN_CLASS))
                                .and(ElementMatchers.not(ElementMatchers.<TypeDescription>isAbstract()));
                        return matcher;
                    }

                    @Override
                    public ElementMatcher<MethodDescription> buildMethodsMatcher() {
                        return ElementMatchers.isMethod()
                                .and(ElementMatchers.named("submit")
                                        .or(ElementMatchers.named("execute"))
                                        .or(ElementMatchers.named("schedule"))
                                        .or(ElementMatchers.named("invoke")))
                                .and(ElementMatchers.takesArgument(0, Runnable.class)
                                        .or(ElementMatchers.takesArgument(0, Callable.class))
                                        .or(ElementMatchers.takesArgument(0, ForkJoinTask.class)));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPT_THREAD_POOL_CLASS;
                    }
                }
        };
    }
}
