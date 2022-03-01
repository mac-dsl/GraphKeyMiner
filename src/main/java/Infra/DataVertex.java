package Infra;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.stream.Collectors;

public class DataVertex extends Vertex implements Serializable {

    private String vertexURI="";
    private HashSet<HashSet<String>> uniqueness;

    public DataVertex(String uri, String type) {
        super(type.toLowerCase());
        this.vertexURI=uri.toLowerCase();
        this.addAttribute("uri",vertexURI);
        uniqueness =new HashSet<>();
    }

    public boolean isInduced(CandidateGKey gkey)
    {
        return gkey
                .getAttributes()
                .stream()
                .allMatch(node -> this.hasAttribute(node.getName()));
    }

    public boolean isUnique(HashSet<String> attributeNames)
    {
        return uniqueness
                .stream()
                .anyMatch(attributeNames::containsAll);
    }

    public String valueOf(HashSet<String> attributeNames)
    {
        String res= attributeNames
                .stream()
                .map(this::getAttributeValueByName)
                .collect(Collectors.joining());
        return res;
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
