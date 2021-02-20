package com.decision.core.manager.loader;

import com.decision.core.classloader.AgentClassLoader;
import com.decision.core.plugin.interceptor.InstanceAroundInterceptor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author KD
 * @Date 2021/2/1 17:33
 */
public class InterceptorInstanceLoader {
    private static ConcurrentHashMap<String, InstanceAroundInterceptor> INSTANCE_CACHE = new ConcurrentHashMap<String, InstanceAroundInterceptor>();
    private static ReentrantLock INSTANCE_LOAD_LOCK = new ReentrantLock();
    private static Map<ClassLoader, ClassLoader> EXTEND_PLUGIN_CLASSLOADERS = new HashMap<ClassLoader, ClassLoader>();


    /**
     * 加载拦截处理类
     *
     * @param className
     * @param targetClassLoader
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static InstanceAroundInterceptor load(String className, ClassLoader targetClassLoader, String decisionHome) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        if (targetClassLoader == null) {
            targetClassLoader = InterceptorInstanceLoader.class.getClassLoader();
        }
        String instanceKey = className + "_OF_" + targetClassLoader.getClass()
                .getName() + "@" + Integer.toHexString(targetClassLoader
                .hashCode());
        InstanceAroundInterceptor inst = INSTANCE_CACHE.get(instanceKey);
        if (inst == null) {
            ClassLoader pluginLoader = null;
            INSTANCE_LOAD_LOCK.lock();
            try {
                pluginLoader = EXTEND_PLUGIN_CLASSLOADERS.get(targetClassLoader);
                if (pluginLoader == null) {
                    pluginLoader = new AgentClassLoader(targetClassLoader, getDecisionPluginJarPath(decisionHome));
                    EXTEND_PLUGIN_CLASSLOADERS.put(targetClassLoader, pluginLoader);
                }
            } finally {
                INSTANCE_LOAD_LOCK.unlock();
            }
            inst = (InstanceAroundInterceptor) Class.forName(className, true, pluginLoader).newInstance();
            if (inst != null) {
                INSTANCE_CACHE.put(instanceKey, inst);
            }
        }

        return inst;
    }

    private static String getDecisionPluginJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "plugins";
    }
}
