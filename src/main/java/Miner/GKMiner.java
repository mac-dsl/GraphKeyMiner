package Miner;

import Dependency.DependencyGraph;
import Infra.*;
import Lattice.Lattice;
import Summary.SummaryGraph;
import Util.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class GKMiner {

    private VF2DataGraph dataGraph;
    private double delta;
    private int k;
    private String type;
    private Lattice lattice;
    private HashMap<String, ArrayList<CandidateGKey>> allGKeys;
    private SummaryGraph summaryGraph;
    private DependencyGraph dependencyGraph;

    public GKMiner(VF2DataGraph dataGraph, SummaryGraph summaryGraph, DependencyGraph dependencyGraph, String type, double delta, int k)
    {
        this.dataGraph=dataGraph;
        this.delta=delta;
        this.k=k;
        this.type=type;
        this.summaryGraph=summaryGraph;
        this.allGKeys=new HashMap<>();
        this.lattice=new Lattice(summaryGraph,type,delta,k);
        this.dependencyGraph = dependencyGraph;
        lattice.createLattice();
    }

    public void mine()
    {
        CandidateGKey gkey=lattice.next();
        while (gkey!=null)
        {
            if(isAGraphkey(gkey))
            {
                if(!allGKeys.containsKey(type))
                    allGKeys.put(type, new ArrayList<>());
                allGKeys.get(type).add(gkey);
                lattice.prune(gkey);
            }
            gkey=lattice.next();
        }
    }

    public HashMap<String, ArrayList<CandidateGKey>> getAllGKeys() {
        return allGKeys;
    }

    private boolean isAGraphkey(CandidateGKey gkey)
    {
        boolean isGKey=true;

        if(gkey.getDependantTypes().isEmpty()) // Constant GKey
        {
            HashSet<String> attributeNames = gkey
                    .getAttributes()
                    .stream()
                    .map(CandidateNode::getNodeName)
                    .collect(Collectors.toCollection(HashSet::new));
            HashMap<String, HashSet<String>> valueMap=new HashMap<>();
            double numberOfInducedNodes=0;
            for (DataVertex v:dataGraph.getGraph().vertexSet()) {
                if(v.getTypes().contains(gkey.getMainType()) && v.isInduced(gkey.getAttributes()))
                {
                    numberOfInducedNodes++;
                    if(!v.isUnique(attributeNames))
                    {
                        String var=v.valueOf(attributeNames);
                        if(!valueMap.containsKey(var))
                            valueMap.put(var,new HashSet<>());
                        valueMap.get(var).add(v.getVertexURI());
                    }
                }
            }
            if((numberOfInducedNodes/summaryGraph.getSummaryVertex(gkey.getMainType()).getCount())<delta)
            {
                lattice.prune(gkey);
                return false;
            }
            for (String var:valueMap.keySet()) {
                if(valueMap.get(var).size()>1)
                    isGKey=false;
                else
                    dataGraph.getNode(valueMap.get(var).iterator().next()).addUniqueness(attributeNames);
            }
        }
        else // Variable GKey
        {
            for (CandidateNode node:gkey.getDependantTypes()) {
                if(!allGKeys.containsKey(node.getNodeName()))
                {
                    dependencyGraph.addEdge(type,node.getNodeName());
                    if(dependencyGraph.isCyclic())
                    {
                        dependencyGraph.removeEdge(type,node.getNodeName());
                        // Need to remove the dependency as well from the Lattice
                    }
                    else
                    {
                        GKMiner recursiveMiner=new GKMiner(dataGraph,summaryGraph,dependencyGraph,node.getNodeName(),delta, (k-gkey.getAttributes().size() - gkey.getDependantTypes().size()));
                        recursiveMiner.mine();
                        if(!allGKeys.containsKey(node.getNodeName()))
                        {
                            lattice.prune(gkey);
                            return false;
                        }
                    }
                }
            }
            HashSet<String> attributeNames = gkey
                    .getAttributes()
                    .stream()
                    .map(CandidateNode::getNodeName)
                    .collect(Collectors.toCollection(HashSet::new));
            HashSet<String> dependantTypes = gkey
                    .getDependantTypes()
                    .stream()
                    .map(CandidateNode::getNodeName)
                    .collect(Collectors.toCollection(HashSet::new));
            attributeNames.addAll(dependantTypes);
            HashMap<String, HashSet<String>> valueMap=new HashMap<>();
            double numberOfInducedNodes=0;
            for (DataVertex v:dataGraph.getGraph().vertexSet()) {
                if(v.getTypes().contains(gkey.getMainType()) && v.isInduced(gkey.getAttributes(), gkey.getDependantTypes(), dataGraph.getGraph().outgoingEdgesOf(v)))
                {
                    numberOfInducedNodes++;
                    if(!v.isUnique(attributeNames))
                    {
                        String var = v.valueOf(gkey.getDependantTypes(),dataGraph.getGraph().outgoingEdgesOf(v));
                        if(var.equals("-1"))
                        {
                            lattice.prune(gkey);
                            return false;
                        }
                        var+=v.valueOf(attributeNames);
                        if(!valueMap.containsKey(var))
                            valueMap.put(var,new HashSet<>());
                        valueMap.get(var).add(v.getVertexURI());
                    }
                }
            }
            if((numberOfInducedNodes/summaryGraph.getSummaryVertex(gkey.getMainType()).getCount())<delta)
            {
                lattice.prune(gkey);
                return false;
            }
            for (String var:valueMap.keySet()) {
                if(valueMap.get(var).size()>1)
                    isGKey=false;
                else
                    dataGraph.getNode(valueMap.get(var).iterator().next()).addUniqueness(attributeNames);
            }
            if(isGKey)
            {
                for (HashSet<String> vertexID:valueMap.values()) {
                    dataGraph.getNode(vertexID.iterator().next()).setEntityID(Config.entityID++);
                }
            }
        }
        return isGKey;
    }

}
