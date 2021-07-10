package com.Components;

import org.apache.maven.model.Dependency;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class DependencyCandidate {

    private String artifactId;
    private String groupId;

    public DependencyCandidate(String groupId, String artifactId){
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    public DependencyCandidate(Dependency dependency){
        this.groupId = dependency.getGroupId();
        this.artifactId = dependency.getArtifactId();
    }

    public static ArrayList<DependencyCandidate> convertJSONToDependencyCandidates(JSONArray dependencies){

        ArrayList<DependencyCandidate> dependencyCandidates = new ArrayList<>();
        int i = 0;

        while(i<dependencies.size()){
            DependencyCandidate dependencyCandidate = new DependencyCandidate(
                    ((JSONObject) dependencies.get(i)).get("g").toString().replace("-", "."),
                    ((JSONObject) dependencies.get(i)).get("a").toString().replace("-", ".")
            );
            if(!dependencyCandidates.contains(dependencyCandidate)){
                dependencyCandidates.add(dependencyCandidate);
            }
            i++;
        }

        return dependencyCandidates;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public boolean equals(Object o){
            if(o == this) return true;
            DependencyCandidate dependency = (DependencyCandidate) o;
            return dependency.getArtifactId().equals(this.getArtifactId())
                    && dependency.getGroupId().equals(this.getGroupId());
    }
}
