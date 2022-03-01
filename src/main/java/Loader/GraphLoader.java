package Loader;

import Infra.VF2DataGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for graph loaders
 * DBPedia and IMDB loaders extend this class
 */

public class GraphLoader {

    //region --[Fields: Protected]---------------------------------------

    /** size of the graph: #edges + #attributes */
    protected int graphSize=0;

    // Graph instance
    protected VF2DataGraph graph;

    // This will be used to filter the nodes that are not from the types in our TGFD lists
    // For example, if the tgfd is about "soccerplayer" and "team", we will only load the node types of "soccerplayer" and "team"
    // The filtering will be done if "properties.myProperties.optimizedLoadingBasedOnTGFD" set to TRUE
    protected Set <String> validTypes;

    // Same as validTypes, this will also be used to filter the attributes that are not from the types in our TGFD lists
    // The filtering will be done if "properties.myProperties.optimizedLoadingBasedOnTGFD" set to TRUE
    protected Set<String> validAttributes;

    //endregion

    //region --[Constructors]--------------------------------------------

    public GraphLoader()
    {
        graph=new VF2DataGraph();
        validTypes=new HashSet <>();
        validAttributes=new HashSet<>();
    }

    //endregion

    //region --[Properties: Public]--------------------------------------

    public VF2DataGraph getGraph() {
        return graph;
    }

    public void setGraph(VF2DataGraph graph) {
        this.graph = graph;
    }

    /**
     * @return Size of the graph
     */
    public int getGraphSize() {
        return graphSize;
    }

    //endregion

    //region --[Private Methods]-----------------------------------------

    //endregion

}
