//import org.apache.maven.model.Model;
//import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
//import org.apache.maven.project.MavenProject;
//import org.apache.maven.shared.jar.JarAnalyzer;
//import org.apache.maven.shared.jar.identification.JarIdentification;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.net.URL;
//import java.net.URLClassLoader;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//import java.util.zip.ZipException;
//
//
//public class AnalyzeMavenProject {
//
//    public static void main(String args[]) throws IOException {
//
////        Scanner sc = new Scanner(System.in);
////        String path = sc.nextLine();
////        byte[] buf = new byte[1024*1024*50];
//
//        // path of where project folder is
//        String fPath = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/" +
//                "Repository mining/SourceCodeAnalyzer/sample_projects/basic_mvn_project/my-app/target/";
//        String fName = "my-app-1.0-SNAPSHOT.jar";
//        File jarFile = new File(fPath + fName);
//        JarAnalyzer jar = new JarAnalyzer(jarFile);
//        JarIdentification jarIdentification;
//
//        try{
//            System.out.println("Hello, World");
//
//
////            URL[] url = {new URL(fPath + fName)};
//
////            archive.MyClassLoader myClassLoader = new archive.MyClassLoader(url);
//
////            Map<String, List<Class<?>>> test = myClassLoader.scanJar(jar);
//
//        } catch (Exception e){
//            e.printStackTrace();
//        } finally {
//            jar.closeQuietly();
//        }
//    }
//
//
//}
