package com.decision.core.plugin;

import com.decision.core.classloader.PluginJarClassLoader;
import com.decision.core.manager.loader.InterceptorInstanceLoader;
import org.apache.commons.io.FileUtils;
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
 * @Author KD
 * @Date 2021/1/21 11:44
 */
public class PluginLoader {
    private final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    /**
     * 已加载的插件集合
     */
    private final Map<String, DecisionPluginDefine> loadedPluginBOMap = new ConcurrentHashMap<String, DecisionPluginDefine>();
    private final List<DecisionPluginDefine> loadedPluginList = new ArrayList<DecisionPluginDefine>();

    public List<DecisionPluginDefine> loadPlugins(String decisionHome) {
        String decisionPluginJarPath = getDecisionPluginJarPath(decisionHome);
        List<DecisionPluginDefine> loadedPluginDefines = new ArrayList<DecisionPluginDefine>();
        File[] pluginLibFiles = getPluginLibFiles(decisionPluginJarPath);
        for (final File pluginLibDir : pluginLibFiles) {
            // 对插件访问权限进行校验
            if (pluginLibDir.exists() && pluginLibDir.canRead()) {
                loadedPluginDefines = load(pluginLibDir);
            } else {
                logger.warn("plugin-lib not access, ignore flush load this lib. path={}", pluginLibDir);
            }
        }

        return loadedPluginDefines;
    }

    private File[] getPluginLibFiles(String pluginJarPath) {
        final Collection<File> foundPluginJarFiles = new LinkedHashSet<File>();
        final File fileOfPath = new File(pluginJarPath);
        if (fileOfPath.isDirectory()) {
            foundPluginJarFiles.addAll(FileUtils.listFiles(new File(pluginJarPath), new String[]{"jar"}, false));
        } else {
            if (StringUtils.endsWithIgnoreCase(fileOfPath.getPath(), ".jar")) {
                foundPluginJarFiles.add(fileOfPath);
            }
        }
        return foundPluginJarFiles.toArray(new File[]{});
    }


    private File[] toPluginJarFileArray(File pluginLibDir) {
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


    private File[] listPluginJarFileInLib(File pluginLibDir) {
        final File[] pluginJarFileArray = toPluginJarFileArray(pluginLibDir);
        Arrays.sort(pluginJarFileArray);
        logger.debug("加载插件 plugin-lib={}, 找到插件 {} plugin-jar files : {}",
                pluginLibDir,
                pluginJarFileArray.length,
                join(pluginJarFileArray, ",")
        );
        return pluginJarFileArray;
    }

    public List<DecisionPluginDefine> load(File pluginLibDir) {
        try {
            for (File pluginJarFile : listPluginJarFileInLib(pluginLibDir)) {
                boolean hasPluginLoadedSuccessFlag = false;
                PluginJarClassLoader decisionClassLoader = null;
                logger.debug("准备加载插件 plugin-jar={};", pluginJarFile);
                try {
                    decisionClassLoader = new PluginJarClassLoader(pluginJarFile.getPath(), PluginLoader.class.getClassLoader(), new PluginJarClassLoader.Routing(PluginLoader.class.getClassLoader(), "com.decision.core.*"));

                    final ClassLoader preTcl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(decisionClassLoader);

                    try {
                        hasPluginLoadedSuccessFlag = loadingPlugins(decisionClassLoader, pluginJarFile);
                    } finally {
                        Thread.currentThread().setContextClassLoader(preTcl);
                    }

                } finally {
                    if (!hasPluginLoadedSuccessFlag
                            && null != decisionClassLoader) {
                        logger.warn("加载已完成，但没有加载到插件，plugin-jar={};", pluginJarFile);
                    }
                }
            }
        } catch (Throwable t) {
            logger.error("加载插件失败! plugin-jar={};", pluginLibDir, t);
        }
        for (Map.Entry<String, DecisionPluginDefine> entry : loadedPluginBOMap.entrySet()) {
            loadedPluginList.add(entry.getValue());
        }
        return loadedPluginList;
    }

    private boolean loadingPlugins(final ClassLoader pluginClassLoader, final File pluginJarFile) {

        final Set<String> loadedPluginUniqueIds = new LinkedHashSet<String>();
        final ServiceLoader<DecisionPluginDefine> pluginServiceLoader = ServiceLoader.load(DecisionPluginDefine.class, pluginClassLoader);
        final Iterator<DecisionPluginDefine> pluginIt = pluginServiceLoader.iterator();
        while (pluginIt.hasNext()) {

            final DecisionPluginDefine plugin;
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
                onLoad(uniqueId, plugin, pluginJarFile);
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

        logger.info("load plugin success, loaded {} plugin in plugin-jar={}, plugins={}",
                loadedPluginUniqueIds.size(),
                pluginJarFile,
                loadedPluginUniqueIds
        );
        return !loadedPluginUniqueIds.isEmpty();
    }

    public void onLoad(final String uniqueId,
                       final DecisionPluginDefine plugin,
                       final File pluginJarFile) throws Throwable {

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
        // 注册到插件列表中
        loadedPluginBOMap.put(uniqueId, plugin);

    }

    private static String getDecisionPluginJarPath(String decisionHome) {
        return decisionHome + File.separatorChar + "plugins";
    }
}
