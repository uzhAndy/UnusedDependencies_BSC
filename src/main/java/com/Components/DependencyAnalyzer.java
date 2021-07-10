package com.Components;

import com.Utils.Logger;
import com.Utils.RequestBuilder;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.FileWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

public class DependencyAnalyzer {

    private ArrayList<DependencyExtended> usedDependencies = new ArrayList<>();
    private ArrayList<DependencyExtended> unusedDependencies = new ArrayList<>();
    private ArrayList<String> notDeterminedImports = new ArrayList<>();
    private ArrayList<String> importStatementsToIgnore = new ArrayList<>(); // imports that import a module from the project can be ignored
    private DependencyReport dependencyReport = new DependencyReport("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\SourceCodeAnalyzer");
    private Logger log= new Logger();
    private String baseModule;
    private ArrayList<String> cachedImports = new ArrayList<>();
    private int declaredDependencyCount;

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

        this.baseModule = project.getSrcPath();


        project.getSubModules().forEach(proj -> {
            this.determineDependenciesUsage(proj);
            this.determineDependenciesOfModule(proj);
        });

        this.determineDependenciesOfModule(project);

        this.dependencyReport.addUsedDependencies(usedDependencies);
        this.dependencyReport.addUnusedDependencies(unusedDependencies);

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
        this.usedDependencies.clear();
        this.unusedDependencies.clear();
    }

    /***
     *
     * @param project
     * @return
     */

    private ArrayList<String> getModuleImportStatements(Project project){

        ArrayList<String> modules = new ArrayList<>();

        for(ImportDeclaration importDeclaration : project.getUsedImports()){
            if(importDeclaration.getNameAsString().contains(project.getRoot().getBuildFile().getGroupId())){
                modules.add(importDeclaration.getNameAsString());
            }
        }
        return modules;
    }

    /***
     *
     * @param project
     */

    private void whiteListModules(Project project){
        for(Dependency dependency : project.getDeclaredDependencies()){

            for(String module : project.getDeclaredModules()){
                if(module.equals(dependency.getArtifactId())){
                    this.usedDependencies.add(new DependencyExtended(project.getBuildFile(), dependency));
                }
            }
        }
    }

    /***
     *
     * @param declaredDependencies
     * @return
     */

    private ArrayList<DependencyExtended>  removeDeclaredModules(ArrayList<DependencyExtended> declaredDependencies){

        ArrayList<DependencyExtended> retDeclaredDependencies = new ArrayList<>(declaredDependencies);

        for(DependencyExtended dependency : declaredDependencies){
            for(DependencyExtended usedDependency : this.usedDependencies){
                if(dependency.equals(usedDependency)){
                    retDeclaredDependencies.remove(dependency);
                }
            }
        }

        return retDeclaredDependencies;
    }

    /***
     *
     * @param project
     */
    private void determineDependenciesOfModule(Project project){

        this.declaredDependencyCount += project.getBuildFile().getDeclaredDependencies().size();

        ArrayList<DependencyExtended> declaredDependencies = DependencyExtended.convertDependenciesToDependenciesExtended(project.getBuildFile());

        this.importStatementsToIgnore = this.getModuleImportStatements(project);
        this.whiteListModules(project);

        declaredDependencies = this.removeDeclaredModules(declaredDependencies);

        ArrayList<DependencyExtended> cleanUpUsedDependencies = this.cleanUpUnusedDependencies(project, declaredDependencies);

        try{
            for (DependencyExtended dependency : cleanUpUsedDependencies){
                if(!this.usedDependencies.contains(dependency)){
                    this.usedDependencies.add(dependency);
                }
            }
        } catch (NullPointerException e){
        }

        ArrayList<DependencyExtended> unusedDependenciesThisModule = determineUnusedDependencies(project, cleanUpUsedDependencies);
        unusedDependenciesThisModule.forEach(dp -> {
            if (!this.unusedDependencies.contains(dp)) {
                this.unusedDependencies.add(dp);
            }
        });

        try {
            this.log.addLogDependencyUsage(cleanUpUsedDependencies, DependencyExtended.DependencyType.USED, project, project.isRoot());
            this.log.addLogDependencyUsage(unusedDependenciesThisModule, DependencyExtended.DependencyType.UNUSED, project, project.isRoot());
        } catch (IOException e){
        }

    }

    /***
     *
     * @param project
     * @param declaredDependencies
     * @return
     */

    private ArrayList<DependencyExtended> cleanUpUnusedDependencies(Project project, ArrayList<DependencyExtended> declaredDependencies){

        ArrayList<DependencyExtended> actuallyUsedDependencies = new ArrayList<>();

        for(ImportDeclaration notDeterminedImport: project.getUsedImports()){
            if(!notDeterminedImport.getNameAsString().startsWith("java.") &&
                    !this.importStatementsToIgnore.contains(notDeterminedImport.getNameAsString()))


            try {
                DependencyExtended foundDependency = this.searchForFCAndGroupId(project,
                                                                        notDeterminedImport.getNameAsString(),
                                                                        declaredDependencies
                                                        );
                if(!actuallyUsedDependencies.contains(foundDependency) & foundDependency != null){
                    foundDependency.addFileUsage(notDeterminedImport.getNameAsString());
                    actuallyUsedDependencies.add(foundDependency);
                } else if (actuallyUsedDependencies.contains(foundDependency)){
                    DependencyExtended temp = actuallyUsedDependencies.get(actuallyUsedDependencies.indexOf(foundDependency));
                    temp.addFileUsage(notDeterminedImport.getNameAsString());
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

        ArrayList<DependencyExtended> usedDependencies = new ArrayList<>();

        try{
            System.out.println("Number of imports: " + project.getUsedImports().size());

            for(ImportDeclaration importDeclaration : project.getUsedImports()){

                System.out.print("Looking for: " + importDeclaration.getNameAsString() + "\t" +
                        "is module:" + this.importIsModule(project.getDeclaredModules(), importDeclaration.getNameAsString()) +
                         "\n"
                );

                if(!(importDeclaration.getNameAsString().startsWith("java.") ||
                        this.importIsModule(project.getDeclaredModules(), importDeclaration.getNameAsString()) ||
                        this.notDeterminedImports.contains(importDeclaration.getNameAsString()))){

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
                    log.addLogClassAndItsDependency("determineUsedDependencies", parentProj.getSrcPath() + " -- " + importDeclaration.getNameAsString(), dependencyOfClass.toString());

                    try{
                        if(!usedDependencies.contains(dependencyOfClass) && dependencyOfClass.getGroupId() != null){
                            StringBuilder tempRep = new StringBuilder().append("buildFile: " + dependencyOfClass.getBuildFile().getAbsolutePath()).append("\tstored dependencies: ");
                            usedDependencies.forEach(dep -> tempRep.append(dep).append(";"));
                            usedDependencies.add(dependencyOfClass);
                        } else if(usedDependencies.contains(dependencyOfClass)){
                            dependencyOfClass.addFileUsage(project.getSrcPath());
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
        try{
            JSONArray jsonDependencies = requestBuilder.getDependenciesOfClass();

            ArrayList<DependencyCandidate> dependencyCandidates = DependencyCandidate.convertJSONToDependencyCandidates(jsonDependencies);

            DependencyExtended classDependency = new DependencyExtended();

            ArrayList<Dependency> dependenciesToCompareWith = project.getDeclaredDependencies();

            for (Dependency dependency : dependenciesToCompareWith) {

                if (dependencyCandidates.contains(new DependencyCandidate(dependency))) {
                    return new DependencyExtended(project.getBuildFile(), dependency, project.getSrcPath());
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
        } catch (ConnectException | NullPointerException e){
         return new DependencyExtended();
        }
    }

    /***
     *
     * @param project
     * @param importedClass
     * @param unusedDependencies
     * @return
     */
    private DependencyExtended searchForFCAndGroupId(Project project, String importedClass, ArrayList<DependencyExtended> unusedDependencies){

        if(this.isMethod(importedClass)){
            importedClass = this.removeMethod(importedClass);
        }

        RequestBuilder request = new RequestBuilder();

        for(DependencyExtended dependency: unusedDependencies) {
            request.setImportedClassGroupIdArtifactId(importedClass, dependency.getGroupId(), dependency.getArtifactId());
            request.createConnection();
            try {
                JSONArray jsonDependencies = request.getDependenciesOfClass();
                if (jsonDependencies.size() > 0) {
                    log.addLogClassAndItsDependency("searchForFCAndGroupId",  " -- " + importedClass, dependency.toString());
                    this.cachedImports.add(importedClass);
                    return dependency;
                }
            } catch (IOException | ParseException e) {
            }
        }

//        if (this.isOriginalImport(importedClass, project.getUsedImports())) {
//
//            String[] importedClassSplit = importedClass.split("\\.");
//
//            String higherOrderImport = String.join(".",
//                                                Arrays.copyOfRange(importedClassSplit,
//                                                0,
//                                                importedClassSplit.length - 1)
//            );
//            if(!this.cachedImports.contains(higherOrderImport)){
//                return this.searchForFCAndGroupId(project, higherOrderImport, unusedDependencies);
//            }
//        }

        if(project.isChildModule()){
            return this.searchForFCAndGroupId(project.getParent(),
                    importedClass,
                    DependencyExtended.convertDependenciesToDependenciesExtended(
                            project.getParent().getBuildFile()
                    )
            );
        } else {
            return null;
        }
    }


    /***
     *
     * @param declaredModules
     * @param declaredImport
     * @return
     */
    private boolean importIsModule(ArrayList<String> declaredModules, String declaredImport){

        for(String declaredModule : declaredModules){
            if(declaredImport.contains(declaredModule)){
                return true;
            }
        }
        return false;
    }

    private boolean isMethod(String importedClass){
        String[] importedClassSplit = importedClass.split("\\.");
        if(Character.isUpperCase(importedClassSplit[0].charAt(0))){
            return true;
        }
        return false;
    }

    private String removeMethod(String importedClass){
        String[] importedClassSplit = importedClass.split("\\.");
        return String.join(".",
                Arrays.copyOfRange(importedClassSplit,
                        0,
                        importedClassSplit.length - 1)
        );
    }

    private boolean isOriginalImport(String importedClass, ArrayList<ImportDeclaration> usedImports) throws IndexOutOfBoundsException{
        for(ImportDeclaration importDeclaration: usedImports){
            if(importDeclaration.getNameAsString().contains(importedClass)){
                if(importDeclaration.getNameAsString().equals(importedClass)){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return true;
    }

}
