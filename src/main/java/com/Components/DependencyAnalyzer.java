package com.Components;

import com.Utils.Logger;
import com.Utils.RequestBuilder;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;



public class DependencyAnalyzer {

    private DependencyReport dependencyReport = new DependencyReport("C:/Users/LenovoThinkPadT450s/Desktop/maven-surefire-master/");
    Logger log = new Logger();


    /***Class to analyze the dependency usage of a given project. It mainly checks which dependency a used and imported
     * class is from and adds said dependency to a list of used dependencies. To obtain the list of unused dependencies,
     * a set difference is created.
     */

    public DependencyAnalyzer(){
    }

    /***
     * determine the dependency of each of the used imports in the project and add them to their respective list.
     */
    public void determineDependenciesUsage(Project project){

        project.getSubModules().forEach(proj -> this.determineDependenciesUsage(proj));

//        System.out.println("Project: " + project.getSrcPath());
        this.determineDependenciesOfModule(project);

    }

    /***
     * Given the path, where the report is supposed to be located, a text report is produced, which writes down the used
     * and unused dependencies of the analyzed project.
     * @param fPath
     * @throws IOException
     */

    public void produceUsageReport(String fPath) throws IOException {

        FileWriter fw = new FileWriter(fPath, true);

        fw.write(this.dependencyReport.getReport());
        fw.close();
    }

    private void determineDependenciesOfModule(Project project){

        ArrayList<DependencyExtended> usedDependencies = this.determineUsedDependencies(project);
        ArrayList<DependencyExtended> unUsedDependencies = this.determineUnusedDependencies(project, usedDependencies);

        this.dependencyReport.addUsedDependencies(usedDependencies);

        this.dependencyReport.addUnusedDependencies(unUsedDependencies);
    }

    /***
     * creating the set difference of declared dependencies in a project and the used dependencies of a project
     * @return
     */
    private ArrayList<DependencyExtended> determineUnusedDependencies(Project project, ArrayList<DependencyExtended> usedDependencies){

        ArrayList<DependencyExtended> unusedDependencies = new ArrayList<>();
        for(Dependency declaredDependency : project.getDeclaredDependencies()){

            DependencyExtended dependencyExtended = new DependencyExtended(project.getBuildFile(), declaredDependency);

            if(!usedDependencies.contains(dependencyExtended)){
                unusedDependencies.add(dependencyExtended);
            }
        }
        return unusedDependencies;
    }

    /***
     * determining what dependencies are effectively used in a project based on the used imports
     */
    private ArrayList<DependencyExtended> determineUsedDependencies(Project project){

        ArrayList<DependencyExtended> usedDependencies = new ArrayList<>();

        try{
            for(ImportDeclaration importDeclaration : project.getUsedImports()){
                if(!importDeclaration.getNameAsString().startsWith("java.")){

                    DependencyExtended dependencyOfClass;


                    dependencyOfClass = this.determineDependencyOfClass(project,
                                                                                importDeclaration.getNameAsString()

                    );
                    Project parentProj = project;
                    while(dependencyOfClass.getGroupId() == null && parentProj.isChildModule()){

//                        System.out.println("Could not find import: " + importDeclaration.getNameAsString()
//                                        + " in pom: " + parentProj.getBuildFile().getAbsolutePath() + " looking in: " + parentProj.getParent().getBuildFile().getAbsolutePath());

                        parentProj = parentProj.getParent();
                        dependencyOfClass = this.determineDependencyOfClass(parentProj,
                                                                            importDeclaration.getNameAsString()
                        );
                    }


//                    System.out.println("Import: " + importDeclaration.getNameAsString() + "\tDependency: " + dependencyOfClass.toString());


                    log.addLog("determineUsedDependencies", parentProj.getSrcPath() + " - " + importDeclaration.getNameAsString(), dependencyOfClass.toString());
                    try{
                        if(!usedDependencies.contains(dependencyOfClass) && dependencyOfClass.getGroupId() != null){
                            StringBuilder tempRep = new StringBuilder().append("buildFile: " + dependencyOfClass.getBuildFile().getAbsolutePath()).append("\tstored dependencies: ");
                            usedDependencies.forEach(dep -> tempRep.append(dep).append(";"));
//                            System.out.println(tempRep. append("\t new dependency:" + dependencyOfClass));
                            usedDependencies.add(dependencyOfClass);
                        }
                    } catch (NullPointerException e){

                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return usedDependencies;
    }


    /***
     * By sending a get request to the search.maven.org API, the dependency which is used in the project can be determined.
     * @param importedClass
     * @return Dependency of the imported class
     * @throws IOException
     * @throws ParseException
     */
    private DependencyExtended determineDependencyOfClass(Project project, String importedClass) throws IOException, ParseException {

        RequestBuilder requestBuilder = new RequestBuilder();
        requestBuilder.setImportedClass(importedClass);
        requestBuilder.createConnection();
        JSONArray dependencyCandidates = requestBuilder.getDependenciesOfClass();

        DependencyExtended classDependency = new DependencyExtended();

        int i = 0;
        ArrayList<Dependency> dependenciesToCompareWith;


        dependenciesToCompareWith = project.getDeclaredDependencies();

        while(i <dependencyCandidates.size()){
            String artifact = ((JSONObject) dependencyCandidates.get(i)).get("a").toString();
            String groupId = ((JSONObject) dependencyCandidates.get(i)).get("g").toString();


            for(Dependency dependency: dependenciesToCompareWith){

                if(artifact.equals(dependency.getArtifactId())
                        && groupId.equals(dependency.getGroupId())){
                    return new DependencyExtended(project.getBuildFile(), dependency);
                }
            }

            i++;
        }
        // issue when a method of a class is imported. When removing the method (omitting the last part of the import
        // string (f.i. "org.junit.Assert.assertThat" --> "org.junit.Assert")
        if(importedClass.split("\\.").length > 2){
            String[] importedClassSplit = importedClass.split("\\.");
            String higherOrderImport = String.join(".", Arrays.copyOfRange(importedClassSplit, 0,importedClassSplit.length - 1));
            return this.determineDependencyOfClass(project, higherOrderImport);

        }
        return classDependency;
    }

//    private boolean notAlreadyDetermined(String importedClass, Project project){
//
//
//
//    }
//
//    private void addMap(String importedClass, Dependency dependency){
//
//    }

}
