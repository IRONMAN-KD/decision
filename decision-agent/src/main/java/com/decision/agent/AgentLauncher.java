package com.decision.agent;

import com.decision.core.classloader.DecisionClassLoader;
import com.decision.core.plugin.DecisionPluginDefine;
import com.decision.core.plugin.PluginInterceptPoint;
import com.decision.core.plugin.interceptor.InstanceInterceptorProxy;
import com.decision.core.util.DecisionUtils;
import com.decision.core.util.LogbackUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

/**
 * DecisionAgent启动器
 *
 * @Author KD
 * @Date 2021/1/20 15:46
 */
public class AgentLauncher {
    private static final Logger logger = LoggerFactory.getLogger(AgentLauncher.class);

    private static final String CORE_CONFIGURE_CLASS = "com.decision.core.CoreConfigure";
    private static final String CORE_LAUNCHER_CLASS = "com.decision.core.CoreLauncher";

    public static void premain(String arguments, Instrumentation inst) {
        initialize(inst);
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(buildListener())
                .disableClassFormatChanges()
                .ignore(ElementMatchers.<TypeDescription>none().and(ElementMatchers.nameStartsWith("com.decision.agent.")));
        List<DecisionPluginDefine> plugins = loadPlugins();
        for (int i = 0; i < plugins.size(); i++) {
            final DecisionPluginDefine plugin = plugins.get(i);
            PluginInterceptPoint[] interceptPoints = plugin.getInterceptPoint();
            for (int j = 0; j < interceptPoints.length; j++) {
                final PluginInterceptPoint interceptPoint = interceptPoints[j];
                AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                            TypeDescription typeDescription,
                                                            ClassLoader classLoader, JavaModule javaModule) {
                        builder = builder.method(interceptPoint.buildMethodsMatcher())
                                .intercept(MethodDelegation.withDefaultConfiguration()
                                        .to(new InstanceInterceptorProxy(plugin.interceptorAdviceClass().getName())));
                        return builder;
                    }
                };
                agentBuilder = agentBuilder.type(interceptPoint.buildTypesMatcher()).transform(transformer);
            }
        }
        agentBuilder.installOn(inst);
    }

    private static void initialize(Instrumentation) {
        try {
            String decisionHome = DecisionUtils.getDecisionHomePath();
            //初始化日志框架
            LogbackUtils.init(getDecisionLogCfgPath(decisionHome));


        } catch (Exception e) {
            logger.error("init decision error :{}", e);
        }

    }

    private static List<DecisionPluginDefine> loadPlugins() {
        List<DecisionPluginDefine> plugins = new ArrayList<DecisionPluginDefine>();
        try {
            String decisionHome = DecisionUtils.getDecisionHomePath();
            //使用自定义classloader，尽量避免agent类影响业务系统
            final DecisionClassLoader decisionClassLoader = new DecisionClassLoader(getDecisionCoreJarPath(decisionHome));
            Class<?> classOfCoreLauncher = decisionClassLoader.loadClass(CORE_LAUNCHER_CLASS);
            Object instanceOfCoreLauncher = classOfCoreLauncher.getMethod("getInstance").invoke(null);
            plugins = (List<DecisionPluginDefine>) classOfCoreLauncher.getMethod("loadPlugins", String.class)
                    .invoke(instanceOfCoreLauncher, getDecisionPluginJarPath(decisionHome));
        } catch (Exception e) {
            logger.error("load plugin error :{}", e);
        }
        return plugins;
    }

    private static AgentBuilder.Listener buildListener() {
        return new AgentBuilder.Listener() {

            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
                logger.error("", throwable);
            }

            @Override
            public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }
        };
    }

    private static String getDecisionCfgPath(String decisionHome) {
        return decisionHome + File.separatorChar + "cfg";
    }

    private static String getDecisionLogCfgPath(String decisionHome) {
        return getDecisionCfgPath(decisionHome) + File.separatorChar + "decision-logback.xml";
    }

    private static String getDecisionPropertiesPath(String decisionHome) {
        return decisionHome + File.separatorChar + "decision.properties";
    }


    private static String getDecisionCoreJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "lib" + File.separator + "decision-core.jar";
    }

    private static String getDecisionPluginJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "plugin";
    }

}
