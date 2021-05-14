package com.Components;

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

    private final Project project;

    private final ArrayList<Dependency> usedDependencies = new ArrayList<>();
    private final ArrayList<Dependency> unusedDependencies = new ArrayList<>();

    /***Class to analyze the dependency usage of a given project. It mainly checks which dependency a used and imported
     * class is from and adds said dependency to a list of used dependencies. To obtain the list of unused dependencies,
     * a set difference is created.
     * @param project
     */

    public DependencyAnalyzer(Project project){
        this.project = project;
    }

    /***getter method of the usedDependencies list
     * @return usedDependencies
     */
    public ArrayList<Dependency> getUsedDependencies() {
        return usedDependencies;
    }

    /**getter method of the unusedDependencies list
     * @return unusedDependencies
     */
    public ArrayList<Dependency> getUnusedDependencies() {
        return unusedDependencies;
    }

    /***
     * determine the dependency of each of the used imports in the project and add them to their respective list.
     */
    public void determineDependenciesUsage(){
        this.addUsedDependencies();
        this.addUnusedDependencies();
    }

    /***
     * Given the path, where the report is supposed to be located, a text report is produced, which writes down the used
     * and unused dependencies of the analyzed project.
     * @param fPath
     * @throws IOException
     */

    public void produceUsageReport(String fPath) throws IOException {

        StringBuilder report = new StringBuilder();

        report.append("Unused Dependencies: \n");

        for(Dependency dependency : this.getUnusedDependencies()){
            report.append(dependency.toString()).append("\n");
        }

        report.append("Used Dependencies: \n");

        for(Dependency dependency : this.getUsedDependencies()){
            report.append(dependency.toString()).append("\n");
        }

        FileWriter fw = new FileWriter(fPath);

        fw.write(report.toString());
        fw.close();
    }

    /***
     * creating the set difference of declared dependencies in a project and the used dependencies of a project
     */
    private void addUnusedDependencies(){
        for(Dependency declaredDependency : this.project.getDeclaredDependencies()){
            if(!this.usedDependencies.contains(declaredDependency)){
                this.unusedDependencies.add(declaredDependency);
            }
        }
    }

    /***
     * determining what dependencies are effectively used in a project based on the used imports
     */
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


    /***
     * By sending a get request to the search.maven.org API, the dependency which is used in the project can be determined.
     * @param importedClass
     * @return Dependency of the imported class
     * @throws IOException
     * @throws ParseException
     */
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
        // issue when a method of a class is imported. When removing the method (omitting the last part of the import
        // string (f.i. "org.junit.Assert.assertThat" --> "org.junit.Assert")
        if(classDependency.getGroupId() == null && importedClass.split("\\.").length > 2){
            String[] importedClassSplit = importedClass.split("\\.");
            String higherOrderImport = String.join(".", Arrays.copyOfRange(importedClassSplit, 0,importedClassSplit.length - 1));

            return this.determineDependencyOfClass(higherOrderImport);

        }

        return classDependency;
    }




}
