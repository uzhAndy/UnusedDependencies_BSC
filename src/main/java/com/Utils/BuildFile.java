package com.Utils;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BuildFile extends File {

    private ArrayList<Dependency> declaredDependencies = new ArrayList();
    private ArrayList<Dependency> importedDependencies = new ArrayList<>();
    private static String BUILD_FILE_TYPE = "MAVEN";


    /***The BuildFile class extends the regular file class to have additional functionalities which are relevant to the
     * dependency usage analysis by converting the .java file to its DetailAST representation
     * @param pathname
     */
    public BuildFile(String pathname) {
        super(pathname);
        this.determineDeclaredDependencies();
    }

    /***Simplification of initialization of the ProjectFile class
     * @param file .java file
     * @return projectFile instance
     */
    public static BuildFile initializeBuildFile(File file){
        return new BuildFile(file.getAbsolutePath());
    }

    /***
     * getter method of declared dependencies in the build file
     * @return declaredDependencies
     */
    public ArrayList<Dependency> getDeclaredDependencies() {
        return declaredDependencies;
    }

    /***
     * retrieving a list of dependencies declared in the buildFile (currently only maven) and storing
     * the dependencies to the list of declared dependencies
     */
    private void determineDeclaredDependencies(){
        try{
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(this.getAbsolutePath()));
            this.declaredDependencies.addAll(model.getDependencies());
//            this.declaredDependencies.forEach(dependency -> System.out.println(dependency));
        } catch (XmlPullParserException xmlPullParserException) {
            xmlPullParserException.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public ArrayList<Dependency> getImportedDependencies() {
        return importedDependencies;
    }

    public void setDeclaredDependencies(ArrayList<Dependency> declaredDependencies) {
        this.declaredDependencies = declaredDependencies;
    }

    public void addDeclaredDependency(Dependency dependency){
        this.declaredDependencies.add(dependency);
    }

    public void setImportedDependencies(ArrayList<Dependency> importedDependencies) {
        this.importedDependencies = importedDependencies;
    }

    public void addImportedDependency(Dependency dependency){
        this.importedDependencies.add(dependency);
    }
}
