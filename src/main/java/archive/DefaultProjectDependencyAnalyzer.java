package archive;

import archive.DefaultCallGraph;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.analyzer.*;
import org.apache.maven.shared.dependency.analyzer.asm.ASMDependencyAnalyzer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class DefaultProjectDependencyAnalyzer implements ProjectDependencyAnalyzer{

    private final boolean isIgnoredTest;

    private final ClassAnalyzer classAnalyzer = new DefaultClassAnalyzer();

    private final DependencyAnalyzer dependencyAnalyzer = new ASMDependencyAnalyzer();

    private final Map<Artifact, Set<String>> artifactUsedClassesMap = new HashMap<>();

    public DefaultProjectDependencyAnalyzer(boolean isIgnoredTest){
        this.isIgnoredTest = isIgnoredTest;
    }

    private Map<Artifact, Set<String>> artifactClassMap;
    private Set<Artifact> declaredDependencies;

    /**
     *
     * @param mavenProject is a open source GitHub project
     * @return list of used (declared and undeclared) + unused dependencies
     * @throws ProjectDependencyAnalyzerException
     */
    @Override
    public ProjectDependencyAnalysis analyze(MavenProject mavenProject) throws ProjectDependencyAnalyzerException {
        try{
            // get the classes of a dependency

            artifactClassMap = buildArtifactClassMap(mavenProject);
            declaredDependencies =  mavenProject.getDependencyArtifacts();

            buildProjectDependencyClasses(mavenProject);
            Set<String> projectClasses = new HashSet<>(DefaultCallGraph.getProjectVertices());
            buildProjectDependencyClasses(mavenProject);

            // search for the dependencies used by the project

            Set<Artifact> usedArtifacts = determineUsedArtifacts(
                    artifactClassMap,
                    DefaultCallGraph.referencedClassMembers(projectClasses)
            );

            // get the used and declared dependencies

            Set<Artifact> usedDeclaredArtifacts = new LinkedHashSet<>(declaredDependencies);
            usedDeclaredArtifacts.retainAll(usedArtifacts);

            // remove used and declared dependencies
            Set<Artifact> usedUndeclaredArtifacts = new LinkedHashSet<>(usedArtifacts);
            usedUndeclaredArtifacts = removeAll(usedUndeclaredArtifacts, declaredDependencies);

            // get unused declared dependencies
            Set<Artifact> unusedDeclaredDependencies = new LinkedHashSet<>(declaredDependencies);
            unusedDeclaredDependencies = removeAll(unusedDeclaredDependencies, usedArtifacts);

            return new ProjectDependencyAnalysis(usedDeclaredArtifacts,usedUndeclaredArtifacts, unusedDeclaredDependencies);
        } catch (IOException ioException){
            throw new ProjectDependencyAnalyzerException("Dependency analysis unsuccessfull", ioException);
        }
    }

    private Set<Artifact> removeAll(Set<Artifact> usedUndeclaredArtifacts, Set<Artifact> declaredDependencies) {
        Set<Artifact> results = new LinkedHashSet<>(usedUndeclaredArtifacts.size());
        for (Artifact artifact: usedUndeclaredArtifacts){
            boolean found = false;
            for (Artifact artifact1 : declaredDependencies){
                if (artifact.getDependencyConflictId().equals(artifact1)){
                    found = true;
                    break;
                }
            }
            if (!found) {
                results.add(artifact);
            }
        }
        return results;
    }

    /**
     *
     * @param mavenProject
     * @throws IOException
     */
    private void buildProjectDependencyClasses(MavenProject mavenProject) throws IOException{
        String outputDirectory = mavenProject.getBuild().getOutputDirectory();
        collectDependencyClasses(outputDirectory);
    }

    private Set<Artifact> determineUsedArtifacts(Map<Artifact, Set<String>> artifactClassMap,
                                                 Set<String> referencedClasses){
        Set<Artifact> usedArtifacts = new HashSet<>();

        // look for used members in each class in the dependency classes
        for (String class_ : referencedClasses){
            Artifact artifact = findArtifactForClassName(artifactClassMap, class_);
            if (artifact != null){
                if (!artifactUsedClassesMap.containsKey(artifact)){
                    artifactUsedClassesMap.put(artifact, new HashSet<>());
                }
                artifactUsedClassesMap.get(artifact).add(class_);
                usedArtifacts.add(artifact);
            }
        }
        return usedArtifacts;
    }

    private Artifact findArtifactForClassName(Map<Artifact, Set<String>> artifactClassMap, String class_) {
        for (Map.Entry<Artifact, Set<String>> entry: artifactClassMap.entrySet()){
            if (entry.getValue().contains(class_)){
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     *
     * @param path
     * @return
     * @throws IOException
     */
    private Set<String> collectDependencyClasses(String path) throws IOException{
        URL url = new File(path).toURI().toURL();
        return dependencyAnalyzer.analyze(url);
    }

    /**
     * might be not needed
     * @param mavenProject
     * @return
     * @throws IOException
     */
    private Map<Artifact, Set<String>> buildArtifactClassMap(MavenProject mavenProject) throws IOException{


        Map<Artifact, Set<String>> artifactClassMap = new LinkedHashMap<>();

        // get all direct dependencies of mavenProject
        Set<Artifact> dependencyArtifacts = mavenProject.getArtifacts();

        for (Artifact artifact : dependencyArtifacts){
            File file = artifact.getFile();

            if (file != null && file.getName().endsWith(".jar")){
                try (JarFile jarFile = new JarFile(file)){
                    Enumeration<JarEntry> jarEntries = jarFile.entries();
                    Set<String> classes = new HashSet<>();
                    while (jarEntries.hasMoreElements()){
                        String entry = jarEntries.nextElement().getName();
                        if (entry.endsWith(".class")){
                            String className = entry.replace('/', '-');
                            className = className.substring(0, className.length() - ".class".length());
                            classes.add(className);
                        }
                    }
                    artifactClassMap.put(artifact, classes);
                }
            }else if (file != null && file.isDirectory()){
                URL url = file.toURI().toURL();
                Set<String> classes = classAnalyzer.analyze(url);
                artifactClassMap.put(artifact, classes);
            }
        }
        return artifactClassMap;
    }
}
