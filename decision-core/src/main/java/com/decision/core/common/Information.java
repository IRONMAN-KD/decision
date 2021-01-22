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
public @interface Information {

    /**
     * 未知版本
     */
    String UNKNOW_VERSION = "UNKNOW_VERSION";

    /**
     * 未知作者
     */
    String UNKNOW_AUTHOR = "UNKNOW_AUTHOR";

    /**
     * 插件ID
     * 全JVM唯一标记了一个插件，所有基于插件的操作都将基于此ID来完成
     *
     * @return 插件ID
     */
    String id();


    /**
     * 插件版本号
     *
     * @return 插件版本号
     */
    String version() default UNKNOW_VERSION;

    /**
     * 插件作者
     *
     * @return 插件作者
     */
    String author() default UNKNOW_AUTHOR;


}
