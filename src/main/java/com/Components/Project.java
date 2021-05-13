package com.Components;

import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Project {

    private String src_path;
    private File dir;
    private ArrayList<ProjectFile> projectFiles = new ArrayList<>();
    private ArrayList<BuildFile> buildFiles = new ArrayList<>();
    private ArrayList<ImportDeclaration> uniqueImports = new ArrayList<>();

    private ArrayList<File> filePaths;


    /***
     * Constructor of Project
     * scans directory and assigns .java files to a list of ProjectFiles pom.xml files are added to a list of BuildFiles, also the determines whether each declared import is used
     * @param src_path source path of the project
     */
    public Project(String src_path){
        this.src_path = src_path;

        this.dir = new File(this.src_path);

        this.filePaths = filePaths(this.dir.listFiles(), new ArrayList<>());

        for(File itm : this.filePaths){
            if (itm.getAbsolutePath().endsWith(".java")
                    && !itm.getAbsolutePath().contains("\\.")) {
                this.projectFiles.add(ProjectFile.initializeProjectFile(itm));
            } else if (itm.getAbsolutePath().contains("pom.xml")){
                this.buildFiles.add(BuildFile.initializeBuildFile(itm));
            }
        }

        this.determineUsedImports();
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
        return buildFiles;
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
        for(BuildFile buildFile : this.getBuildFiles()){
            uniqueDependencies.addAll(new HashSet<>(buildFile.getDeclaredDependencies()));
        }
        return new ArrayList<>(uniqueDependencies);
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
     * @param fileArrayList
     * @return list of .java and pom.xml files
     */

    private ArrayList<File> filePaths(File[] files, ArrayList<File> fileArrayList) {

        for (File file : files) {
            if (file.isDirectory() && !file.getAbsolutePath().contains("target")) {
                this.filePaths(file.listFiles(), fileArrayList);
            } else if (file.getAbsolutePath().contains(".java") || file.getAbsolutePath().contains(".xml")){
                fileArrayList.add(file.getAbsoluteFile());
            }
        }
        return fileArrayList;
    }
}
