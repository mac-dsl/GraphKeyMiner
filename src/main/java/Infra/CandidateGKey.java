package Infra;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CandidateGKey {

    private String mainType;
    private HashSet<String> allNodeNames;
    private ArrayList<CandidateNode> attributes;
    private ArrayList<CandidateNode> dependantTypes;
    private boolean prune=false;

    public CandidateGKey(String mainType, List<CandidateNode> nodes)
    {
        this.mainType=mainType;
        this.attributes=new ArrayList<>();
        this.dependantTypes=new ArrayList<>();
        allNodeNames =new HashSet<>();
        for (CandidateNode node:nodes) {
            if(node.getCandidateType()==CandidateType.ConstantNode)
            {
                attributes.add(node);
                allNodeNames.add(node.getNodeName());
            }
            else if(node.getCandidateType()==CandidateType.VariableNode)
            {
                dependantTypes.add(node);
                allNodeNames.add(node.getNodeName());
            }
        }
        prune=false;
    }

    public ArrayList<CandidateNode> getAttributes() {
        return attributes;
    }

    public HashSet<String> getAllNodeNames() {
        return allNodeNames;
    }

    public boolean checkToBePruned(CandidateGKey prunedGKey)
    {
        if(allNodeNames.containsAll(prunedGKey.getAllNodeNames()))
        {
            prune = true;
            return true;
        }
        else
            return false;
    }

    public ArrayList<CandidateNode> getDependantTypes() {
        return dependantTypes;
    }

    public String getMainType() {
        return mainType;
    }

    public void setPrune(boolean prune) {
        this.prune = prune;
    }

    public boolean isPrune() {
        return prune;
    }
}
