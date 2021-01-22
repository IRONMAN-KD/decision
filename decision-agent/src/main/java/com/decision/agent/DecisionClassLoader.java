package com.decision.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * 加载Decision的ClassLoader
 *
 * @Author KD
 * @Date 2021/1/20 16:00
 */
class DecisionClassLoader extends URLClassLoader {

    private final String toString;
    private final String path;

    DecisionClassLoader(final String decisionCoreJarFilePath) throws MalformedURLException {
        super(new URL[]{new URL("file:" + decisionCoreJarFilePath)});
        this.path = decisionCoreJarFilePath;
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

    @Override
    public String toString() {
        return toString;
    }


}
