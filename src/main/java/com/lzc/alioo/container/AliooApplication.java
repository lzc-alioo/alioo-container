package com.lzc.alioo.container;

import java.lang.reflect.Field;


public class AliooApplication {
    private static boolean inited;

    public static void run(String[] args) {
        try {
            if (inited) {
                return;
            }
            //加载业务类加载器
            AliooClassLoader aliooClassLoader = AliooClassLoader.init(AliooApplication.class.getClassLoader());

            //插件集合类加载器
            PluginSharableClassLoader sharableClassLoader = PluginSharableClassLoader.init(aliooClassLoader);

            aliooClassLoader.setSharableClassLoader(sharableClassLoader);

//            AliooClassLoader aliooClassLoader = AliooClassLoader.init(AliooApplication.class.getClassLoader(), sharableClassLoader);


            reLaunch(aliooClassLoader);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void reLaunch(AliooClassLoader aliooClassLoader) throws Exception {


        String mainClassName = fetchMainClassName();

        Thread launchThread = new Thread(() -> {
            try {
                // 这里不能简单如此赋值，否则会出现死循环
                // inited = true;
                markField(aliooClassLoader, "inited", true);

                Class mainClass = aliooClassLoader.loadClass(mainClassName);
                mainClass.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
            } catch (Exception e) {
                System.out.println("reLaunch err:" + e.getMessage());
                e.printStackTrace();
            }

        }, "aliooMainThread");
        launchThread.setContextClassLoader(aliooClassLoader);
        launchThread.start();
        launchThread.join();

        // 执行到这里，新启动的main线程已经退出了，可以直接退出进程
        // 如果没有下面这行代码，Application类中业务代码会再执行一次（BizTest3.main(args);）
        System.exit(0);
    }


    public static void markField(ClassLoader classLoader, String fieldName, Object value) {
        try {
            Class<?> sarLauncherClass = classLoader.loadClass(AliooApplication.class.getName());
            Field declaredField = sarLauncherClass.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(null, value);
        } catch (Throwable t) {
            // ignore
        }
    }


    public static String fetchMainClassName() {
        for (StackTraceElement stackTraceElement : new RuntimeException().getStackTrace()) {
            if (stackTraceElement.getMethodName().equals("main")) {
                return stackTraceElement.getClassName();
            }
        }
        throw new RuntimeException("main class not found");
    }


}
