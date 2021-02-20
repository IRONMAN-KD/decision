package com.decision.core.classloader;

/**
 * @author KD
 * @since 2020/1/15
 */
public class BusinessClassLoaderHolder {

    private static final ThreadLocal<DelegateBizClassLoader> HOLDER = new ThreadLocal<DelegateBizClassLoader>();

    public static void setBussinessClassLoader(ClassLoader classLoader) {
        if (null == classLoader) {
            return;
        }
        DelegateBizClassLoader delegateBizClassLoader = new DelegateBizClassLoader(classLoader);
        HOLDER.set(delegateBizClassLoader);
    }


    public static void removeBussinessClassLoader() {
        HOLDER.remove();
    }

    public static DelegateBizClassLoader getBussinessClassLoader() {

        return HOLDER.get();
    }

    public static class DelegateBizClassLoader extends ClassLoader {
        public DelegateBizClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> loadClass(final String javaClassName, final boolean resolve) throws ClassNotFoundException {
            return super.loadClass(javaClassName, resolve);
        }
    }
}
