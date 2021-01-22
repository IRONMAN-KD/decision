package com.decision.agent;

import com.decision.core.util.DecisionUtils;
import com.decision.core.util.LogbackUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * DecisionAgent启动器
 *
 * @Author KD
 * @Date 2021/1/20 15:46
 */
public class AgentLauncher {
    private final Logger logger = LoggerFactory.getLogger(AgentLauncher.class);

    private static final String CORE_CONFIGURE_CLASS = "com.decision.core.CoreConfigure";
    private static final String CORE_LAUNCHER_CLASS = "com.decision.core.CoreLauncher";

    public static void premain(String arguments, Instrumentation inst) {
        initialize(inst);
        AgentBuilder agentBuilder = new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(buildListener())
                .disableClassFormatChanges()
                .ignore(ElementMatchers.<TypeDescription>none().and(ElementMatchers.nameStartsWith("net.beeapm.agent.")));
        for (int i = 0; i < plugins.size(); i++) {
            final AbstractPlugin plugin = plugins.get(i);
            InterceptPoint[] interceptPoints = plugin.buildInterceptPoint();
            for (int j = 0; j < interceptPoints.length; j++) {
                final InterceptPoint interceptPoint = interceptPoints[j];
                AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
                    private final ILog log = LogFactory.getLog("Transform");

                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                            TypeDescription typeDescription,
                                                            ClassLoader classLoader, JavaModule javaModule) {
                        String className = typeDescription.getName();
                        log.exec("class-name={}, plugin-name={}", className, plugin.getName());
                        builder = builder.visit(Advice.to(plugin.interceptorAdviceClass()).on(interceptPoint.buildMethodsMatcher()));
                        FieldDefine[] fields = plugin.buildFieldDefine();
                        if (fields != null && fields.length > 0) {
                            for (int x = 0; x < fields.length; x++) {
                                builder = builder.defineField(fields[x].name, fields[x].type, fields[x].modifiers);
                            }
                        }
                        return builder;
                    }
                };
                agentBuilder = agentBuilder.type(interceptPoint.buildTypesMatcher()).transform(transformer);
            }
        }
        agentBuilder.installOn(inst);
    }

    private static void initialize(Instrumentation inst) {
        try {
            String decisionHome = DecisionUtils.getDecisionHomePath();
            //将spy植入bootstrapClassLoader的搜索范围，即使用BootstrapClassLoader加载spy的类
            inst.appendToBootstrapClassLoaderSearch(new JarFile(new File(getDecisionSpyJarPath(decisionHome))));
            //初始化日志框架
            LogbackUtils.init(getDecisionLogCfgPath(decisionHome));
            //使用自定义classloader，尽量避免agent类影响业务系统
            final DecisionClassLoader decisionClassLoader = new DecisionClassLoader(getDecisionCoreJarPath(decisionHome));

            Class<?> classOfConfigure = decisionClassLoader.loadClass(CORE_CONFIGURE_CLASS);
            Object instanceOfConfigure = classOfConfigure.getMethod("getInstance", String.class)
                    .invoke(null, getDecisionPropertiesPath(decisionHome));
            Class<?> classOfCoreLauncher = decisionClassLoader.loadClass(CORE_LAUNCHER_CLASS);
            Object instanceOfCoreLauncher = classOfCoreLauncher.getMethod("getInstance").invoke(null);
            classOfCoreLauncher.getMethod("init", classOfConfigure, Instrumentation.class)
                    .invoke(instanceOfCoreLauncher, instanceOfConfigure, inst);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static AgentBuilder.Listener buildListener() {
        return new AgentBuilder.Listener() {

            @Override
            public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

            }

            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
                WeavingClassLog.INSTANCE.log(typeDescription, dynamicType);
            }

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
            }

            @Override
            public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
                log.error("", throwable);
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

    private static String getDecisionModulePath(String decisionHome) {
        return decisionHome + File.separatorChar + "module";
    }

    private static String getDecisionCoreJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "lib" + File.separator + "decision-core.jar";
    }

    private static String getDecisionSpyJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "lib" + File.separator + "decision-spy.jar";
    }

}
