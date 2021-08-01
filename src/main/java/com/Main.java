package com;

import com.Components.DependencyAnalyzer;
import com.Components.Project;

import java.util.ArrayList;


public class Main {

    // root directory of project which is going to be analysed
    public static String REPORTS_PATH = "C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Project_Analysis\\NEW";

    public static void main(String[] args){

        long currentTime = System.currentTimeMillis();

        ArrayList<String> FILE_PATHS = new ArrayList<>();

        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\SourceCodeAnalyzer");

//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\Mybatis-PageHelper-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\mall-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\shiro-main");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\camunda-bpm-platform-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\pinpoint-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\eladmin-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\gson-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\languagetool-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\vitess-main");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\spring-boot-admin-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\roncoo-pay-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\Recaf-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\megabasterd-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\flink-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\fastjson-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\amidst-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\algs4-master");

//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\WxJava-develop");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\rest-assured-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\kafka-eagle-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\jsonschema2pojo-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\javapoet-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\cglib-master");
//        FILE_PATHS.add("C:\\Users\\LenovoThinkPadT450s\\OneDrive - Universität Zürich UZH\\Studium\\Bachelor Thesis\\Repository mining\\Projects_To_Analyze\\cat-master");

        try{

            for(String fp : FILE_PATHS){
                // initialize a project
                Project project = new Project(fp, "pom.xml");

                // initialize a dependency analyzer
                DependencyAnalyzer dependencyAnalyzer = new DependencyAnalyzer();

                dependencyAnalyzer.determineDependenciesUsage(project);
                dependencyAnalyzer.produceUsageReport(REPORTS_PATH + "/DependencyReport.txt");
            }

            System.out.println("Runtime (hours): " + (System.currentTimeMillis()-currentTime)/(1000*60*60));

        } catch(Exception e) {
//            e.printStackTrace();
        }
    }

}


