package com.lzc.alioo.container;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarExtractor {

    public static void main(String[] args) {
        // test case
        String jarFilePath = "/Users/mac/alioo-plugin/alioo-container-plugin-x1-1.0-SNAPSHOT-fat.jar"; // 替换为你的JAR文件路径
        try {
            extractJar(jarFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String extractJar(String jarFilePath) throws IOException {
        String destDir = jarFilePath.substring(0, jarFilePath.lastIndexOf("."));
        extractJar(jarFilePath, destDir);
        return destDir;
    }

    public static void extractJar(String jarFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        if (dir.exists()) {
            dir.delete();
        }
        dir.mkdirs();

        try (JarInputStream jarIn = new JarInputStream(new FileInputStream(jarFilePath))) {
            JarEntry entry;

            while ((entry = jarIn.getNextJarEntry()) != null) {
                File file = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream out = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = jarIn.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }

                jarIn.closeEntry();
            }
        }
    }
}
