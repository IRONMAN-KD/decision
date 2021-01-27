package com.decision.core.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 插件信息
 *
 * @Author KD
 * @Date 2021/1/22 10:56
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DecisionPlugin {

    /**
     * 未知版本
     */
    String DEFAULT_VERSION = "DEFAULT_VERSION";

    /**
     * 未知作者
     */
    String DEFAULT_AUTHOR = "DEFAULT_AUTHOR";

    /**
     * 插件ID
     *
     * @return 插件ID
     */
    String id();


    /**
     * 插件版本号
     *
     * @return 插件版本号
     */
    String version() default DEFAULT_VERSION;

    /**
     * 插件作者
     *
     * @return 插件作者
     */
    String author() default DEFAULT_AUTHOR;


}
