package com.decision.core;

import com.decision.core.manager.loader.InterceptorInstanceLoader;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import com.decision.core.plugin.PluginLoader;
import com.decision.core.plugin.interceptor.InstanceInterceptorProxy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * 该类在初始化时与业务系统进行隔离
 *
 * @Author linkedong@vv.cn
 * @Date 2021/2/3 16:10
 */
public class CoreLauncher {
    private final Logger LOGGER = LoggerFactory.getLogger(CoreLauncher.class);


}
