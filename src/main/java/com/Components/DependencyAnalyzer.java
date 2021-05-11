package com.Components;

import com.Utils.RequestBuilder;
import com.github.javaparser.ast.ImportDeclaration;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import org.apache.maven.model.Dependency;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
        this.project.determineUsedImports();

        this.addUsedDependencies();
        for(Dependency declaredDependency : this.project.getDeclaredDependencies()){
            if(!this.usedDependencies.contains(declaredDependency)){
                this.unusedDependencies.add(declaredDependency);
            }
        }

    }

    public ArrayList<Dependency> getUsedDependencies() {
        return usedDependencies;
    }

    public ArrayList<Dependency> getUnusedDependencies() {
        return unusedDependencies;
    }

    private void addUsedDependencies(){
        try{
            for(ImportDeclaration importDeclaration : this.project.getUsedImports()){
                if(!importDeclaration.getNameAsString().startsWith("java.")){

                    Dependency dependencyOfClass = this.determineDependencyOfClass(importDeclaration.getNameAsString());
                    if(dependencyOfClass.getGroupId() != null){
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
        return classDependency;
    }




}
