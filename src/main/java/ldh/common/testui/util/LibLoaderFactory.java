package ldh.common.testui.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.*;

/**
 * Created by ldh on 2019/1/17.
 */
public class LibLoaderFactory {

    private Map<String, URLClassLoader> loaderPathMap = new HashMap();

    private static LibLoaderFactory instance = null;

    public static LibLoaderFactory getInstance() {
        if (instance == null) {
            synchronized (LibLoaderFactory.class) {
                if(instance == null) {
                    instance = new LibLoaderFactory();
                }
            }
        }
        return instance;
    }

    public void loadLib(String path) throws Exception {
        if (loaderPathMap.containsKey(path)) return;
        File file = new File(path);
        File[] jarFiles = file.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar") || name.endsWith(".zip");
            }
        });
        URL[] urls = new URL[1];

        try {
            urls[0] = new URL("file:" + path);
//            URLClassLoader loader = new URLClassLoader(urls);
            Method method = null;
            try {
                URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                boolean accessible = method.isAccessible();
                if (accessible == false) {
                    method.setAccessible(true);
                }
                // 将当前类路径加入到类加载器中
                method.invoke(loader, urls[0]);

                for (File f : jarFiles) {
                    URL url = f.toURI().toURL();
                    method.invoke(loader, url);
                }
            } finally {
                method.setAccessible(false);
            }
//            loaderPathMap.put(path, loader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Class loadClass(String path, String className) throws Exception {
        if (loaderPathMap.containsKey(path)) {
            URLClassLoader loader = loaderPathMap.get(path);
            return loader.loadClass(className);
        }
        loadLib(path);
//        URLClassLoader loader = loaderPathMap.get(path);
//        return loader.loadClass(className);
        return MethodUtil.forClass(className);
    }

    public void close() {
        loaderPathMap.values().stream().forEach(loader -> {
            try {
                if (loader != null) loader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        loaderPathMap.clear();
    }
}
