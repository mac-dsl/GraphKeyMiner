package Infra;

import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.Serializable;
import java.util.*;

public class VF2DataGraph implements Serializable {

    private Graph<DataVertex, RelationshipEdge> graph = new DefaultDirectedGraph<>(RelationshipEdge.class);

    private HashMap<String, DataVertex> nodeMap;

    public VF2DataGraph()
    {
        nodeMap= new HashMap<>();
    }

    public VF2DataGraph(Graph <DataVertex, RelationshipEdge> graph)
    {
        nodeMap= new HashMap<>();
        this.graph = graph;
        for (Vertex v:graph.vertexSet()) {
            DataVertex dataV=(DataVertex) v;
            if(!nodeMap.containsKey(dataV.getVertexURI())) {
                nodeMap.put(dataV.getVertexURI(), dataV);
            }
        }
    }

    public Graph<DataVertex, RelationshipEdge> getGraph() {
        return graph;
    }

    public void addVertex(DataVertex v)
    {
        if(!nodeMap.containsKey(v.getVertexURI()))
        {
            graph.addVertex(v);
            nodeMap.put(v.getVertexURI(),v);
        }
    }

    public DataVertex getNode(String vertexURI)
    {
        return nodeMap.getOrDefault(vertexURI, null);
    }

    public void addEdge(DataVertex v1, DataVertex v2, RelationshipEdge edge)
    {
        graph.addEdge(v1,v2,edge);
    }

    public void hardResetGraph()
    {
        this.getGraph().vertexSet().forEach(DataVertex::hardReset);
    }

    public void softResetGraph()
    {
        this.getGraph().vertexSet().forEach(DataVertex::softReset);
    }

    public void removeEdge(DataVertex v1, DataVertex v2, RelationshipEdge edge)
    {
        for (RelationshipEdge e:graph.outgoingEdgesOf(v1)) {
            DataVertex target=(DataVertex) e.getTarget();
            if(target.getVertexURI().equals(v2.getVertexURI()) && edge.getLabel().equals(e.getLabel()))
            {
                this.graph.removeEdge(e);
                return;
            }
        }
    }

    public int getSize()
    {
        return nodeMap.size();
    }

    public HashMap<String, DataVertex> getNodeMap() {
        return nodeMap;
    }

    public Graph<Vertex, RelationshipEdge> getSubGraphWithinDiameter(@NotNull DataVertex center, int diameter)
    {
        Graph<Vertex, RelationshipEdge> subgraph = new DefaultDirectedGraph<>(RelationshipEdge.class);

        List<DataVertex> withinDiameter=new ArrayList<>();

        // Define a HashMap to store visited vertices
        HashMap<String,Integer> visited=new HashMap<>();

        // Create a queue for BFS
        LinkedList<DataVertex> queue = new LinkedList<>();

        // Mark the current node as visited with distance 0 and then enqueue it
        visited.put(center.getVertexURI(),0);
        queue.add(center);
        // Store the center as the node within the diameter
        withinDiameter.add(center);
        //temp variables
        DataVertex v,w;

        while (queue.size() != 0)
        {
            // Dequeue a vertex from queue and get its distance
            v = queue.poll();
            int distance=visited.get(v.getVertexURI());

            // Outgoing edges
            for (RelationshipEdge edge : graph.outgoingEdgesOf(v)) {
                w = (DataVertex) edge.getTarget();

                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {

                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {

                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        withinDiameter.add(w);
                    }

                }
            }
            // Incoming edges
            for (RelationshipEdge edge : graph.incomingEdgesOf(v)) {
                w = (DataVertex) edge.getSource();

                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {

                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {

                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        withinDiameter.add(w);
                    }

                }
            }
        }
        for (Vertex vertex:withinDiameter) {
            subgraph.addVertex(vertex);
        }
        for (DataVertex source:withinDiameter) {
            for (RelationshipEdge e:graph.outgoingEdgesOf(source)) {
                DataVertex target=(DataVertex)e.getTarget();
                if(visited.containsKey(target.getVertexURI()))
                    subgraph.addEdge(e.getSource(),e.getTarget(),e);
            }
        }
        return subgraph;
    }

