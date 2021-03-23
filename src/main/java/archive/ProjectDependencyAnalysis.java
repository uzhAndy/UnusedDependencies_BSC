//import org.apache.maven.artifact.Artifact;
//
//import java.util.Collections;
//import java.util.LinkedHashSet;
//import java.util.Set;
//
//public class ProjectDependencyAnalysis {
//
//    private final Set<Artifact> usedDeclaredArtifacts;
//    private final Set<Artifact> unusedDeclaredArtifacts;
//
//    public ProjectDependencyAnalysis(Set<Artifact> usedDeclaredArtifacts, Set<Artifact> unusedDeclaredArtifacts){
//        this.usedDeclaredArtifacts = usedDeclaredArtifacts;
//        this.unusedDeclaredArtifacts = unusedDeclaredArtifacts;
//    }
//
//    private Set<Artifact> safeCopy(Set<Artifact> artifactSet){
//        return (artifactSet == null) ? Collections.emptySet() :
//                Collections.unmodifiableSet(new LinkedHashSet<>(artifactSet));
//    }
//}
