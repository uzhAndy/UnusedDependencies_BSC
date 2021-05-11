package com.Components;

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
    private String package_;
    private static String BUILD_FILE_TYPE = "MAVEN";

    public BuildFile(String pathname) {
        super(pathname);
        this.determineDeclaredDependencies();
    }

    public static BuildFile initializeBuildFile(File file){
        return new BuildFile(file.getAbsolutePath());
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

    public ArrayList<Dependency> getDeclaredDependencies() {
        return declaredDependencies;
    }

    public ArrayList<Dependency> getImportedDependencies() {
        return importedDependencies;
    }

    private void determineDeclaredDependencies(){
        try{
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(this.getAbsolutePath()));
            this.declaredDependencies.addAll(model.getDependencies());
        } catch (XmlPullParserException xmlPullParserException) {
            xmlPullParserException.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
