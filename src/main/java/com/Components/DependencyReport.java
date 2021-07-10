package com.Components;

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
                                                        .append(dependency).append(";")
                                                        .append(DependencyType.USED).append(";")
                                                        .append(dependency.getNumberOfImportedClasses())
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
                                                        .append(dependency).append(";")
                                                        .append(DependencyType.USED)
                                                        .append("\n");

            if(!this.reportString.toString().contains(candidateDependency))
            this.reportString.append(dependency.getBuildFile().toString()).append(";")
                    .append(dependency).append(";")
                    .append(DependencyType.UNUSED)
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


