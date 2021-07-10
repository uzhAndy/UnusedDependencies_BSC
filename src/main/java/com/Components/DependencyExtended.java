package com.Components;

import com.Utils.BuildFile;
import org.apache.maven.model.Dependency;

import java.util.ArrayList;

public class DependencyExtended {

    private BuildFile buildFile;
    private Dependency dependency;
    private boolean properInitialization = false;
    private ArrayList<String> fileUsages = new ArrayList();


    public DependencyExtended(BuildFile buildFile, Dependency dependency, String fileUsage){
        this.buildFile = buildFile;
        this.dependency = dependency;
        this.properInitialization = true;
        this.fileUsages.add(fileUsage);
    }

    public DependencyExtended(BuildFile buildFile, Dependency dependency){
        this.buildFile = buildFile;
        this.dependency = dependency;
        this.properInitialization = true;
    }

    public DependencyExtended(){
    }

    public static ArrayList<DependencyExtended> convertDependenciesToDependenciesExtended(BuildFile buildFile){
        ArrayList<Dependency> declaredDependencies = buildFile.getDeclaredDependencies();
        ArrayList<DependencyExtended> dependenciesExtended = new ArrayList<>();
        for(Dependency dependency: declaredDependencies){
            dependenciesExtended.add(new DependencyExtended(buildFile, dependency));
        }
        return  dependenciesExtended;
    }

    public int getNumberOfImportedClasses() {
        return fileUsages.size();
    }

    public void addFileUsage(String fileUsage){
        this.fileUsages.add(fileUsage);
    }

    public Dependency getDependency() {
        return this.dependency;
    }

    public String getGroupId(){
        if(properInitialization){
            return this.dependency.getGroupId();
        } else{
            return null;
        }
    }

    public String getArtifactId(){
        return this.dependency.getArtifactId();
    }

    public BuildFile getBuildFile() {
            return this.buildFile;
    }

    public String toString(){
        if(this.properInitialization){
            return this.dependency.toString();
        } else {
            return new Dependency().toString();
        }
    }

    @Override
    public boolean equals(Object o){
        DependencyExtended dependencyExtended = (DependencyExtended) o;
        return this.getBuildFile().equals(dependencyExtended.getBuildFile())
                && this.dependency.equals(dependencyExtended.getDependency());
    }

    public enum DependencyType{
        USED, UNUSED;
    }

}
