package com.decision.core.classloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 加载Decision的ClassLoader
 *
 * @Author KD
 * @Date 2021/1/20 16:00
 */
public class DecisionClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(DecisionClassLoader.class);
    private final String toString;
    private final String path;
    private List<File> jarFiles;

    public DecisionClassLoader(String decisionJarPath) throws MalformedURLException {
        super(new URL[]{new URL("file:" + decisionJarPath)});
        this.path = decisionJarPath;
        this.toString = String.format("DecisionClassLoader[path=%s;]", path);
    }


    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (null != url) {
            return url;
        }
        url = super.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> urls = findResources(name);
        if (null != urls) {
            return urls;
        }
        urls = super.getResources(name);
        return urls;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        try {
            Class<?> aClass = findClass(name);
            if (resolve) {
                resolveClass(aClass);
            }
            return aClass;
        } catch (Exception e) {
            return super.loadClass(name, resolve);
        }
    }


    public void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public String toString() {
        return toString;
    }


}
