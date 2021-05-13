package com.Components;

import com.Utils.RequestBuilder;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/***
 *
 */

public class DependencyAnalyzer {

    private Project project;

    private ArrayList<Dependency> usedDependencies = new ArrayList<>();
    private ArrayList<Dependency> unusedDependencies = new ArrayList<>();

    public DependencyAnalyzer(Project project){
        this.project = project;
    }

    public void determineDependenciesUsage(){
        this.addUsedDependencies();
        this.addUnusedDependencies();
    }

    public ArrayList<Dependency> getUsedDependencies() {
        return usedDependencies;
    }

    public ArrayList<Dependency> getUnusedDependencies() {
        return unusedDependencies;
    }

    private void addUnusedDependencies(){
        for(Dependency declaredDependency : this.project.getDeclaredDependencies()){
            if(!this.usedDependencies.contains(declaredDependency)){
                this.unusedDependencies.add(declaredDependency);
            }
        }
    }

    private void addUsedDependencies(){
        try{
            for(ImportDeclaration importDeclaration : this.project.getUsedImports()){
                if(!importDeclaration.getNameAsString().startsWith("java.")){

                    Dependency dependencyOfClass = this.determineDependencyOfClass(importDeclaration.getNameAsString());
                    if(dependencyOfClass.getGroupId() != null && !this.usedDependencies.contains(dependencyOfClass)){
                        this.usedDependencies.add(dependencyOfClass);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private Dependency determineDependencyOfClass(String importedClass) throws IOException, ParseException {

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setImportedClass(importedClass);
        requestBuilder.createConnection();
        JSONArray dependencyCandidates = requestBuilder.getDependenciesOfClass();

        Dependency classDependency = new Dependency();

        int i = 0;
        boolean dependencyFound = false;

        while(i <dependencyCandidates.size() && !dependencyFound){
            String artifact = ((JSONObject) dependencyCandidates.get(i)).get("a").toString();
            String groupId = ((JSONObject) dependencyCandidates.get(i)).get("g").toString();

            for(BuildFile buildFile: this.project.getBuildFiles()){
                for(Dependency dependency: buildFile.getDeclaredDependencies()){

                    if(artifact.equals(dependency.getArtifactId())
                            && groupId.equals(dependency.getGroupId())
                            && !dependencyFound){
                        dependencyFound = true;
                        classDependency = dependency;
                    }
                }
            }
            i++;
        }

        if(classDependency.getGroupId() == null && importedClass.split("\\.").length > 2){
            String[] importedClassSplit = importedClass.split("\\.");
            String higherOrderImport = String.join(".", Arrays.copyOfRange(importedClassSplit, 0,importedClassSplit.length - 1));

            return this.determineDependencyOfClass(higherOrderImport);

        }

        return classDependency;
    }




}
