import org.apache.maven.model.Dependency;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static antlr.Utils.loadClass;

public class DependencyAnalyzer {

    Project project;
    ArrayList<Class> importedClasses = new ArrayList<>();

    public DependencyAnalyzer(Project project){
        this.project = project;
    }


    public ArrayList<Class> getImportedPackageFromClass(String className){

        this.determineUsedDependenciesFromImportedClasses();
        return this.importedClasses;
    }

    public void determineUsedDependenciesFromImportedClasses(){
        for(String className : this.project.getImports()){
            if(this.importedClasses.isEmpty()){
                try{
                    Class importedClass = loadClass(className);
                    this.determineDependencyOfClass(importedClass);
                }catch (ClassNotFoundException e){
                    //todo resolve issue when class is not in any of the classpath
                    System.out.println("Could not find package: " + className);
                    e.printStackTrace();
                }
            }
        }
    }

    public void determineUnusedDependenciesFromImportedClasses(){
        if(this.project.getImportedDependencies().isEmpty()){
            this.determineUsedDependenciesFromImportedClasses();
        }

        for(Dependency dependency : this.project.getDependencies()){
            if(!this.project.getImportedDependencies().contains(dependency)){
                this.project.addNotImportedDependency(dependency);
            }
        }

    }

    private void determineDependencyOfClass(Class importedClass){

        boolean dependencyFound = false;

        for(Dependency dependency : this.project.getDependencies()){
            if(importedClass.getPackage().getName().contains(dependency.getGroupId())
                    && !dependencyFound && !this.project.getImportedDependencies().contains(dependency)){
                dependencyFound = true;
                this.project.addImportedDependency(dependency);
            } else{
                System.out.println("Package name: " + importedClass.getPackage().getName() + " " +dependency.getGroupId() + ": groupId");

            }
        }
    }


}
