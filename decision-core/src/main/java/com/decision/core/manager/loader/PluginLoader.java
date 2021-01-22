package com.decision.core.manager.loader;

import com.decision.core.classloader.PluginJarClassLoader;
import com.decision.core.common.DecisionPlugin;
import com.decision.core.common.Information;
import org.apache.commons.lang3.ArrayUtils;
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
    // 已加载的插件集合
    private final Map<String, CoreModule> loadedModuleBOMap = new ConcurrentHashMap<String, CoreModule>();

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

    public void load(){
        try{
            for (File pluginJarFile : listPluginJarFileInLib()) {
                boolean hasModuleLoadedSuccessFlag = false;
                PluginJarClassLoader pluginJarClassLoader = null;
                logger.info("prepare loading plugin-jar={};", pluginJarFile);
                try {
                    pluginJarClassLoader = new PluginJarClassLoader(pluginJarFile);

                    final ClassLoader preTCL = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(pluginJarClassLoader);

                    try {
                        hasModuleLoadedSuccessFlag = loadingPlugins(pluginJarClassLoader,pluginJarFile);
                    } finally {
                        Thread.currentThread().setContextClassLoader(preTCL);
                    }

                } finally {
                    if (!hasModuleLoadedSuccessFlag
                            && null != pluginJarClassLoader) {
                        logger.warn("loading module-jar completed, but NONE module loaded, will be close ModuleJarClassLoader. module-jar={};", moduleJarFile);
                        pluginJarClassLoader.close();
                    }
                }
            }
        }catch (Throwable t){
            logger.error("loading plugin-jar  error! plugin-jar={};", pluginLibDir, t);
        }

    }
    private boolean loadingPlugins(final PluginJarClassLoader pluginClassLoader,final File pluginJarFile) {

        final Set<String> loadedModuleUniqueIds = new LinkedHashSet<String>();
        final ServiceLoader<DecisionPlugin> moduleServiceLoader = ServiceLoader.load(DecisionPlugin.class, pluginClassLoader);
        final Iterator<DecisionPlugin> pluginIt = moduleServiceLoader.iterator();
        while (pluginIt.hasNext()) {

            final DecisionPlugin plugin;
            try {
                plugin = pluginIt.next();
            } catch (Throwable cause) {
                logger.warn("loading decisionPlugin instance failed: instance occur error, will be ignored. plugin-jar={}", pluginJarFile, cause);
                continue;
            }

            final Class<?> classOfModule = plugin.getClass();

            // 判断插件是否实现了@Information标记
            if (!classOfModule.isAnnotationPresent(Information.class)) {
                logger.warn("loading decisionPlugin instance failed: not implements @Information, will be ignored. class={};plugin-jar={};",
                        classOfModule,
                        pluginJarFile
                );
                continue;
            }

            final Information info = classOfModule.getAnnotation(Information.class);
            final String uniqueId = info.id();

            // 判断插件ID是否合法
            if (StringUtils.isBlank(uniqueId)) {
                logger.warn("loading decisionPlugin instance failed: @Information.id is missing, will be ignored. class={};plugin-jar={};",
                        classOfModule,
                        pluginJarFile
                );
                continue;
            }


            try {
                onLoad(uniqueId, classOfModule, plugin, pluginJarFile, pluginClassLoader);
            } catch (Throwable cause) {
                logger.warn("loading plugin instance failed: will be ignored. plugin={};class={};plugin-jar={};",
                        uniqueId,
                        classOfModule,
                        pluginJarFile,
                        cause
                );
                continue;
            }

            loadedModuleUniqueIds.add(uniqueId);

        }


        logger.info("loaded plugin-jar completed, loaded {} module in plugin-jar={}, modules={}",
                loadedModuleUniqueIds.size(),
                pluginJarFile,
                loadedModuleUniqueIds
        );
        return !loadedModuleUniqueIds.isEmpty();
    }

    public void onLoad(final String uniqueId,
                       final Class pluginClass,
                       final DecisionPlugin plugin,
                       final File pluginJarFile,
                       final PluginJarClassLoader pluginClassLoader) throws Throwable {

        // 如果之前已经加载过了相同ID的插件，则放弃当前插件的加载
        if (loadedModuleBOMap.containsKey(uniqueId)) {
            final CoreModule existedCoreModule = get(uniqueId);
            logger.info("IMLCB: module already loaded, ignore load this module. expected:module={};class={};loader={}|existed:class={};loader={};",
                    uniqueId,
                    moduleClass, moduleClassLoader,
                    existedCoreModule.getModule().getClass().getName(),
                    existedCoreModule.getLoader()
            );
            return;
        }

        // 需要经过ModuleLoadingChain的过滤
        providerManager.loading(
                uniqueId,
                moduleClass,
                module,
                moduleJarFile,
                moduleClassLoader
        );

        // 之前没有加载过，这里进行加载
        logger.info("IMLCB: found new module, prepare to load. module={};class={};loader={};",
                uniqueId,
                moduleClass,
                moduleClassLoader
        );

        // 这里进行真正的插件加载
        load(uniqueId, module, moduleJarFile, moduleClassLoader);
    }
}
