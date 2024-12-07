package com.lzc.alioo.container;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;


public class PluginSharableClassLoader extends URLClassLoader {
    private static final Map<String, PluginClassLoader> pluginNameMap = new HashMap<>();
    private static final Map<String, PluginClassLoader> classNamePluginCache = new HashMap<>();


    public static PluginSharableClassLoader init(AliooClassLoader aliooClassLoader) {
        //插件集合类加载器
        PluginSharableClassLoader sharableClassLoader = new PluginSharableClassLoader();

        // 加载插件类加载器
        sharableClassLoader.initPluginClassLoader(aliooClassLoader);

        return sharableClassLoader;
    }

    public PluginSharableClassLoader() {
        super(new URL[0], ClassLoader.getSystemClassLoader().getParent());
    }


    public void initPluginClassLoader(AliooClassLoader aliooClassLoader) {
        String pluginPath = System.getProperty("alioo.plugin.path", System.getProperty("user.home") + File.separator + "alioo-plugin/");

        File[] pluginFiles = new File(pluginPath).listFiles();
        if (pluginFiles == null) {
            System.out.println("not found alioo plugin by pluginPath:" + pluginPath);
            return;
        }
        for (int i = 0; i < pluginFiles.length; i++) {
            if (pluginFiles[i].getName().endsWith(".jar")) {
                String pluginName = pluginFiles[i].getName().replace(".jar", "");
                if (this.contains(pluginName)) {
                    continue;
                }
                PluginClassLoader pluginClassLoader = PluginClassLoader.init(pluginName, pluginFiles[i], aliooClassLoader);
                register(pluginClassLoader);
            }
        }

    }

    public void register(PluginClassLoader pluginClassLoader) {
        pluginNameMap.put(pluginClassLoader.getName(), pluginClassLoader);
        //pluginClassLoader提取出所有url中jar中class文件
        pluginClassLoader.getExportedClass().stream().forEach(className -> classNamePluginCache.put(className, pluginClassLoader));
    }

    public boolean contains(String pluginName) {

        return pluginNameMap.containsKey(pluginName);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class clazz = null;
        //预判一下当前加载的类是否在某个插件类加载器中

        PluginClassLoader pluginClassLoader = classNamePluginCache.get(name.replace('.', '/') + ".class");
        if (pluginClassLoader != null) {
            clazz = pluginClassLoader.loadClassData(name);
            return clazz;
        }
        return null;


    }
}
