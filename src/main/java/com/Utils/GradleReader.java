package com.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradleReader {

    private String[] rawDependencies;
    private ArrayList<Dependency> dependencies = new ArrayList<>();

    public GradleReader(BuildFile gradleFile){
        String contents = new String();
        try{
            contents = FileUtils.readFileToString(gradleFile, StandardCharsets.UTF_8);
        } catch (IOException e){
//            e.printStackTrace();
        System.out.println("IOException while reading gradle build file");

        }


        Pattern rawDependencies = Pattern.compile("dependencies \\{\\n((?:.*?|\\n)*?)}", Pattern.MULTILINE);
        Matcher matcher = rawDependencies.matcher(contents);

        if(matcher.find()){
            this.rawDependencies = matcher.group(1).split("\n");
            Pattern isolatedDependencies = Pattern.compile("\'(?:.*)\'");

            for(String rawDependency : this.rawDependencies){

                matcher = isolatedDependencies.matcher(rawDependency);

                if(matcher.find()){

//                    System.out.println(rawDependency);

                    String[] dependencyString = matcher.group(0).replace("\'", "").split(":");

                    Dependency dependency = new Dependency();
                    try{
                        dependency.setGroupId(dependencyString[0].replace("-", "."));
                        dependency.setArtifactId(dependencyString[1].replace("-", "."));
                        dependency.setVersion(dependencyString[2].replace("-", "."));
                        this.dependencies.add(dependency);
//                        System.out.println(dependency);
                    } catch (ArrayIndexOutOfBoundsException e){
//                        System.out.println(gradleFile.getAbsolutePath() + "------" + rawDependency + "------");
                    }
                }
            }
        }

    }

    public ArrayList<Dependency> getDependencies() {
        return this.dependencies;
    }
}
