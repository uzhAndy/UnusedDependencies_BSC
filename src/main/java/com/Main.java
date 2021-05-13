package com;

import com.Components.DependencyAnalyzer;
import com.Components.Project;


public class Main {

    // root directory of project which is going to be analysed
    private static final String FILE_PATH = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/Repository mining/sample_projects/maven-samples-master/single-module";

    public static void main(String[] args){

        try{

            // initialize a project
            Project project = new Project(FILE_PATH);

            // initialize a dependency analyzer
            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(project);

            dependencyAnalyzer.determineDependenciesUsage();
            dependencyAnalyzer.produceUsageReport(FILE_PATH + "/DependencyReport.txt");

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


