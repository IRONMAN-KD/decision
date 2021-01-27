package com.decision.core.manager.loader;

import com.decision.core.CorePlugin;
import com.decision.core.classloader.PluginJarClassLoader;
import com.decision.core.common.DecisionPlugin;
import com.decision.core.common.DecisionPluginDefine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
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
    private final Map<String, CorePlugin> loadedPluginBOMap = new ConcurrentHashMap<String, CorePlugin>();

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
        logger.info("loading plugin-lib={}, found {} plugin-jar files : {}",
                pluginLibDir,
                pluginJarFileArray.length,
                join(pluginJarFileArray, ",")
        );
        return pluginJarFileArray;
    }

    public Map<String, CorePlugin> load() {
        try {
            for (File pluginJarFile : listPluginJarFileInLib()) {
                boolean hasModuleLoadedSuccessFlag = false;
                PluginJarClassLoader pluginJarClassLoader = null;
                logger.info("prepare loading plugin-jar={};", pluginJarFile);
                try {
                    pluginJarClassLoader = new PluginJarClassLoader(new URL[]{new URL("file:" + pluginJarFile.getPath())},
                            new PluginJarClassLoader.Routing(
                                    PluginJarClassLoader.class.getClassLoader(),
                                    "^com\\.decision\\.core\\..*",
                                    "^javax\\.servlet\\..*",
                                    "^javax\\.annotation\\.Resource.*$"
                            ));

                    final ClassLoader preTCL = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(pluginJarClassLoader);

                    try {
                        hasModuleLoadedSuccessFlag = loadingPlugins(pluginJarClassLoader, pluginJarFile);
                    } finally {
                        Thread.currentThread().setContextClassLoader(preTCL);
                    }

                } finally {
                    if (!hasModuleLoadedSuccessFlag
                            && null != pluginJarClassLoader) {
                        logger.warn("loading plugin-jar completed, but NONE module loaded, will be close ModuleJarClassLoader. module-jar={};", moduleJarFile);
                        pluginJarClassLoader.close();
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("loading plugin-jar  error! plugin-jar={};", pluginLibDir, t);
        }
        return loadedPluginBOMap;
    }

    private boolean loadingPlugins(final PluginJarClassLoader pluginClassLoader, final File pluginJarFile) {

        final Set<String> loadedPluginUniqueIds = new LinkedHashSet<String>();
        final ServiceLoader<DecisionPluginDefine> moduleServiceLoader = ServiceLoader.load(DecisionPluginDefine.class, pluginClassLoader);
        final Iterator<DecisionPluginDefine> pluginIt = moduleServiceLoader.iterator();
        while (pluginIt.hasNext()) {

            final DecisionPluginDefine plugin;
            try {
                plugin = pluginIt.next();
            } catch (Throwable cause) {
                logger.warn("loading decisionPlugin instance failed: instance occur error, will be ignored. plugin-jar={}", pluginJarFile, cause);
                continue;
            }

            final Class<?> classOfPlugin = plugin.getClass();

            if (!classOfPlugin.isAnnotationPresent(DecisionPlugin.class)) {
                logger.warn("loading decisionPlugin instance failed: not implements @DecisionPlugin, will be ignored. class={};plugin-jar={};",
                        classOfPlugin,
                        pluginJarFile
                );
                continue;
            }

            final DecisionPlugin info = classOfPlugin.getAnnotation(DecisionPlugin.class);
            final String uniqueId = info.id();

            // 判断插件ID是否合法
            if (StringUtils.isBlank(uniqueId)) {
                logger.warn("loading decisionPlugin instance failed: @DecisionPlugin.id is missing, will be ignored. class={};plugin-jar={};",
                        classOfPlugin,
                        pluginJarFile
                );
                continue;
            }


            try {
                onLoad(uniqueId, plugin, pluginJarFile, pluginClassLoader);
            } catch (Throwable cause) {
                logger.warn("loading plugin instance failed: will be ignored. plugin={};class={};plugin-jar={};",
                        uniqueId,
                        classOfPlugin,
                        pluginJarFile,
                        cause
                );
                continue;
            }

            loadedPluginUniqueIds.add(uniqueId);

        }

        logger.info("loaded plugin-jar completed, loaded {} module in plugin-jar={}, modules={}",
                loadedPluginUniqueIds.size(),
                pluginJarFile,
                loadedPluginUniqueIds
        );
        return !loadedPluginUniqueIds.isEmpty();
    }

    public void onLoad(final String uniqueId,
                       final DecisionPluginDefine plugin,
                       final File pluginJarFile,
                       final PluginJarClassLoader pluginClassLoader) throws Throwable {

        // 如果之前已经加载过了相同ID的插件，则放弃当前插件的加载
        if (loadedPluginBOMap.containsKey(uniqueId)) {
            logger.debug("plugin already loaded. module={};", uniqueId);
            return;
        }

        logger.info("plugin module, plugin={};class={};plugin-jar={};",
                uniqueId,
                plugin.getClass().getName(),
                pluginJarFile
        );
        // 初始化插件信息
        final CorePlugin corePlugin = new CorePlugin(uniqueId, pluginJarFile, pluginClassLoader, plugin);
        // 设置为已经加载
        corePlugin.markLoaded(true);
        // 注册到插件列表中
        loadedPluginBOMap.put(uniqueId, corePlugin);

    }
}
