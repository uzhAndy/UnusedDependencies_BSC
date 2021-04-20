import org.apache.maven.model.Dependency;

import java.util.ArrayList;


public class Main {

//    private static void printLines(String name, InputStream ins) throws Exception{
//        String line = null;
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(ins)
//        );
//        while ((line = in.readLine()) != null){
//            System.out.println(name + " " + line);
//        }
//    }

//    private static void runProcess(String command, String fileDirectory) throws Exception{
//        Process process = Runtime.getRuntime().exec(command, null, new File(fileDirectory));
//
//        printLines(command + " stdout:", process.getInputStream());
//        printLines(command + " stderr:", process.getErrorStream());
//        process.waitFor();
//        System.out.println(command + " exitValues() " + process.exitValue());
//    }

    public static void main(String[] args){


        try{

            Project currentProject = new Project("C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH" +
                    "/Studium/Bachelor Thesis/Repository mining/SourceCodeAnalyzer/sample_projects");
            ArrayList<Dependency> declaredDependencies = currentProject.getDependencies();
            ArrayList<String> importedClasses = currentProject.getImports();

            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(currentProject);
            dependencyAnalyzer.determineUsedDependenciesFromImportedClasses();
            dependencyAnalyzer.determineUnusedDependenciesFromImportedClasses();

            System.out.println("Breakpoint");




//            List declaredDependencies = getDependenciesFromPom("pom.xml");
//
//            for(int i=0; i<declaredDependencies.size(); i++){
//                System.out.println(declaredDependencies.get(i));
//            }



//            // path of where project folder is
//            String path = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/Repository mining/SourceCodeAnalyzer/sample_projects/basic_mvn_project";
//
//            // create new maven project
//            String createMavenProject = "cmd.exe /c mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false";
//
//            // package new maven project
//            String fileDirectoryPackageMavenProject = path + "/my-app";
//            String packageMavenProject = "cmd.exe /c mvn clean package";
//
//            // execute command to create new maven project
//            runProcess(createMavenProject, path);
//
//            // execute command to package maven project
//            runProcess(packageMavenProject, fileDirectoryPackageMavenProject);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}


