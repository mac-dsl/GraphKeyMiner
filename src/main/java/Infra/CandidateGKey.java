package Infra;

import java.util.ArrayList;
import java.util.List;

public class CandidateGKey {

    private int id;
    private String mainType;
    private ArrayList<String> attributes;
    private ArrayList<String> dependantTypes;

    public CandidateGKey(int id, String mainType, ArrayList<String> attributes, ArrayList<String> dependantTypes)
    {
        this.id=id;
        this.mainType=mainType;
        this.attributes=attributes;
        this.dependantTypes=dependantTypes;
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }

    public ArrayList<String> getDependantTypes() {
        return dependantTypes;
    }

    public String getMainType() {
        return mainType;
    }

    public int getId() {
        return id;
    }
}
