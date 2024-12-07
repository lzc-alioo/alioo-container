package com.lzc.alioo.container;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class PluginClassLoader extends URLClassLoader {
    private AliooClassLoader aliooClassLoader;
    private String pluginDir = null;
    private List<String> importList = new ArrayList<>();

    private PluginClassLoader(String pluginName, String pluginDir, URL[] urlPath, AliooClassLoader aliooClassLoader) {
        super(pluginName, urlPath, ClassLoader.getSystemClassLoader());
        this.aliooClassLoader = aliooClassLoader;
        this.pluginDir = pluginDir;
        //读取META-INF/import.index 到importList
        Path importPath = Path.of(pluginDir, "META-INF/import.index");
        if (Files.exists(importPath)) {
            try {
                importList = Files.readAllLines(importPath);
            } catch (IOException e) {
                log.error("read import.index from " + pluginDir + " error", e);
            }
        }

    }

    public static PluginClassLoader init(String pluginName, File pluginFile, AliooClassLoader aliooClassLoader) {
        URL[] urlPath = new URL[0];
        String pluginDir = null;
        try {
            pluginDir = JarExtractor.extractJar(pluginFile.getAbsolutePath());
            urlPath = new URL[]{pluginFile.toURI().toURL()};
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        PluginClassLoader instance = new PluginClassLoader(pluginName, pluginDir, urlPath, aliooClassLoader);
        System.out.println("found alioo plugin by pluginPath:" + pluginFile.getAbsolutePath());
        return instance;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class clazz = null;
        //对于import的类优先使用lzcClassLoader进行统一加载
        String packagePath = name.substring(0, name.lastIndexOf('.'));
        if (importList.contains(packagePath) || importList.contains(name)) {
            clazz = aliooClassLoader.loadClass(name);
            if (clazz != null) {
                return clazz;
            } else {
                throw new ClassNotFoundException(name + " not in " + aliooClassLoader.getName());
            }
        }

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


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");

        //find class from classes
        {
            Path classPath = Paths.get(pluginDir, "classes", name.replace('.', '/') + ".class");
            try {
                if (Files.exists(classPath)) {
                    byte[] classData = Files.readAllBytes(classPath);
                    Class<?> cls = defineClass(name, classData, 0, classData.length);
//                    cache.put(name, cls);
                    return cls;
                }
            } catch (IOException e) {
                throw new ClassNotFoundException("Class " + name + " not found by " + this.getName());
            }


            //find class from lib
            {
                File libDir = new File(pluginDir, "lib");
                if (libDir.exists()) {
                    for (File file : libDir.listFiles(f -> f.getName().endsWith(".jar"))) {
                        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {
                            ZipEntry entry;
                            while ((entry = zis.getNextEntry()) != null) {
                                if (entry.getName().equals(path)) {
                                    byte[] classData = zis.readAllBytes();
                                    Class<?> cls = defineClass(name, classData, 0, classData.length);
//                                   cache.put(name, cls);
                                    return cls;
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        }
        throw new ClassNotFoundException("Class " + name + " not found by " + this.getName());
    }


    public List<String> getExportedClass() {
        List<String> list = new ArrayList<>();
        String startPathStr = pluginDir + "/classes";
        Path startPath = Paths.get(startPathStr);

        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        list.add(file.toString().substring(startPathStr.length() + 1));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}

