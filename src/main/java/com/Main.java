package com;

import com.Components.DependencyAnalyzer;
import com.Components.Project;
import org.apache.maven.model.Dependency;

import java.io.File;
import java.io.FileWriter;

public class Main {

    private static final String FILE_PATH = "C:/Users/LenovoThinkPadT450s/OneDrive - Universität Zürich UZH/Studium/Bachelor Thesis/Repository mining/sample_projects/maven-samples-master/single-module";

    public static void main(String[] args){

        try{
            Project project = new Project(FILE_PATH);

            DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer(project);
            dependencyAnalyzer.determineDependenciesUsage();

            StringBuilder report = new StringBuilder();

            report.append("Unused Dependencies: \n");

            for(Dependency dependency : dependencyAnalyzer.getUnusedDependencies()){
                report.append(dependency.toString()).append("\n");
            }

            report.append("Used Dependencies: \n");

            for(Dependency dependency : dependencyAnalyzer.getUsedDependencies()){
                report.append(dependency.toString()).append("\n");
            }

            String reportFilePath = FILE_PATH + "/DependencyReport.txt";

            File newTxt = new File(reportFilePath);
            FileWriter fw = new FileWriter(reportFilePath);

            fw.write(report.toString());
            fw.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


