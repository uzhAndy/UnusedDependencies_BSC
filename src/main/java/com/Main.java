package com;

import com.Components.DependencyAnalyzer;
import com.Components.Project;

import java.util.ArrayList;


public class Main {

    // root directory of project which is going to be analysed

    public static void main(String[] args){

        long currentTime = System.currentTimeMillis();

        ArrayList<String> FILE_PATHS = new ArrayList<>();

//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\SourceCodeAnalyzer");

        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\Mybatis-PageHelper-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\mall-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\shiro-main");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\camunda-bpm-platform-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\pinpoint-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\eladmin-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\gson-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\aws-doc-sdk-examples-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\rabbitmq-tutorials-master");
        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\languagetool-master");
        try{

            for(String fp : FILE_PATHS){
                // initialize a project
                Project project = new Project(fp, "pom.xml");

                // initialize a dependency analyzer
                DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();

                dependencyAnalyzer.determineDependenciesUsage(project);
                dependencyAnalyzer.produceUsageReport(fp + "/DependencyReport.txt");
            }

            System.out.println("Runtime (seconds): " + (System.currentTimeMillis()-currentTime)/1000);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}


