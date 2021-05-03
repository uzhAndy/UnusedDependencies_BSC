package com;

import org.apache.maven.model.Dependency;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/***
 *
 */

public class DependencyAnalyzer {

    private Project project;
    private ArrayList<Dependency> importedDependencies = new ArrayList<>();
    private ArrayList<Dependency> notImportedDependencies = new ArrayList<>();

    public DependencyAnalyzer(Project project){
        this.project = project;
    }

    public ArrayList<Dependency> determineUsedDependenciesFromImportedClasses(){

        for(String className : this.project.getImports()){

            try {
                if(!className.startsWith("java.")) this.determineDependencyOfClass(className);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return this.importedDependencies;
    }

    public void determineUnusedImports(){

        UnusedImportsCheck unusedImportsCheck = new UnusedImportsCheck();
//        unusedImportsCheck.beginTree(detailAst);
    }

    public ArrayList<Dependency> determineUnusedDependenciesFromImportedClasses(){

        for(Dependency dependency : this.project.getDependencies()){
            if(!this.importedDependencies.contains(dependency)){
                this.notImportedDependencies.add(dependency);
            }
        }
        return this.notImportedDependencies;
    }

    private void determineDependencyOfClass(String importedClass) throws IOException, ParseException {

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setImportedClass(importedClass);
        requestBuilder.createConnection();
        JSONArray dependencyCandidates = requestBuilder.getDependenciesOfClass();

        int i = 0;
        boolean dependencyFound = false;

        while(i <dependencyCandidates.size() && !dependencyFound){
            String artifact = ((JSONObject) dependencyCandidates.get(i)).get("a").toString();
            String groupId = ((JSONObject) dependencyCandidates.get(i)).get("g").toString();

            for(Dependency dependency: this.project.getDependencies()){

                if(artifact.equals(dependency.getArtifactId())
                        && groupId.equals(dependency.getGroupId())
                        && !dependencyFound){
//                    System.out.println(" --- dependency: " + dependency.toString());
                    dependencyFound = true;
                    if(!this.importedDependencies.contains(dependency)) this.importedDependencies.add(dependency);

                }
            }
            i++;
        }
//    if(!dependencyFound) System.out.println("Could not find imported class: " + importedClass);
    }


}
