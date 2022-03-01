package Infra;

public class CandidateNode {

    private String name;
    private CandidateType type;

    public CandidateNode(String name, CandidateType type)
    {
        this.name=name;
        this.type=type;
    }

    public CandidateType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
