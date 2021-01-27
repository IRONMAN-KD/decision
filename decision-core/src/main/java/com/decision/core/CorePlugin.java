package com.decision.core;

import com.decision.core.classloader.PluginJarClassLoader;
import com.decision.core.common.DecisionPluginDefine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 沙箱插件内核封装对象
 *
 * @Author KD
 * @Date 2021/1/22 10:56
 */
public class CorePlugin {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 全局唯一编号
     */
    private final String uniqueId;

    /**
     * 插件归属Jar文件
     */
    private final File jarFile;

    /**
     * 插件加载的ClassLoader
     */
    private final PluginJarClassLoader loader;

    /**
     * 插件
     */
    private final DecisionPluginDefine plugin;


    /**
     * 是否已被加载
     */
    private boolean isLoaded;

    /**
     * 插件业务对象
     *
     * @param uniqueId 插件ID
     * @param jarFile  插件归属Jar文件
     * @param loader   插件加载ClassLoader
     * @param plugin   插件
     */
    public CorePlugin(final String uniqueId,
                      final File jarFile,
                      final PluginJarClassLoader loader,
                      final DecisionPluginDefine plugin) {
        this.uniqueId = uniqueId;
        this.jarFile = jarFile;
        this.loader = loader;
        this.plugin = plugin;
    }


    /**
     * 判断插件是否已经被加载
     *
     * @return TRUE:被加载;FALSE:未被加载
     */
    public boolean isLoaded() {
        return isLoaded;
    }


    /**
     * 标记插件加载状态
     *
     * @param isLoaded 插件加载状态
     * @return this
     */
    public CorePlugin markLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
        return this;
    }

    /**
     * 获取ModuleJar文件
     *
     * @return ModuleJar文件
     */
    public File getJarFile() {
        return jarFile;
    }

    /**
     * 获取对应的ModuleJarClassLoader
     *
     * @return ModuleJarClassLoader
     */
    public PluginJarClassLoader getLoader() {
        return loader;
    }

    /**
     * 获取插件ID
     *
     * @return 插件ID
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * 获取插件实例
     *
     * @return 插件实例
     */
    public DecisionPluginDefine getPlugin() {
        return plugin;
    }


    @Override
    public String toString() {
        return String.format(
                "plugin[id=%s;class=%s;]",
                uniqueId,
                plugin.getClass()
        );
    }


}
