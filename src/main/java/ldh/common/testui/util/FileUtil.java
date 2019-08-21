package ldh.common.testui.util;

import sun.applet.AppletClassLoader;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by ldh on 2017/2/26.
 */
public class FileUtil {

    public static String loadJarFile(String file) throws Exception {
        InputStream inputStream = FileUtil.class.getResourceAsStream(file);
        String data = loadFile(inputStream);
        inputStream.close();
        return data;
    }

    public static String loadFile(InputStream inputStream) throws Exception {
        int size = inputStream.available();
        byte[] data = new byte[size];
        int l = inputStream.read(data);
        return new String(data, "utf-8");
    }

    public static String getSourceRoot() {
        return System.getProperty("user.dir") + "/";
    }

//    public static void loadFileTree(String dir, TreeItem<TreeNode> root) {
//        File file = new File(dir);
//        File[] files = file.listFiles();
//        if (files == null) return;
//        for (File f : files) {
//            TreeNodeTypeEnum type = TreeNodeTypeEnum.ITEM;
//            if (f.isFile() && (f.getName().endsWith(".java") || f.getName().endsWith(".xml") ||
//                f.getName().endsWith(".jsp") || f.getName().endsWith(".css") || f.getName().endsWith(".js") ||
//                    f.getName().endsWith(".fxml") || f.getName().endsWith(".json"))) {
//                type = TreeNodeTypeEnum.JAVA_FILE;
//            }
//            TreeNode tn = new TreeNode(type, f.getName(), root.getValue());
//            tn.setData(f.getPath());
//            TreeItem<TreeNode> c = new TreeItem<>(tn);
//            root.getChildren().add(c);
//            loadFileTree(f.getPath(), c);
//        }
//        root.setExpanded(true);
//    }

    public static String loadFile(String file) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for(String line : lines){
            sb.append(line).append("\r\n");
        }
        return sb.toString();
    }

    public static void saveFile(String file, String json) throws IOException {
        Files.write(Paths.get(file), json.getBytes("utf-8"), StandardOpenOption.CREATE);
    }

    public static List<Class> searchClass(String path) throws Exception {
        List<Class> resultClass = new ArrayList<>();
//        String classpath = FileUtil.class.getResource("/").getPath();
//        String basePack = path.replace(".", File.separator);
//        String searchPath = classpath + basePack;
//        addClass(resultClass, path, searchPath);
//        if (classpath.contains("test-classes")) {
//            searchPath = searchPath.replace("test-classes", "classes");
//            addClass(resultClass, path, searchPath);
//        }
        resultClass.addAll(searchJarClass(path));
        return resultClass;
    }

    private static void addClass(List<Class> resultClass, String path, String searchPath) throws ClassNotFoundException {
        File file = new File(searchPath);
        File[] files = file.listFiles((f, name)-> name.endsWith(".class"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName().substring(0, f.getName().lastIndexOf("."));
                if (name.contains("$")) continue;
                Class clazz = Class.forName(path + "." + name);
                resultClass.add(clazz);
            }
        }
    }

    public static List<Class> searchJarClass(String path) throws Exception{
        List<Class> resultClass = new ArrayList<>();
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(path.replace(".", "/"));
//        Enumeration<URL> urlEnumeration = AppletClassLoader.getSystemClassLoader().getResources(path.replace(".", "/"));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            String protocol = url.getProtocol();//大概是jar
            if ("jar".equalsIgnoreCase(protocol)) {
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null) {
                    JarFile jarFile = connection.getJarFile();
                    if (jarFile != null) {
                        //得到该jar文件下面的类实体
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements()) {
                            JarEntry entry = jarEntryEnumeration.nextElement();
                            String jarEntryName = entry.getName();
                            //这里我们需要过滤不是class文件和不在basePack包名下的类
                            if (jarEntryName.contains(".class") && jarEntryName.replaceAll("/",".").startsWith(path)) {
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                try{
                                    System.out.println(className);
                                    Class cls = Class.forName(className);
                                    resultClass.add(cls);

                                } catch (Exception e) {
//                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else if ("file".equalsIgnoreCase(protocol)) {
                addClass(resultClass, path, url.getPath());
            }
        }
        return resultClass;
    }

    public static List<String> searchFiles(String dir) {
        List<String> result = new ArrayList();
        File file = new File(dir);
        File[] files = file.listFiles();
        for(File f : files) {
            String name = f.getName();
            if (name.endsWith(".class")) {
                result.add(name.substring(0, name.indexOf(".")));
                continue;
            }
            result.addAll(searchChildFiles(f, f.getName()));
        }
        return result;
    }

    private static List<String> searchChildFiles(File f, String pref) {
        List<String> result = new ArrayList();
        File[] files = f.listFiles();
        for(File c : files) {
            String name = c.getName();
            if (name.endsWith(".class")) {
                result.add(pref + "." + name.substring(0, name.indexOf(".")));
                continue;
            }
            result.addAll(searchChildFiles(c, pref + "." + f.getName()));
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(getSourceRoot());
    }
}
