package archive;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

public class MyClassLoader extends URLClassLoader {


    public MyClassLoader(URL[] urls) {
        super(urls);
    }

    public Map<String, List<Class<?>>> scanJar(File jarFile) throws Exception {

        //load jar file into JVM
        loadJar(jarFile);

        Map<String, List<Class<?>>> classes = new HashMap<String, List<Class<?>>>();

        List<Class<?>> interfaces = new ArrayList<Class<?>>();
        List<Class<?>> classes_ = new ArrayList<Class<?>>();
        List<Class<?>> enums = new ArrayList<Class<?>>();
        List<Class<?>> annotations = new ArrayList<Class<?>>();

        classes.put("interfaces", interfaces);
        classes.put("classes", classes_);
        classes.put("annotations", annotations);
        classes.put("enums", enums);

        //count the classes loaded

        int count = 0;

        //your jar file
        JarFile jar = new JarFile(jarFile);

        //getting the files ito the jar
        Enumeration<? extends JarEntry> enumeration = jar.entries();

        // iterate into the files in the jar
        while (enumeration.hasMoreElements()){
            ZipEntry zipEntry = enumeration.nextElement();

            //check whether zipEntry is class
            if (zipEntry.getName().endsWith(".class")){

                // relative path of file into the jar
                String className = zipEntry.getName();

                //complete class name
                className = className.replace(".class", "").replace("/", ".");

                //load class definition from JVM
                Class<?> class_ = this.loadClass(className);

                try{
                    if (class_.isInterface()){
                        interfaces.add(class_);
                    } else if (class_.isAnnotation()){
                        annotations.add(class_);
                    } else if (class_.isEnum()){
                        enums.add(class_);
                    } else {
                        classes_.add(class_);
                    }
                    count++;
                } catch (ClassCastException e){
                    e.printStackTrace();
                }
            }

        }

        System.out.println("Total: " + count);
        return classes;

    }

    private static synchronized void loadJar(File jar) throws Exception {
        try{
            URLClassLoader loader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            URL url = jar.toURI().toURL();
            //disallowed if already loaded
            for (URL it : Arrays.asList(loader.getURLs())){
                if(it.equals(url)) return ;
            }

            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{
                    URL.class
            });

            method.invoke(loader, new Object[]{url});

        } catch (MalformedURLException e){
            e.printStackTrace();
        }

    }

}