    public ArrayList<RelationshipEdge> getEdgesWithinDiameter(@NotNull DataVertex center, int diameter)
    {
        ArrayList<RelationshipEdge> edges = new ArrayList<>();

        // Define a HashMap to store visited vertices
        HashMap<String,Integer> visited=new HashMap<>();

        // Create a queue for BFS
        LinkedList<DataVertex> queue = new LinkedList<>();

        // Mark the current node as visited with distance 0 and then enqueue it
        visited.put(center.getVertexURI(),0);
        queue.add(center);

        //temp variables
        DataVertex v,w;

        while (queue.size() != 0)
        {
            // Dequeue a vertex from queue and get its distance
            v = queue.poll();
            int distance=visited.get(v.getVertexURI());

            // Outgoing edges
            for (RelationshipEdge edge : graph.outgoingEdgesOf(v)) {
                w = (DataVertex) edge.getTarget();

                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {

                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {
                        edges.add(edge);
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        //withinDiameter.add(w);
                    }

                }
            }
            // Incoming edges
            for (RelationshipEdge edge : graph.incomingEdgesOf(v)) {
                w = (DataVertex) edge.getSource();

                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {

                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {
                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        edges.add(edge);
                        //withinDiameter.add(w);
                    }

                }
            }
        }
        return edges;
    }

    public int getSubGraphSize(@NotNull DataVertex center, int diameter)
    {
        int size=0;

        List<DataVertex> withinDiameter=new ArrayList<>();

        // Define a HashMap to store visited vertices
        HashMap<String,Integer> visited=new HashMap<>();

        // Create a queue for BFS
        LinkedList<DataVertex> queue = new LinkedList<>();
        // Mark the current node as visited with distance 0 and then enqueue it
        visited.put(center.getVertexURI(),0);
        queue.add(center);
        // Store the center as the node within the diameter
        withinDiameter.add(center);
        //temp variables
        DataVertex v,w;
        while (queue.size() != 0)
        {
            // Dequeue a vertex from queue and get its distance
            v = queue.poll();
            int distance=visited.get(v.getVertexURI());
            // Outgoing edges
            for (RelationshipEdge edge : graph.outgoingEdgesOf(v)) {
                w = (DataVertex) edge.getTarget();
                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {
                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {
                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        withinDiameter.add(w);
                    }
                }
            }
            // Incoming edges
            for (RelationshipEdge edge : graph.incomingEdgesOf(v)) {
                w = (DataVertex) edge.getSource();
                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {
                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {
                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        withinDiameter.add(w);
                    }

                }
            }
        }
        for (DataVertex source : withinDiameter) {
            for (RelationshipEdge e : graph.outgoingEdgesOf(source)) {
                DataVertex target = (DataVertex) e.getTarget();
                if (visited.containsKey(target.getVertexURI()))
                    size++;
            }
        }
        return size;
    }

    public List<Vertex> getVerticesWithinDiameter(DataVertex center, int diameter)
    {
        List<Vertex> withinDiameter=new ArrayList<>();

        // Define a HashMap to store visited vertices
        HashMap<String,Integer> visited=new HashMap<>();

        // Create a queue for BFS
        LinkedList<DataVertex> queue = new LinkedList<>();

        // Mark the current node as visited with distance 0 and then enqueue it
        visited.put(center.getVertexURI(),0);
        queue.add(center);
        // Store the center as the node within the diameter
        withinDiameter.add(center);
        //temp variables
        DataVertex v,w;

        while (queue.size() != 0)
        {
            // Dequeue a vertex from queue and get its distance
            v = queue.poll();
            int distance=visited.get(v.getVertexURI());

            // Outgoing edges
            for (RelationshipEdge edge : graph.outgoingEdgesOf(v)) {
                w = (DataVertex) edge.getTarget();

                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {

                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {

                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        withinDiameter.add(w);
                    }

                }
            }
            // Incoming edges
            for (RelationshipEdge edge : graph.incomingEdgesOf(v)) {
                w = (DataVertex) edge.getSource();

                // Check if the vertex is not visited
                if (!visited.containsKey(w.getVertexURI())) {

                    // Check if the vertex is within the diameter
                    if (distance + 1 <= diameter) {

                        //Enqueue the vertex and add it to the visited set
                        visited.put(w.getVertexURI(), distance + 1);
                        queue.add(w);
                        withinDiameter.add(w);
                    }

                }
            }
        }
        return withinDiameter;
    }

    public void updateGraphByAttribute(DataVertex v1, Attribute attribute)
    {
        nodeMap.get(v1.getVertexURI()).setOrAddAttribute(attribute);
    }

    private boolean isValidType(Set<String> validTypes, Set<String> givenTypes)
    {
        return givenTypes.stream().anyMatch(validTypes::contains);
    }

}
