package com.Components;

import com.Utils.BuildFile;
import com.Utils.Logger;
import com.Utils.ProjectFile;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Project {

    private final String src_path;
    private final String projectType;
    private BuildFile buildFile;
    private boolean isRoot = false;
    private boolean isChildModule = false;
    private boolean isParentModule = false;
    private Project parentModule;
    private ArrayList<ProjectFile> projectFiles = new ArrayList<>();
    private ArrayList<ImportDeclaration> uniqueImports = new ArrayList<>();
    private ArrayList<Project> subModules = new ArrayList<>();
    private ArrayList<File> filePaths = new ArrayList<>();
    private Logger log = new Logger();

    /***
     * Constructor of Project
     * scans directory and assigns .java files to a list of ProjectFiles pom.xml files are added to a list of BuildFiles, also the determines whether each declared import is used
     * @param src_path source path of the project
     */
    public Project(String src_path, String projectType){
        this.src_path = src_path;
        this.isRoot = true;
        this.projectType = projectType;
        this.initializeProject();
    }

    public Project(String src_path, Project parentModule, String projectType){

        this.src_path = src_path;
        this.parentModule = parentModule;
        this.projectType = projectType;
        this.initializeProject();

    }

    public String getProjectType() {
        return projectType;
    }

    public String getSrcPath() {
        return src_path;
    }

    public ArrayList<Project> getSubModules() {
        if(this.isParentModule()){
            if(this.getChild().isParentModule()){
                return this.getChild().getSubModules();
            }
        }
        return this.subModules;
    }

    /***
     * getter for class variable projectFiles
     * @return list of ProjectFiles
     */

    public ArrayList<ProjectFile> getProjectFiles() {
        return projectFiles;
    }

    /***
     * getter for class variable buildFiles
     * @return list of BuildFiles
     */
    public ArrayList<BuildFile> getBuildFiles() {

        ArrayList<BuildFile> buildFiles = new ArrayList<>();

        for(Project project: this.subModules){
            buildFiles.add(project.getBuildFile());
        }

        return buildFiles;
    }

    public BuildFile getBuildFile(){
        return this.buildFile;
    }

    /***
     * getter for class variable usedImports
     * @return list of the imports which are used in the source code
     */

    public ArrayList<ImportDeclaration> getUsedImports() {
        return uniqueImports;
    }

    public ArrayList<Dependency> getDeclaredDependencies(){
        Set<Dependency> uniqueDependencies = new HashSet<>();

        uniqueDependencies.addAll(new HashSet<Dependency>(this.buildFile.getDeclaredDependencies()));

        return new ArrayList<>(uniqueDependencies);
    }

    public ArrayList<String> getDeclaredModules(){

        return this.getAllDeclaredModules(this);

    }

    public Project getSibling(){

        int index = this.getIndexOfSibling();

        try{
            return this.getParent().getSubModules().get(index);
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    public Project getChild(){
        if(this.isParentModule()){
            return this.subModules.get(0);
        }else{
            return null;
        }
    }

    public boolean isParentModule() {
        return this.isParentModule;
    }

    public boolean isChildModule(){
        return this.isChildModule;
    }

    public boolean isRoot() {
        return this.isRoot;
    }

    public Project getParent(){
        return this.parentModule;
    }

    public Project getRoot(){
        if(this.isRoot){
            return this;
        } else {
            return this.getParent().getRoot();
        }
    }



    private void initializeProject(){

        File dir = new File(this.src_path);

        this.determineProjectStructure(dir.listFiles());

        for(File itm : this.filePaths){
            if (itm.getAbsolutePath().endsWith(".java")
                    && !itm.getAbsolutePath().contains("\\.")
                    && !itm.getAbsolutePath().contains("module-info.java")) {
                this.projectFiles.add(ProjectFile.initializeProjectFile(itm));
            } else if (itm.getAbsolutePath().contains(projectType)){
//                System.out.println("processing build file: " + itm.getAbsolutePath());
                this.buildFile = BuildFile.initializeBuildFile(itm);
            }
        }

        this.determineUsedImports();
        try {
            this.log.addLogImportDeclarations(this, this.isRoot);
            this.log.addLogDeclaredDependencies(this, this.isRoot);

        }catch (IOException e){
            System.out.println("Adding logs for import declarations or declared dependencies failed");
        }
    }

    private int getIndexOfSibling(){
        int ret = -1;
        for(int i = 0; i<this.getParent().getSubModules().size(); i++){
            if(this.getParent().getSubModules().get(i) == this){
                return i +1;
            }
        }
        return ret;
    }

    /***
     * Consolidates all declared and used imports in the ProjectFiles to a list without duplicates
     */
    private void determineUsedImports(){

        Set<ImportDeclaration> allUsedImports = new HashSet<>();

        for(ProjectFile projectFile : this.getProjectFiles()){
            allUsedImports.addAll(new HashSet<>(projectFile.getUsedImports()));
        }
        this.uniqueImports = new ArrayList<>(allUsedImports);
    }

    /***
     * getting all files from the file path given in the constructor
     * @param files
     * @return list of .java and pom.xml files
     */

    private void determineProjectStructure(File[] files) {

        for (File file : files) {

            if (file.isDirectory()) {
                if(this.isSubmodule(file)){
                    Project subModule = new Project(file.getAbsolutePath(), this, projectType);
                    subModule.isChildModule = true;
                    this.subModules.add(subModule);
                    this.isParentModule = true;

                }else{
                    this.determineProjectStructure(file.listFiles());
                }
            } else if (file.getAbsolutePath().contains(".java") || file.getAbsolutePath().contains(projectType)){
                this.filePaths.add(file.getAbsoluteFile());
            }
        }
    }

    public ArrayList<Dependency> getAllDependencies(){
        ArrayList<Dependency> dependencies = new ArrayList<>();
        Project proj = this;
        while(proj.isChildModule()){
            dependencies.addAll(this.getParent().getAllDependencies());
            proj = proj.getParent();
        }
        dependencies.addAll(this.getDeclaredDependencies());
        return dependencies;
    }

    public ArrayList<String> getAllDeclaredModules(Project project){
        while(project.isChildModule){
            return getAllDeclaredModules(project.getParent());
        }
        return project.getBuildFile().getDeclaredModules();
    }

    private boolean isParenModule(){
        return this.isParentModule;
    }

    private boolean isSubmodule(File file){

        File[] toAnalyse = file.listFiles();

        for(File subFile : toAnalyse){
            if (subFile.getAbsolutePath().contains(projectType)){
                return true;
            }
        }
        return false;
    }
}
