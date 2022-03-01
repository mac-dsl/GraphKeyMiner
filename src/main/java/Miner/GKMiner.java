package Miner;

import Infra.*;
import Lattice.Lattice;
import Summary.SummaryGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class GKMiner {

    private VF2DataGraph dataGraph;
    double delta;
    int k;
    private Lattice lattice;
    private ArrayList<CandidateGKey> allGKeys;
    private SummaryGraph summaryGraph;

    public GKMiner(VF2DataGraph dataGraph, SummaryGraph summaryGraph, String type, double delta, int k)
    {
        this.dataGraph=dataGraph;
        this.delta=delta;
        this.k=k;
        this.summaryGraph=summaryGraph;
        this.allGKeys=new ArrayList<>();
        this.lattice=new Lattice(summaryGraph,type,delta,k);
        lattice.createLattice();
    }

    public void mine()
    {
        CandidateGKey gkey=lattice.next();
        while (gkey!=null)
        {
            if(isGkey(gkey))
            {
                allGKeys.add(gkey);
                lattice.prune(gkey);
            }
            gkey=lattice.next();
        }
    }

    private boolean isGkey(CandidateGKey gkey)
    {
        boolean isGKey=true;

        HashSet<String> attributeNames = gkey
                .getAttributes()
                .stream()
                .map(CandidateNode::getName)
                .collect(Collectors.toCollection(HashSet::new));
        HashMap<String, HashSet<String>> valueMap=new HashMap<>();
        double numberOfInducedNodes=0;
        for (DataVertex v:dataGraph.getGraph().vertexSet()) {
            if(v.getTypes().contains(gkey.getMainType()) && v.isInduced(gkey))
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
        return isGKey;
    }

}
