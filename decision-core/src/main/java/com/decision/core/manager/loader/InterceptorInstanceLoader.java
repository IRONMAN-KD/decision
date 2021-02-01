package com.decision.core.manager.loader;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author linkedong@vv.cn
 * @Date 2021/2/1 17:33
 */
public class InterceptorInstanceLoader {
    private static ConcurrentHashMap<String, Object> INSTANCE_CACHE = new ConcurrentHashMap<String, Object>();
    private static ReentrantLock INSTANCE_LOAD_LOCK = new ReentrantLock();

    /**
     * @param className
     * @param <T>
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    public static <T> T load(String className) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        ClassLoader targetClassLoader = Thread.currentThread().getContextClassLoader();
        String instanceKey = className + "_OF_" + targetClassLoader.getClass()
                .getName() + "@" + Integer.toHexString(targetClassLoader
                .hashCode());
        Object inst = INSTANCE_CACHE.get(instanceKey);
        if (inst == null) {
            INSTANCE_LOAD_LOCK.lock();
            try {
                inst = Class.forName(className, true, targetClassLoader).newInstance();
            } finally {
                INSTANCE_LOAD_LOCK.unlock();
            }
            if (inst != null) {
                INSTANCE_CACHE.put(instanceKey, inst);
            }
        }

        return (T) inst;
    }
}
