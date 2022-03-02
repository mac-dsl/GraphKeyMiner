package Lattice;

import Infra.*;
import Summary.SummaryGraph;
import org.paukov.combinatorics3.Generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lattice {

    private SummaryGraph summaryGraph;
    private String type;
    private ArrayList<CandidateNode> availableCandidates;
    private double delta;
    private int k;

    private int level=1,index=0,maxLevel=1;

    private HashMap<Integer, List<CandidateGKey>> allCandidates;

    public Lattice(SummaryGraph summaryGraph, String type, double delta, int k)
    {
        this.summaryGraph=summaryGraph;
        this.delta=delta;
        this.type=type;
        this.k=k;
        allCandidates=new HashMap<>();
        availableCandidates = new ArrayList<>();
    }

    public void createLattice()
    {
        SummaryVertex main=summaryGraph.getSummaryVertex(type);
        findAvailableAttributesAndTypes(main);
        createCombinations();
    }

    public CandidateGKey next()
    {
        if(!allCandidates.containsKey(level))
            return null;
        if(index>=allCandidates.get(level).size()) {
            level++;
            index=0;
            if(!allCandidates.containsKey(level))
                return null;
            else
                return allCandidates.get(level).get(index++);
        }
        else
            return allCandidates.get(level).get(index++);

    }

    public int prune(CandidateGKey gkey)
    {
        int numberOfPrunedGKeys=0;
        int currentLevel=level+1;
        while (currentLevel<=maxLevel)
        {
            for (CandidateGKey candidate:allCandidates.get(currentLevel)) {
                if(candidate.checkToBePruned(gkey))
                    numberOfPrunedGKeys++;
            }
            currentLevel++;
        }
        return numberOfPrunedGKeys;
    }

    private void createCombinations()
    {
        maxLevel = availableCandidates.size();
        if(maxLevel>k)
            maxLevel=k;
        for (int i=1;i<=maxLevel;i++)
        {
            allCandidates.put(i,new ArrayList<>());
            int finalI = i;
            Generator.combination(availableCandidates)
                    .simple(i)
                    .stream()
                    .forEach(elem -> allCandidates.get(finalI).add(new CandidateGKey(type,elem)));
        }
    }

    private void findAvailableAttributesAndTypes(SummaryVertex main)
    {
        if(main!=null)
        {
            for (Attribute attr:main.getAllAttributesList()) {
                if((attr.getCount()/main.getCount())>delta)
                {
                    availableCandidates.add(new CandidateNode(attr.getAttrName(),null,CandidateType.ConstantNode));
                }
            }
            for (RelationshipEdge e:summaryGraph.getSummaryGraph().outgoingEdgesOf(main)) {
                if((e.getCount()/((SummaryVertex)(e.getTarget())).getCount())>delta)
                {
                    availableCandidates.add(new CandidateNode(e.getTarget().getType(), e.getLabel(), CandidateType.VariableNode));
                }
            }
        }
    }

}
