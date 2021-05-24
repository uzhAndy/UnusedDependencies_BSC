package com.Components;

import org.apache.maven.model.Dependency;

import java.util.ArrayList;

public class DependencyReport {

    private StringBuilder reportString = new StringBuilder();
    private String fPath;
    private ArrayList<DependencyExtended> usedDependencies = new ArrayList<>();

    public DependencyReport(String rPath){
        this.fPath = rPath;
    }

    public String getRPath() {
        return fPath;
    }

    public void addUsedDependencies(ArrayList<DependencyExtended> dependencies){

        for(DependencyExtended dependency : dependencies){

            StringBuilder candidateDependency = new StringBuilder()
                                                        .append(dependency.getBuildFile().toString()).append(";")
                                                        .append(dependency.toString()).append(";")
                                                        .append(DependencyType.USED.toString())
                                                        .append("\n");

            if(!this.reportString.toString().contains(candidateDependency.toString()))
            {

                this.usedDependencies.add(dependency);
                this.reportString.append(candidateDependency);
            }
        }
    }

    public void addUnusedDependencies(ArrayList<DependencyExtended> dependencies){
        for(DependencyExtended dependency: dependencies){

            StringBuilder candidateDependency = new StringBuilder()
                                                        .append(dependency.getBuildFile().toString()).append(";")
                                                        .append(dependency.toString()).append(";")
                                                        .append(DependencyType.USED.toString())
                                                        .append("\n");

            if(!this.reportString.toString().contains(candidateDependency))
            this.reportString.append(dependency.getBuildFile().toString()).append(";")
                    .append(dependency.toString()).append(";")
                    .append(DependencyType.UNUSED.toString())
                    .append("\n");
        }
    }

    public String getReport(){
        return this.reportString.toString();
    }
    enum DependencyType{
        USED, UNUSED;
    }
}


