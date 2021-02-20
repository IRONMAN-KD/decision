package com.decision.core.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.decision.core.plugin.common.StringUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


/**
 * Logback日志框架工具类
 *
 * @author KD
 */
public class LogbackUtils {


    /**
     * 初始化Logback日志框架
     */
    public static void init(final String decisionHome) {
        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final JoranConfigurator configurator = new JoranConfigurator();
        final File configureFile = new File(getDecisionLogCfgPath(decisionHome));
        configurator.setContext(loggerContext);
        loggerContext.reset();
        InputStream is = null;
        final Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
        try {
            is = new FileInputStream(configureFile);
            configurator.doConfigure(is);
            logger.info(StringUtil.getLogo());
            logger.info("initializing decision logback success. file={};", configureFile);
        } catch (Throwable cause) {
            logger.warn("initialize decision logback failed. file={};", configureFile, cause);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static Logger getLogger(Class clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * 销毁Logback日志框架
     */
    public static void destroy() {
        try {
            ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
        } catch (Throwable cause) {
            cause.printStackTrace();
        }
    }


    private static String getDecisionLogCfgPath(String decisionHome) {
        return decisionHome + File.separatorChar + "cfg" + File.separatorChar + "decision-logback.xml";
    }

    private static String getDecisionLogHomePath(String decisionHome) {
        return decisionHome + File.separatorChar + "logs";
    }

}
