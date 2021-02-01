package com.decision.core;

import com.decision.core.manager.loader.PluginLoader;
import com.decision.core.plugin.AbstractDecisionPluginDefine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @Author KD
 * @Date 2021/1/21 11:44
 */
public class CoreLauncher {
    private final Logger logger = LoggerFactory.getLogger(CoreLauncher.class);


    public static CoreLauncher getInstance() {
        return new CoreLauncher();
    }

    public List<AbstractDecisionPluginDefine> loadPlugins(String pluginJarPath) {
        List<AbstractDecisionPluginDefine> loadedPluginDefines = new ArrayList<AbstractDecisionPluginDefine>();
        File[] pluginLibFiles = getPluginLibFiles(pluginJarPath);
        for (final File moduleLibDir : pluginLibFiles) {
            // 对模块访问权限进行校验
            if (moduleLibDir.exists() && moduleLibDir.canRead()) {
                loadedPluginDefines = new PluginLoader(moduleLibDir)
                        .load();
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
