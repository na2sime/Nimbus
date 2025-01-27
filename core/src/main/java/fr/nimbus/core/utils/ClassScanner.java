package fr.nimbus.core.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClassScanner {

    private ClassScanner() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Class<?>> scan(String packageName) throws ClassNotFoundException {
        String path = packageName.replace('.', '/');
        File directory = new File("target/classes/" + path);

        List<Class<?>> classes = new ArrayList<>();
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().replace(".class", "");
                    classes.add(Class.forName(className));
                }
            }
        }
        return classes;
    }

}
