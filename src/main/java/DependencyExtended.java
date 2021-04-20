import org.apache.maven.model.Dependency;


public class DependencyExtended extends Dependency {

    String groupId;
    String artifactId;
    String version;
    boolean isImported;
    boolean isUsed;

    public DependencyExtended(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setImported(boolean imported) {
        isImported = imported;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }


    public boolean isImported() {
        return isImported;
    }

    public boolean isUsed() {
        return isUsed;
    }

//    @Override
//    public Scope getScope() {
//        return scope;
//    }
}
