package com.Utils;

import com.Components.DependencyExtended;
import com.Components.Project;
import com.github.javaparser.ast.ImportDeclaration;
import org.apache.maven.model.Dependency;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class Logger {

    private static String CLASS_DEPENDENCY_LOG = "C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Project_Analysis\\NEW\\Logs.txt";
    private static String DEPENDENCY_USAGE_REPORT = "C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Project_Analysis\\NEW\\DependencyReportUsage.txt";
    private static String IMPORT_DECLARATIONS = "C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Project_Analysis\\NEW\\ImportDeclarations.txt";
    private static String DEPENDENCY_REPORT_DECLARATIONS = "C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Project_Analysis\\NEW\\DependencyReportDeclarations.txt";
    private static String NOT_DETERMINED_IMPORTS = "C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Project_Analysis\\NEW\\NotDeterminedImports.txt";

    private FileWriter fileWriter;

    public Logger(){
    }

    public void addLogClassAndItsDependency(String methodName, String parameters, String returnValue) throws IOException {

        this.fileWriter = new FileWriter(CLASS_DEPENDENCY_LOG, true);

        StringBuilder logString = new StringBuilder();

        Date logTime = new Date();
        logString.append(logTime).append("\t")
                    .append(methodName).append("\t")
                    .append(parameters).append("\t")
                    .append(returnValue).append("\n");

        this.fileWriter.write(logString.toString());
        this.fileWriter.close();
    }

    public void addLogNotDeterminedImports(Project project, ArrayList<String> notDeterminedImports) {

        ArrayList<Dependency> dependencies = project.getAllDependencies();

        try{
            this.fileWriter = new FileWriter(NOT_DETERMINED_IMPORTS, true);
            StringBuilder logString = new StringBuilder();
            for(String imprt : notDeterminedImports){
                logString.append(project.getSrcPath()).append(";").append(imprt).append(";").append(dependencies).append("\n");
            }

            this.fileWriter.write(logString.toString());
            this.fileWriter.close();
        } catch (IOException e){

        }


    }

    public void addLogDependencyUsage(ArrayList<DependencyExtended> dependencies, DependencyExtended.DependencyType usageType, Project project, boolean reportStart) throws IOException {

        this.fileWriter = new FileWriter(DEPENDENCY_USAGE_REPORT, true);

        String baseModule = project.getSrcPath();

        if(!baseModule.equals(baseModule)) {
            baseModule = project.getSrcPath().replace(project.getRoot().getSrcPath(), "");
        }
        StringBuilder logString = new StringBuilder();

        if(reportStart){
            logString.append(baseModule).append("\n");
        }

        logString.append("************** Module: ").append(baseModule).append(" Number of ").append(usageType).append(" dependencies: ")
                .append(dependencies.size()).append("\n").append("**************").append("\n");

        Date logTime = new Date();

        for(DependencyExtended dependency: dependencies){
            logString.append(logTime).append("\t")
                    .append(usageType).append("\tmodule: ").append(baseModule).append(" build file: ").append(dependency.getBuildFile().toString().replace(baseModule, "")).append("\t")
                    .append("groupId:").append(dependency.getGroupId()).append(", artifactId").append(dependency.getArtifactId()).append("\t")
                    .append(usageType).append("\n").append("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").append("\n");
        }

        this.fileWriter.write(logString.toString());
        this.fileWriter.close();

    }

    public void addLogImportDeclarations(Project project, boolean reportStart) throws IOException {

        this.fileWriter = new FileWriter(IMPORT_DECLARATIONS, true);

        String projectFPath = project.getSrcPath();

        StringBuilder logString = new StringBuilder();


        for(ImportDeclaration importDeclaration: project.getUsedImports()){
            logString.append(projectFPath).append(";").append(importDeclaration.getNameAsString()).append("\n");

        }

        this.fileWriter.write(logString.toString());
        this.fileWriter.close();

    }

    public void addLogDeclaredDependencies(Project project, boolean reportStart) throws IOException {

        this.fileWriter = new FileWriter(DEPENDENCY_REPORT_DECLARATIONS, true);

        StringBuilder logString = new StringBuilder();

        String buildFilePath = project.getSrcPath();

        for(Dependency dependency: project.getBuildFile().getDeclaredDependencies()){
            logString.append(buildFilePath).append(";").append("GroupId: ").append(dependency.getGroupId()).append("\tArtifactId: ").append(dependency.getArtifactId()).append("\n");
        }

        this.fileWriter.write(logString.toString());
        this.fileWriter.close();
    }

}
