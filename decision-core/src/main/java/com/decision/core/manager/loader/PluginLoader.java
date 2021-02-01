package com.decision.core.manager.loader;

import com.decision.core.classloader.DecisionClassLoader;
import com.decision.core.plugin.AbstractDecisionPluginDefine;
import com.decision.core.plugin.DecisionPlugin;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.io.FileUtils.convertFileCollectionToFileArray;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * 插件加载
 *
 * @Author KD
 * @Date 2021/1/22 10:20
 */
public class PluginLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final File pluginLibDir;
    /**
     * 已加载的插件集合
     */
    private final Map<String, AbstractDecisionPluginDefine> loadedPluginBOMap = new ConcurrentHashMap<String, AbstractDecisionPluginDefine>();
    private final List<AbstractDecisionPluginDefine> loadedPluginList = new ArrayList<AbstractDecisionPluginDefine>();

    public PluginLoader(File pluginLibDir) {
        this.pluginLibDir = pluginLibDir;
    }

    private File[] toPluginJarFileArray() {
        if (pluginLibDir.exists()
                && pluginLibDir.isFile()
                && pluginLibDir.canRead()
                && StringUtils.endsWith(pluginLibDir.getName(), ".jar")) {
            return new File[]{
                    pluginLibDir
            };
        } else {
            return convertFileCollectionToFileArray(
                    listFiles(pluginLibDir, new String[]{"jar"}, false)
            );
        }
    }


    private File[] listPluginJarFileInLib() {
        final File[] pluginJarFileArray = toPluginJarFileArray();
        Arrays.sort(pluginJarFileArray);
        logger.info("加载插件 plugin-lib={}, 找到插件 {} plugin-jar files : {}",
                pluginLibDir,
                pluginJarFileArray.length,
                join(pluginJarFileArray, ",")
        );
        return pluginJarFileArray;
    }

    public List<AbstractDecisionPluginDefine> load() {
        try {
            for (File pluginJarFile : listPluginJarFileInLib()) {
                boolean hasPluginLoadedSuccessFlag = false;
                DecisionClassLoader pluginJarClassLoader = null;
                logger.info("准备加载插件 plugin-jar={};", pluginJarFile);
                try {
                    pluginJarClassLoader = new DecisionClassLoader(pluginJarFile.getPath());

                    final ClassLoader preTCL = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(pluginJarClassLoader);

                    try {
                        hasPluginLoadedSuccessFlag = loadingPlugins(pluginJarClassLoader, pluginJarFile);
                    } finally {
                        Thread.currentThread().setContextClassLoader(preTCL);
                    }

                } finally {
                    if (!hasPluginLoadedSuccessFlag
                            && null != pluginJarClassLoader) {
                        logger.warn("加载已完成，但没有加载到插件，plugin-jar={};", pluginJarFile);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("加载插件失败! plugin-jar={};", pluginLibDir, t);
        }
        for (Map.Entry<String, AbstractDecisionPluginDefine> entry : loadedPluginBOMap.entrySet()) {
            loadedPluginList.add(entry.getValue());
        }
        return loadedPluginList;
    }

    private boolean loadingPlugins(final DecisionClassLoader pluginClassLoader, final File pluginJarFile) {

        final Set<String> loadedPluginUniqueIds = new LinkedHashSet<String>();
        final ServiceLoader<AbstractDecisionPluginDefine> pluginServiceLoader = ServiceLoader.load(AbstractDecisionPluginDefine.class, pluginClassLoader);
        final Iterator<AbstractDecisionPluginDefine> pluginIt = pluginServiceLoader.iterator();
        while (pluginIt.hasNext()) {

            final AbstractDecisionPluginDefine plugin;
            try {
                plugin = pluginIt.next();
            } catch (Throwable cause) {
                logger.warn("加载插件实例出错，将跳过该实例, plugin-jar={}", pluginJarFile, cause);
                continue;
            }

            final Class<?> classOfPlugin = plugin.getClass();

            if (!classOfPlugin.isAnnotationPresent(DecisionPlugin.class)) {
                logger.warn("加载插件实例出错: 没有使用@DecisionPlugin注解, 跳过该实例. class={};plugin-jar={};",
                        classOfPlugin,
                        pluginJarFile
                );
                continue;
            }

            final DecisionPlugin info = classOfPlugin.getAnnotation(DecisionPlugin.class);
            final String uniqueId = info.id();

            // 判断插件ID是否合法
            if (StringUtils.isBlank(uniqueId)) {
                logger.warn("加载插件实例出错: @DecisionPlugin.id 未配置, 跳过该实例. class={};plugin-jar={};",
                        classOfPlugin,
                        pluginJarFile
                );
                continue;
            }


            try {
                onLoad(uniqueId, plugin, pluginJarFile, pluginClassLoader);
            } catch (Throwable cause) {
                logger.warn("加载插件实例出错: 跳过该实例. plugin={};class={};plugin-jar={};",
                        uniqueId,
                        classOfPlugin,
                        pluginJarFile,
                        cause
                );
                continue;
            }

            loadedPluginUniqueIds.add(uniqueId);

        }

        logger.info("插件加载完成, loaded {} plugin in plugin-jar={}, plugins={}",
                loadedPluginUniqueIds.size(),
                pluginJarFile,
                loadedPluginUniqueIds
        );
        return !loadedPluginUniqueIds.isEmpty();
    }

    public void onLoad(final String uniqueId,
                       final AbstractDecisionPluginDefine plugin,
                       final File pluginJarFile,
                       final DecisionClassLoader pluginClassLoader) throws Throwable {

        // 如果之前已经加载过了相同ID的插件，则放弃当前插件的加载
        if (loadedPluginBOMap.containsKey(uniqueId)) {
            logger.debug("插件已加载过，放弃当前加载. plugin={};", uniqueId);
            return;
        }

        logger.info("plugin module, plugin={};class={};plugin-jar={};",
                uniqueId,
                plugin.getClass().getName(),
                pluginJarFile
        );
        plugin.setUniqueId(uniqueId);
        // 注册到插件列表中
        loadedPluginBOMap.put(uniqueId, plugin);

    }
}
