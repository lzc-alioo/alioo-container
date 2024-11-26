package com.lzc.alioo.container;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliooClassLoader extends URLClassLoader {

    private static AliooClassLoader instance;

    private static PluginSharableClassLoader sharableClassLoader;

    private static boolean inited;

    private Map<String, Class> cache;

    private AliooClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.cache = new HashMap<>();

    }


    public static AliooClassLoader init(ClassLoader classLoader, PluginSharableClassLoader pluginSharableClassLoader) {
        if (inited) {
            return instance;
        }

        URL[] urls = ClassLoaderUtils.getUrls(classLoader);
        instance = new AliooClassLoader(urls, ClassLoader.getSystemClassLoader().getParent());
        sharableClassLoader = pluginSharableClassLoader;
        inited = true;
        return instance;

    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        //优先检查是否已经在缓存中
        Class clazz = null;
        if ((clazz = cache.get(name)) != null) {
            return clazz;
        }

        //插件中查找
        if ((clazz = sharableClassLoader.loadClass(name)) != null) {
            log.info("Loaded By " + sharableClassLoader + " name: " + name);
            return clazz;
        }


        //项目classpath中查找
        if ((clazz = super.loadClass(name)) != null) {
            log.info("Loaded By " + this + " name: " + name);
            cache.put(name, clazz);
            return clazz;
        }

        throw new ClassNotFoundException(name);

    }

}

