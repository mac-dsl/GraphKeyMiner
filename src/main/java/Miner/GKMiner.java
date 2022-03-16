package Miner;

import Dependency.DependencyGraph;
import Infra.*;
import Lattice.Lattice;
import Summary.SummaryGraph;
import Util.Config;
import Util.Helper;

import java.util.*;
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
    private boolean originalType;

    public GKMiner(VF2DataGraph dataGraph, SummaryGraph summaryGraph, DependencyGraph dependencyGraph, HashMap<String, ArrayList<CandidateGKey>> allGKeys, String type, double delta, int k, boolean originalType)
    {
        if(Config.debug)
            System.out.println("GKMiner constructor, type:" + type  + ", k = " + k);
        this.dataGraph=dataGraph;
        this.delta=delta;
        this.k=k;
        this.type=type;
        this.summaryGraph=summaryGraph;
        this.allGKeys=allGKeys;
        this.dependencyGraph = dependencyGraph;
        this.originalType = originalType;

        Helper.setTemporaryTimer();
        this.lattice=new Lattice(summaryGraph,type,delta,k);
        lattice.createLattice();
        if(Config.debug)
            Helper.printWithTime("Miner (Lattice creation time): ");
    }

    public void mine()
    {
        CandidateGKey gkey=lattice.next();
        while (gkey!=null)
        {
            if(!gkey.isPruned() && isAGraphkey(gkey))
            {
                if(!allGKeys.containsKey(type))
                    allGKeys.put(type, new ArrayList<>());
                allGKeys.get(type).add(gkey);
                lattice.prune(gkey);
                if(!originalType)
                    break;
            }
            gkey=lattice.next();
        }
    }

    private boolean isAGraphkey(CandidateGKey gkey)
    {
        Helper.setTemporaryTimer();
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
                    if(!Config.optimize || !v.isUnique(attributeNames))
                    {
                        String var=v.valueOf(attributeNames);
                        if(!valueMap.containsKey(var))
                            valueMap.put(var,new HashSet<>());
                        valueMap.get(var).add(v.getVertexURI());
                    }
                }
            }
            gkey.setInducedEntities(numberOfInducedNodes);
            gkey.setTotalNumberOfMatches(summaryGraph.getSummaryVertex(gkey.getMainType()).getCount());
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
            HashSet<String> toBeRemoved=new HashSet<>();
            for (CandidateNode node:gkey.getDependantTypes()) {
                if(!allGKeys.containsKey(node.getNodeName()))
                {
                    dependencyGraph.addEdge(type,node.getNodeName());
                    if(type.equals(node.getNodeName()) || dependencyGraph.isCyclic())
                    {
                        dependencyGraph.removeEdge(type,node.getNodeName());
                        toBeRemoved.add(node.getNodeName());
                        // Need to remove the dependency as well from the Lattice
                    }
                    else
                    {
                        if(Config.debug)
                            System.out.println("Recursive call from '"+gkey.getMainType()+"' for '"+node.getNodeName()+"' - Current candidate: " + gkey);
                        GKMiner recursiveMiner=new GKMiner(dataGraph,summaryGraph,dependencyGraph,allGKeys,node.getNodeName(),delta, (k - gkey.getAttributes().size() - gkey.getDependantTypes().size()),false);
                        recursiveMiner.mine();
                        if(!allGKeys.containsKey(node.getNodeName()))
                        {
                            lattice.prune(gkey);
                            return false;
                        }
                    }
                }
            }
            gkey.getDependantTypes()
                    .removeIf(candidateNode -> toBeRemoved.contains(candidateNode.getNodeName()));

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
                    if(!Config.optimize || !v.isUnique(attributeNames))
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
        if(Config.debug)
            Helper.printWithTime("Miner.IsAGkey = "+isGKey+" for candidate ["+gkey.getMainType()+"] of size [" +(gkey.getDependantTypes().size() + gkey.getAttributes().size()) + "]), ");
        return isGKey;
    }
}
