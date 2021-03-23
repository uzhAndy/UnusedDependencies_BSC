import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

    private static void printLines(String name, InputStream ins) throws Exception{
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins)
        );
        while ((line = in.readLine()) != null){
            System.out.println(name + " " + line);
        }
    }

    private static void runProcess(String command, String fileDirectory) throws Exception{
        Process process = Runtime.getRuntime().exec(command, null, new File(fileDirectory));

        printLines(command + "stdout:", process.getInputStream());
        printLines(command + "stderr:", process.getErrorStream());
        process.waitFor();
        System.out.println(command + " exitValues() " + process.exitValue());
    }

    public static void main(String[] args){
        try{

            String path = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/" +
                    "Repository mining/SourceCodeAnalyzer/sample_projects/basic_mvn_project";

            String fileDirectoryCreateMavenProject = path;
            String createMavenProject = "mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false";

            String fileDirectoryPackageMavenProject = "../sample_projects/my-app";
            String packageMavenProject = "mvn package";

            runProcess(createMavenProject, fileDirectoryCreateMavenProject);

        } catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("Hello, World");
    }
}
