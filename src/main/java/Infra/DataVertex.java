package Infra;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DataVertex extends Vertex implements Serializable {

    private String vertexURI="";
    private HashSet<HashSet<String>> uniqueness;
    private int entityID=-1;

    public DataVertex(String uri, String type) {
        super(type.toLowerCase());
        this.vertexURI=uri.toLowerCase();
        //this.addAttribute("uri",vertexURI);
        uniqueness =new HashSet<>();
    }

    public boolean isInduced(ArrayList<CandidateNode> attributeNodes)
    {
        return attributeNodes
                .stream()
                .allMatch(node -> this.hasAttribute(node.getNodeName()));
    }

    public boolean isInduced(ArrayList<CandidateNode> attributeNodes, ArrayList<CandidateNode> dependantTypes, Set<RelationshipEdge> edges)
    {
        boolean res = attributeNodes
                .stream()
                .allMatch(node -> this.hasAttribute(node.getNodeName()));
        if(!res)
            return false;
        boolean exists;
        for (CandidateNode node : dependantTypes) {
            exists = false;
            for (RelationshipEdge e : edges) {
                if (e.getLabel().equals(node.getEdgeName()) && e.getTarget().getTypes().contains(node.getNodeName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists)
                return false;
        }
        return true;
    }

    public void hardReset()
    {
        uniqueness.removeIf(unique -> unique.size() > 1);
        this.entityID = -1;
    }

    public void softReset()
    {
        this.entityID = -1;
    }

    public boolean isUnique(HashSet<String> attributeNames)
    {
        return uniqueness
                .stream()
                .anyMatch(attributeNames::containsAll);
    }

    public String valueOf(ArrayList<CandidateNode> dependantTypes, Set<RelationshipEdge> edges)
    {
        String res="";
        for (CandidateNode node : dependantTypes) {
            for (RelationshipEdge e : edges) {
                if (e.getLabel().equals(node.getEdgeName()) && e.getTarget().getTypes().contains(node.getNodeName())) {
                    if(((DataVertex)e.getTarget()).getEntityID()!=-1)
                    {
                        res+=((DataVertex)e.getTarget()).getEntityID();
                    }
                    else
                        return "-1";
                    break;
                }
            }
        }
        return res;
    }

    public String valueOf(HashSet<String> attributeNames)
    {
        return attributeNames
                .stream()
                .map(this::getAttributeValueByName)
                .collect(Collectors.joining());
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public void addUniqueness(String attributeName)
    {
        HashSet<String> var=new HashSet<>();
        var.add(attributeName);
        this.uniqueness.add(var);
    }

    public void addUniqueness(HashSet<String> attributeNames)
    {
        this.uniqueness.add(attributeNames);
    }

    public String getVertexURI() {
        return vertexURI;
    }

    @Override
    public String toString() {
        return "vertex{" +
                "type='" + getTypes() + '\'' +
                ", attributes=" + super.getAllAttributesList() +
                '}';
    }


    @Override
    public boolean isMapped(Vertex v) {
        if(v instanceof DataVertex)
            return false;
        if (super.getTypes().containsAll(v.getTypes())) {
            if (super.getAllAttributesNames().containsAll(v.getAllAttributesNames())) {
                for (Attribute attr : v.getAllAttributesList())
                    if (!super.getAttributeValueByName(attr.getAttrName()).equals(attr.getAttrValue())) {
                        return false;
                    }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(@NotNull Vertex o) {
        if(o instanceof DataVertex)
        {
            DataVertex v=(DataVertex) o;
            return this.vertexURI.compareTo(v.vertexURI);
        }
        else
            return 0;
    }
}
