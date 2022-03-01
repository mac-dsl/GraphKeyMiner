package Dependency;

import java.util.*;

public class DependencyGraph {

    private HashMap<Integer, List<Integer>> adj;
    private HashMap<String, Integer> nodes;
    private int id=0;

    public DependencyGraph()
    {
        adj = new HashMap<>();
        nodes = new HashMap<>();
    }

    public void addEdge(String source, String dest) {
        if(!nodes.containsKey(source))
            addNode(source);
        if(!nodes.containsKey(dest))
            addNode(dest);
        adj.get(nodes.get(source)).add(nodes.get(dest));
    }

    public void removeEdge(String source, String dest) {
        if(nodes.containsKey(source) && nodes.containsKey(dest))
        {
            adj.get(nodes.get(source)).removeIf(x -> Objects.equals(x, nodes.get(dest)));
        }

    }
    public boolean isCyclic()
    {
        boolean[] visited = new boolean[id];
        boolean[] recStack = new boolean[id];
        for (int i = 0; i < id; i++)
            if (isCyclicUtil(i, visited, recStack))
                return true;
        return false;
    }

    private boolean isCyclicUtil(int i, boolean[] visited, boolean[] recStack)
    {
        if (recStack[i])
            return true;
        if (visited[i])
            return false;
        visited[i] = true;

        recStack[i] = true;
        List<Integer> children = adj.get(i);

        for (Integer c: children)
            if (isCyclicUtil(c, visited, recStack))
                return true;

        recStack[i] = false;

        return false;
    }

    private void addNode(String nodeName)
    {
        if(!nodes.containsKey(nodeName))
        {
            nodes.put(nodeName, id);
            adj.put(id,new LinkedList<>());
            id++;
        }
    }

}
