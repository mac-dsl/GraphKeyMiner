package Summary;

import Infra.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashMap;

public class SummaryGraph {

    VF2DataGraph dataGraph;
    private Graph<SummaryVertex, RelationshipEdge> summaryGraph = new DefaultDirectedGraph<>(RelationshipEdge.class);

    private HashMap<String, SummaryVertex> nodeMap;

    public void SummaryGraph(VF2DataGraph dataGraph)
    {
        this.nodeMap=new HashMap<>();
        this.dataGraph=dataGraph;
    }


    public void summary()
    {
        addVertices();
        addEdges();
        addAttributes();
    }

    public Graph<SummaryVertex, RelationshipEdge> getSummaryGraph() {
        return summaryGraph;
    }

    public SummaryVertex getSummaryVertex(String type)
    {
        return nodeMap.getOrDefault(type,null);
    }

    private void addVertices()
    {
        int id=1;
        for (Vertex v:dataGraph.getGraph().vertexSet()) {
            DataVertex d_v=(DataVertex) v;
            for (String type:d_v.getTypes()) {
                if(!nodeMap.containsKey(type))
                {
                    SummaryVertex summaryVertex=new SummaryVertex(id++,type);
                    summaryVertex.incrementCount();
                    summaryGraph.addVertex(summaryVertex);
                }
                else
                {
                    nodeMap.get(type).incrementCount();
                }
            }
        }
    }

    private void addAttributes()
    {
        for (Vertex v:dataGraph.getGraph().vertexSet()) {
            DataVertex d_v=(DataVertex) v;
            for (String type:d_v.getTypes()) {
                SummaryVertex summaryVertex = nodeMap.get(type);
                for (Attribute attr:d_v.getAllAttributesList()) {
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
    }
}
