import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Project {

    private String src_path;
    File dir;
    ArrayList<File> projectFiles = new ArrayList<>();
    ArrayList<File> buildFiles = new ArrayList<>();
    ArrayList<File> filePaths;
    ArrayList<Dependency> scrappedDependencies = new ArrayList<>();
    ArrayList<Dependency> dependencies = new ArrayList<>();
    ArrayList<Dependency> notImportedDependencies = new ArrayList<>();
    private ArrayList<Dependency> importedDependencies = new ArrayList<>();

    ArrayList<String> imports = new ArrayList<>();

    public Project(String src_path){
        this.src_path = src_path;

        this.dir = new File(this.src_path);

        this.filePaths = filePaths(this.dir.listFiles(), new ArrayList<>());
        for(File itm : this.filePaths){
            if (itm.getAbsolutePath().endsWith(".java")) {
                this.projectFiles.add(itm);
            } else {
                this.buildFiles.add(itm);
            }
        }
    }

    /***
     *
     * @return list of scraped dependencies from pom.xml files
     * @throws IOException
     * @throws XmlPullParserException
     */
    public ArrayList<Dependency> getDependencies() {
        if(this.scrappedDependencies.isEmpty()){
            this.determineDependenciesFromPom();
        }
        return this.scrappedDependencies;
    }

    public ArrayList<Dependency> getImportedDependencies() {
        return importedDependencies;
    }

    //todo add implementation to get all dependencies that have not been imported in the project files
    public ArrayList<Dependency> getNotImportedDependencies(){
        if(this.notImportedDependencies.isEmpty()){
            this.determineNotImportedDependencies();
        }
        return this.notImportedDependencies;
    }
//todo add implementation
    private void determineNotImportedDependencies(){
        for(Dependency dependency : this.scrappedDependencies){
        }
    }

    /***
     *
     * @return list of imports that have been found in the java project

     */
    public ArrayList<String> getImports(){
        if(this.imports.isEmpty()){
            this.determineImportsFromJavaFiles();
        }
        return this.imports;
    }

    /***
     * This method scrapes all pom.xml files and adds them to
     */
    private void determineDependenciesFromPom(){

        this.scrappedDependencies.clear();
        try{
            for(File itm : this.buildFiles){
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(itm.getAbsolutePath()));
                this.scrappedDependencies.addAll(model.getDependencies());
            }

            for(Dependency dependency : this.scrappedDependencies){
                if(!this.dependencies.contains(dependency)){
                    this.dependencies.add(dependency);
                }
            }
        } catch (IOException ioException){
            ioException.printStackTrace();
        } catch (XmlPullParserException xmlPullParserException){
            xmlPullParserException.printStackTrace();
        }

    }

    private void determineImportsFromJavaFiles(){

        this.imports.clear();
        String importPattern = "(import).*;";
        Pattern pattern = Pattern.compile(importPattern);

        try{
            for(File itm : this.projectFiles){
                System.out.println(itm.toString());
                FileReader fileReader = new FileReader(itm);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String line;

                while((line=bufferedReader.readLine())!=null){
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find() && !this.imports.contains(line)){
                        this.imports.add(line.replace("import ", "").replace(";", ""));
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private ArrayList<File> filePaths(File[] files, ArrayList<File> fileArrayList) {

        for (File file : files) {
            if (file.isDirectory() && !file.getAbsolutePath().contains("target")) {
                filePaths(file.listFiles(), fileArrayList);
            } else if (file.getAbsolutePath().contains(".java") || file.getAbsolutePath().contains(".xml")){
                fileArrayList.add(file.getAbsoluteFile());
            }
        }
        return fileArrayList;
    }

    public void addImportedDependency(Dependency dependency){
        this.importedDependencies.add(dependency);
    }

    public void addNotImportedDependency(Dependency dependency){
        this.notImportedDependencies.add(dependency);
    }

}
