package Infra;

import java.io.Serializable;

public class Attribute implements Comparable<Attribute>, Serializable {

    private String attrName;
    private String attrValue;
    private double count;

    public Attribute(String attrName, String attrValue)
    {
        this.attrName=attrName.toLowerCase();
        this.attrValue=attrValue.toLowerCase();
    }

    public Attribute(String attrName, int count)
    {
        this.attrName=attrName.toLowerCase();
        this.attrValue=null;
        this.count=count;
    }

    public Attribute(String attrName)
    {
        this.attrName=attrName.toLowerCase();
        this.attrValue=null;
        this.count=1;
    }

    @Override
    public String toString() {
            return "(" +
                    "'" + attrName + '\'' +
                    ", '" + attrValue + '\'' +
                    ", '" + count + '\'' +
                    ')';
    }

    public String getAttrName() {
        return attrName;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public double getCount() {
        return count;
    }

    public void incrementCount()
    {
        count++;
    }

    public void setAttrName(String attrName) { this.attrName = attrName.toLowerCase();}


    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue.toLowerCase();
    }

    @Override
    public int compareTo(Attribute o) {
        return this.attrName.compareTo(o.attrName);
    }
}
