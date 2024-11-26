package com.lzc.alioo.container;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class PluginClassLoader extends URLClassLoader {

    private PluginClassLoader(String module, URL[] urlPath) {
        super(module, urlPath, ClassLoader.getSystemClassLoader().getParent());

        PluginSharableClassLoader.register(this);
    }


    public static PluginClassLoader init(String module, File pluginFile) {
        URL[] urlPath = new URL[0];
        try {
            urlPath = new URL[]{pluginFile.toURI().toURL()};
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        PluginClassLoader instance = new PluginClassLoader(module, urlPath);
        System.out.println("found alioo plugin by pluginPath:" + pluginFile.getAbsolutePath());
        return instance;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class clazz = null;
        //对于import的类优先使用lzcClassLoader进行统一加载

        clazz = loadClassData(name);
        if (clazz != null) {
            log.info("Loaded By " + this.getName() + "(" + this + ") name: " + name);
        }

        return clazz;

    }

    public Class<?> loadClassData(String className) {
        Class clazz = findLoadedClass(className);
        if (clazz != null) {
            return clazz;
        }
        try {
            return super.loadClass(className);
        } catch (ClassNotFoundException e) {
        }


        return null;
    }


    public List<String> getExportedClass() {
        //todo:实际情况可能是只需要导出部分类文件
        File classFile = new File(this.getURLs()[0].getFile());

        List<String> list = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(classFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    list.add(entry.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

}

