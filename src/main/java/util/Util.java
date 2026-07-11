package util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Util {
    
   
    public enum NiveauScan {
        CLASSE, METHODE, ATTRIBUT
    }

  
    public static List<Class<?>> getClassesWithAnnotation(String packageName, Class<?> annotationClass, NiveauScan niveau) throws Exception {
        
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException(annotationClass.getName() + " n'est pas une annotation valide !");
        }

        List<Class<?>> classesFiltrees = new ArrayList<>();
        List<Class<?>> toutesLesClasses = getClassesInPackage(packageName);
        

        for (Class<?> clazz : toutesLesClasses) {
        String targetAnnotationName = annotationClass.getName();
               
            for (Annotation anno : clazz.getAnnotations()) {
                if (anno.annotationType().getName().equals(targetAnnotationName)) {
                    classesFiltrees.add(clazz);
                    break;
                }
            }
            
        }
        return classesFiltrees;
    }

    public static List<Method> getMethodsWithAnnotation(Class<?> packageName, Class<?> annotationClass, NiveauScan niveau) throws Exception {
        
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException(annotationClass.getName() + " n'est pas une annotation valide !");
        }

        List<Method> classesFiltrees = new ArrayList<>();
       
        String targetAnnotationName = annotationClass.getName();

                for (Method method : packageName.getDeclaredMethods()) {
                  
                    for (Annotation anno : method.getAnnotations()) {
                        if (anno.annotationType().getName().equals(targetAnnotationName)) {
                            classesFiltrees.add(method);
                          
                        }
                    }
                  
                }
        
        return classesFiltrees;
    }

    
    public static List<Class<?>> getClassesInPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(packagePath);
        
        if (resource == null) {
            System.out.println(" Package introuvable : " + packageName);
            return classes;
        }
        
       
        String rawPath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8.name());
        
       
        if (rawPath.startsWith("file:")) {
            rawPath = rawPath.substring(5);
        }
        
      
        if (rawPath.contains("!")) {
            rawPath = rawPath.substring(0, rawPath.indexOf("!"));
        }
        
        File directory = new File(rawPath);

        System.out.println("[DEBUG SCAN] Recherche dans le dossier physique : " + directory.getAbsolutePath());

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                  
                    if (file.getName().endsWith(".class")) {
                        String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                        classes.add(Class.forName(className));
                    }
                }
            }
        } else {
            System.out.println(" Le chemin détecté n'est pas un dossier physique valide pour Tomcat : " + rawPath);
        }
        
        return classes;
    }
}