package Summary;

import Infra.*;
import Util.Config;
import Util.Helper;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;
import java.util.HashSet;

public class SummaryGraph {

    private VF2DataGraph dataGraph;
    private Graph<SummaryVertex, RelationshipEdge> summaryGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);

    private HashMap<String, SummaryVertex> nodeMap;

    public SummaryGraph(VF2DataGraph dataGraph)
    {
        this.nodeMap=new HashMap<>();
        this.dataGraph=dataGraph;
    }

    public void summary()
    {
        Helper.setTemporaryTimer();
        addVertices();
        Helper.printWithTime("Summary Graph (add vertices time): ");
        Helper.setTemporaryTimer();
        addEdges();
        Helper.printWithTime("Summary Graph (add edges time): ");
        Helper.setTemporaryTimer();
        addAttributes();
        Helper.printWithTime("Summary Graph (add attributes time): ");

    }

    public void findUniqueness()
    {
        Helper.setTemporaryTimer();
        findUniquenessOfAttributes();
        Helper.printWithTime("Summary Graph (fine uniqueness time): ");
    }

    public Graph<SummaryVertex, RelationshipEdge> getSummaryGraph() {
        return summaryGraph;
    }

    public SummaryVertex getSummaryVertex(String type)
    {
        return nodeMap.getOrDefault(type,null);
    }

    public void saveToFile()
    {
        StringBuilder sb=new StringBuilder();
        for (SummaryVertex v:nodeMap.values()) {
            sb.append("v," + v.getId() + "," + v.getType() + "," + v.getCount() + ",");
            for (Attribute attribute:v.getAllAttributesList()) {
                sb.append(attribute.getAttrName() + "," + attribute.getCount() + ",");
            }
            if(sb. length() > 0)
                sb. deleteCharAt(sb. length() - 1);
            sb.append("\n");
        }
        summaryGraph.edgeSet()
                .stream()
                .map(edge -> "e,"
                        + ((SummaryVertex) edge.getSource()).getId() + ","
                        + ((SummaryVertex) edge.getTarget()).getId() + ","
                        + edge.getCount() + "\n")
                .forEach(sb::append);
        Helper.saveToFile("summaryGraph",sb);
    }

    private void addVertices()
    {
        int id=1;
        for (DataVertex v:dataGraph.getGraph().vertexSet()) {
            for (String type: v.getTypes()) {
                if(!nodeMap.containsKey(type))
                {
                    SummaryVertex summaryVertex=new SummaryVertex(id++,type);
                    summaryVertex.incrementCount();
                    summaryGraph.addVertex(summaryVertex);
                    nodeMap.put(type,summaryVertex);
                }
                else
                {
                    nodeMap.get(type).incrementCount();
                }
            }
        }
        if(Config.debug)
            System.out.println("Number of nodes in the Summary Graph: " + summaryGraph.vertexSet().size());
    }

    private void addAttributes()
    {
        for (DataVertex v:dataGraph.getGraph().vertexSet()) {
            for (String type: v.getTypes()) {
                SummaryVertex summaryVertex = nodeMap.get(type);
                for (Attribute attr: v.getAllAttributesList()) {
                    Attribute summaryAttr = summaryVertex.getAttributeByName(attr.getAttrName());
                    if(summaryAttr!=null)
                    {
                        summaryAttr.incrementCount();
                    }
                    else
                    {
                        Attribute attribute = new Attribute(attr.getAttrName(),1);
                        summaryVertex.addAttribute(attribute);
                    }
                }
            }
        }
    }

    private void findUniquenessOfAttributes()
    {
        for (String type:nodeMap.keySet()) {
            HashMap<String,HashMap<String, HashSet<String>>> valueMap=new HashMap<>();
            for (DataVertex v:dataGraph.getGraph().vertexSet()) {
                if (v.getTypes().contains(type)) {
                    for (Attribute attr: v.getAllAttributesList()) {
                        if(!valueMap.containsKey(attr.getAttrName()))
                            valueMap.put(attr.getAttrName(),new HashMap<>());
                        if(!valueMap.get(attr.getAttrName()).containsKey(attr.getAttrValue()))
                            valueMap.get(attr.getAttrName()).put(attr.getAttrValue(), new HashSet<>());
                        valueMap.get(attr.getAttrName()).get(attr.getAttrValue()).add(v.getVertexURI());
                    }
                }
            }
//            for (String attributeName:valueMap.keySet()) {
//                for (String attributeValue:valueMap.get(attributeName).keySet()) {
//                    if(valueMap.get(attributeName).get(attributeValue).size()==1)
//                    {
//                        dataGraph.getNode(valueMap.get(attributeName).get(attributeValue).iterator().next()).addUniqueness(attributeName);
//                    }
//                }
//            }
            for (String attributeName:valueMap.keySet()) {
                valueMap
                        .get(attributeName)
                        .keySet()
                        .stream()
                        .filter(attributeValue -> valueMap.get(attributeName).get(attributeValue).size() == 1)
                        .forEach(attributeValue -> dataGraph
                                .getNode(valueMap.get(attributeName).get(attributeValue).iterator().next())
                                .addUniqueness(attributeName));
            }
        }
    }

    private void addEdges()
    {
        for (RelationshipEdge e:dataGraph.getGraph().edgeSet())
        {
            DataVertex src=(DataVertex) e.getSource();
            DataVertex dst=(DataVertex) e.getTarget();
            for (String srcType:src.getTypes())
            {
                SummaryVertex summarySrc = nodeMap.get(srcType);
                for (String dstType:dst.getTypes())
                {
                    SummaryVertex summaryDst = nodeMap.get(dstType);
                    boolean edgeExist=false;
                    for (RelationshipEdge summaryEdge:summaryGraph.outgoingEdgesOf(summarySrc))
                    {
                        if(summaryEdge.getLabel().equals(e.getLabel()) && summaryEdge.getTarget().isMapped(summaryDst))
                        {
                            summaryEdge.incrementCount();
                            edgeExist=true;
                        }
                    }
                    if(!edgeExist)
                    {
                        summaryGraph.addEdge(summarySrc, summaryDst, new RelationshipEdge(e.getLabel()));
                    }
                }
            }
        }
        System.out.println("Done. Nodes: " + summaryGraph.vertexSet().size() + ",  Edges: " +summaryGraph.edgeSet().size());
    }
}
