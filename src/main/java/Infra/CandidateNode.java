package Infra;

public class CandidateNode {

    private String nodeName;
    private String edgeName;
    private CandidateType candidateType;

    public CandidateNode(String nodeName, String edgeName, CandidateType type)
    {
        this.nodeName =nodeName;
        this.edgeName=edgeName;
        this.candidateType =type;
    }

    public CandidateType getCandidateType() {
        return candidateType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getEdgeName() {
        return edgeName;
    }

    @Override
    public String toString() {
        if(candidateType.equals(CandidateType.ConstantNode))
            return "Node{" +
                    "AttributeName='" + nodeName + '\'' +
                    '}';
        else
            return "Node{" +
                    "NodeType='" + nodeName + '\'' +
                    ", EdgeName='" + edgeName + '\'' +
                    '}';
    }
}
