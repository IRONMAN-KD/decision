package com.decision.core;

import com.decision.core.common.DecisionPluginDefine;
import com.decision.core.manager.loader.PluginLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @Author KD
 * @Date 2021/1/21 11:44
 */
public class CoreLauncher {
    private final Logger logger = LoggerFactory.getLogger(CoreLauncher.class);
    private static CoreLauncher instance = new CoreLauncher();

    private CoreLauncher() {

    }


    public CoreLauncher getInstance() {
        return instance;
    }

    public List<DecisionPluginDefine> loadPlugins(String pluginJarPath) {
        final List<DecisionPluginDefine> loadedPluginDefines = new ArrayList<DecisionPluginDefine>();
        File[] pluginLibFiles = getPluginLibFiles(pluginJarPath);
        for (final File moduleLibDir : pluginLibFiles) {
            // 对模块访问权限进行校验
            if (moduleLibDir.exists() && moduleLibDir.canRead()) {
                Map<String, CorePlugin> loadedPlugins = new PluginLoader(moduleLibDir)
                        .load();
                if (loadedPlugins.size() >= 0) {
                    for (Map.Entry<String, CorePlugin> stringCorePluginEntry : loadedPlugins.entrySet()) {
                        loadedPluginDefines.add(stringCorePluginEntry.getValue().getPlugin());
                    }
                }
            } else {
                logger.warn("plugin-lib not access, ignore flush load this lib. path={}", moduleLibDir);
            }
        }

        return loadedPluginDefines;
    }

    private File[] getPluginLibFiles(String pluginJarPath) {
        final Collection<File> foundModuleJarFiles = new LinkedHashSet<File>();
        final File fileOfPath = new File(pluginJarPath);
        if (fileOfPath.isDirectory()) {
            foundModuleJarFiles.addAll(FileUtils.listFiles(new File(pluginJarPath), new String[]{"jar"}, false));
        } else {
            if (StringUtils.endsWithIgnoreCase(fileOfPath.getPath(), ".jar")) {
                foundModuleJarFiles.add(fileOfPath);
            }
        }
        return foundModuleJarFiles.toArray(new File[]{});
    }
}
