package com;

import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.maven.model.Dependency;
import java.util.ArrayList;


public class Main {

    private static final String FILE_PATH = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/Repository mining/SourceCodeAnalyzer";

    public static void main(String[] args){

        try{

            Project currentProject = new Project(FILE_PATH);

            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(currentProject);
            ArrayList<Dependency> importedDependencies = dependencyAnalyzer.determineUsedDependenciesFromImportedClasses();
            ArrayList<Dependency> notImportedDependencies = dependencyAnalyzer.determineUnusedDependenciesFromImportedClasses();

            System.out.println("Number of unused dependencies:");

            for(Dependency dependency: notImportedDependencies){
                System.out.println(dependency);
            }

        } catch(Exception e) {e.printStackTrace();}
    }

}


