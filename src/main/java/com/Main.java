package com;

import com.Components.DependencyAnalyzer;
import com.Components.Project;
import org.apache.maven.model.Dependency;

public class Main {

    private static final String FILE_PATH = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/Repository mining/SourceCodeAnalyzer";

    public static void main(String[] args){

        try{
            Project project = new Project(FILE_PATH);

            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(project);
            dependencyAnalyzer.determineDependenciesUsage();

            System.out.println("Unused Dependencies: ");

            for(Dependency dependency : dependencyAnalyzer.getUnusedDependencies()){
                System.out.println(dependency);
            }

            System.out.println("Used Dependencies: ");

            for(Dependency dependency : dependencyAnalyzer.getUsedDependencies()){
                System.out.println(dependency);
            }

        } catch(Exception e) {e.printStackTrace();}
    }

}


