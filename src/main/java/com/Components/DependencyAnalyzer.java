package com.Components;

import com.Utils.Logger;
import com.Utils.RequestBuilder;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

public class DependencyAnalyzer {

    private ArrayList<DependencyExtended> usedDependencies = new ArrayList<>();
    private ArrayList<DependencyExtended> unusedDependencies = new ArrayList<>();
    private ArrayList<String> notDeterminedImports = new ArrayList<>();
    private DependencyReport dependencyReport = new DependencyReport("C:/Users/LenovoThinkPadT450s/Desktop/maven-surefire-master/");
    private Logger log = new Logger();

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


        ArrayList<DependencyExtended> usedDependenciesThisIteration  = this.determineUsedDependencies(project);
        ArrayList<DependencyExtended> unusedDependenciesThisIteration = this.determineUnusedDependencies(project, usedDependencies);

        ArrayList<DependencyExtended> cleanUpUsedDependencies = this.cleanUpUnusedDependencies(unusedDependenciesThisIteration);

        this.usedDependencies.addAll(usedDependenciesThisIteration);
        try{
            for (DependencyExtended dependency : cleanUpUsedDependencies){
                if(!this.usedDependencies.contains(dependency)){
                    this.usedDependencies.add(dependency);
                }
            }
        } catch (NullPointerException e){
        }

        this.dependencyReport.addUsedDependencies(usedDependencies);
        this.dependencyReport.addUnusedDependencies(unusedDependencies);

        this.notDeterminedImports.clear();
    }

    private ArrayList<DependencyExtended> cleanUpUnusedDependencies(ArrayList<DependencyExtended> unusedDependencies){

        ArrayList<DependencyExtended> actuallyUsedDependencies = new ArrayList<>();



        for(String notDeterminedImport: this.notDeterminedImports){
            try {
                DependencyExtended foundDependency = this.searchForFCAndGroupId(notDeterminedImport, unusedDependencies);
                if(!actuallyUsedDependencies.contains(foundDependency) & foundDependency != null){
                    actuallyUsedDependencies.add(foundDependency);
                }
            }catch (NullPointerException e){
            }
        }
        return actuallyUsedDependencies;
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

//        System.out.println("determineUsedDependencies " + project.getSrcPath());

        ArrayList<DependencyExtended> usedDependencies = new ArrayList<>();

        try{
            for(ImportDeclaration importDeclaration : project.getUsedImports()){
//                System.out.println(importDeclaration.getNameAsString());
                if(!importDeclaration.getNameAsString().startsWith("java.")){

                    DependencyExtended dependencyOfClass;


                    dependencyOfClass = this.determineDependencyOfClass(
                                                project,
                                                importDeclaration.getNameAsString()
                    );

                    Project parentProj = project;
                    while(dependencyOfClass.getGroupId() == null && parentProj.isChildModule()){
                        parentProj = parentProj.getParent();
                        dependencyOfClass = this.determineDependencyOfClass(parentProj,
                                                                            importDeclaration.getNameAsString()
                        );
                    }
                    if(dependencyOfClass.getGroupId() == null && !this.notDeterminedImports.contains(importDeclaration.getNameAsString())) {
                        this.notDeterminedImports.add(importDeclaration.getNameAsString());
                    }
                    log.addLog("determineUsedDependencies", parentProj.getSrcPath() + " -- " + importDeclaration.getNameAsString(), dependencyOfClass.toString());
                    try{
                        if(!usedDependencies.contains(dependencyOfClass) && dependencyOfClass.getGroupId() != null){
                            StringBuilder tempRep = new StringBuilder().append("buildFile: " + dependencyOfClass.getBuildFile().getAbsolutePath()).append("\tstored dependencies: ");
                            usedDependencies.forEach(dep -> tempRep.append(dep).append(";"));
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
        JSONArray jsonDependencies = requestBuilder.getDependenciesOfClass();

        ArrayList<DependencyCandidate> dependencyCandidates = DependencyCandidate.convertJSONToDependencyCandidates(jsonDependencies);

        DependencyExtended classDependency = new DependencyExtended();

        ArrayList<Dependency> dependenciesToCompareWith = project.getDeclaredDependencies();

        for (Dependency dependency : dependenciesToCompareWith) {

            if (dependencyCandidates.contains(new DependencyCandidate(dependency))) {
                return new DependencyExtended(project.getBuildFile(), dependency);
            }
        }

        // issue when a method of a class is imported. When removing the method (omitting the last part of the import
        // string (f.i. "org.junit.Assert.assertThat" --> "org.junit.Assert")
        if (importedClass.split("\\.").length > 2) {
            String[] importedClassSplit = importedClass.split("\\.");
            String higherOrderImport = String.join(".", Arrays.copyOfRange(importedClassSplit, 0, importedClassSplit.length - 1));
            return this.determineDependencyOfClass(project, higherOrderImport);

        }
        return classDependency;
    }

    private DependencyExtended searchForFCAndGroupId(String importedClass, ArrayList<DependencyExtended> unusedDependencies){

        RequestBuilder request = new RequestBuilder();

        for(DependencyExtended dependency: unusedDependencies) {
            request.setImportedClass(importedClass + "%20AND%20g:" + dependency.getGroupId());
            request.createConnection();
            try {
                JSONArray jsonDependencies = request.getDependenciesOfClass();
                if (jsonDependencies.size() > 0) {
                    log.addLog("searchForFCAndGroupId",  " -- " + importedClass, dependency.toString());
                    return dependency;
                }
            } catch (IOException | ParseException e) {
            }
        }

        if (importedClass.split("\\.").length > 2) {

            String[] importedClassSplit = importedClass.split("\\.");

            String higherOrderImport = String.join(".",
                                                Arrays.copyOfRange(importedClassSplit,
                                                0,
                                                importedClassSplit.length - 1)
            );
            return this.searchForFCAndGroupId(higherOrderImport, unusedDependencies);

        }

        return null;
    }

}
