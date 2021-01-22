package com.decision.core;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置类
 *
 * @Author KD
 * @Date 2021/1/21 10:56
 */
public class CoreConfigure {
    private Properties properties;
    private static volatile CoreConfigure instance;

    private CoreConfigure(final String propertiesFilePath) {
        properties = getProperties(propertiesFilePath);
    }

    public static CoreConfigure getInstance(final String propertiesFilePath) {
        return instance = new CoreConfigure(propertiesFilePath);
    }

    private Properties getProperties(String propertiesFilePath) {
        final Properties CfgProperties = new Properties();

        if (null == propertiesFilePath) {
            return CfgProperties;
        }

        final File propertiesFile = new File(propertiesFilePath);
        if (!propertiesFile.exists()
                || !propertiesFile.canRead()) {
            return CfgProperties;
        }


        // 从指定配置文件路径中获取配置信息
        final Properties properties = new Properties();
        InputStream is = null;
        try {
            is = FileUtils.openInputStream(propertiesFile);
            properties.load(is);
        } catch (Throwable cause) {
            // cause.printStackTrace(System.err);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return CfgProperties;
    }
}
