package Lattice;

import Infra.Attribute;
import Infra.CandidateGKey;
import Infra.RelationshipEdge;
import Infra.SummaryVertex;
import Summary.SummaryGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Lattice {

    private SummaryGraph summaryGraph;
    private String type;
    private ArrayList<String> availableAttributes;
    private ArrayList<String> availableDependantTypes;
    private double delta;

    private HashMap<Integer, HashSet<CandidateGKey>> allCandidates;

    public Lattice(SummaryGraph summaryGraph, String type, double delta)
    {
        this.summaryGraph=summaryGraph;
        this.delta=delta;
        this.type=type;
        allCandidates=new HashMap<>();
    }

    public void createLattice()
    {
        SummaryVertex main=summaryGraph.getSummaryVertex(type);
        if(main!=null)
        {
            for (Attribute attr:main.getAllAttributesList()) {
                if((attr.getCount()/main.getCount())>delta)
                {
                    availableAttributes.add(attr.getAttrName());
                }
            }
            for (RelationshipEdge e:summaryGraph.getSummaryGraph().outgoingEdgesOf(main)) {
                if((e.getCount()/((SummaryVertex)(e.getTarget())).getCount())>delta)
                {
                    availableDependantTypes.add(e.getTarget().getType());
                }
            }
        }
    }

}
