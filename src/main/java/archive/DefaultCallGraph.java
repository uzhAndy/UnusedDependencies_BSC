package archive;

import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;

public class DefaultCallGraph {
    private static final AbstractBaseGraph<String , DefaultEdge> directedGraph =
            new DefaultDirectedGraph<>(DefaultEdge.class);
    private static final Set<String> projectVertices = new HashSet<>();
    private static final Map<String, Set<String>> usagesPerClass = new HashMap<>();

    public static void addEdge(String class_, Set<String> referencedClassMembers){
        directedGraph.addVertex(class_);
        for (String referencedClassMember : referencedClassMembers){
            if (!directedGraph.containsVertex(referencedClassMember)){
                directedGraph.addVertex(referencedClassMember);
            }
            directedGraph.addEdge(class_, referencedClassMember);
            projectVertices.add(class_);

            addReferencedClassMember(class_, referencedClassMember);
        }
    }

    public static Set<String> getProjectVertices(){
        return projectVertices;
    }

    public static void cleanDirectedGraph(){
        directedGraph.vertexSet().clear();
        directedGraph.edgeSet().clear();
    }

    public Map<String, Set<String>> getUsagesPerClass(){
        return usagesPerClass;
    }

    public static Set<String> referencedClassMembers(Set<String> projectClasses){
        Set<String> allReferencedClassMembers = new HashSet<>();
        for (String projectClass : projectClasses){
            allReferencedClassMembers.addAll(traverse(projectClass));
        }
        return allReferencedClassMembers;
    }

    private static void addReferencedClassMember(String class_, String referencedClassMember) {
        Set<String> s = usagesPerClass.computeIfAbsent(class_, k -> new HashSet<>());
        s.add(referencedClassMember);
    }

    private static Set<String> traverse(String start){
        Set<String> referencedClassMembers = new HashSet<>();
        Iterator<String> iterator = new DepthFirstIterator<>(directedGraph, start);

        while (iterator.hasNext()){
            referencedClassMembers.add(iterator.next());
        }
        return referencedClassMembers;
    }

}
