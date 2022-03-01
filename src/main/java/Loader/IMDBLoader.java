package Loader;

import Infra.Attribute;
import Infra.DataVertex;
import Infra.RelationshipEdge;
import Util.Config;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.jena.rdf.model.*;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IMDBLoader extends GraphLoader{

    public IMDBLoader(List<String> paths) {

        super();
        for (String path:paths) {
            loadIMDBGraph(path);
        }
    }

    private void loadIMDBGraph(String dataGraphFilePath) {

        if (dataGraphFilePath == null || dataGraphFilePath.length() == 0) {
            System.out.println("No Input Graph Data File Path!");
            return;
        }
        System.out.println("Loading IMDB Graph: "+dataGraphFilePath);

        try
        {
            HashSet<String> types=new HashSet <>();
            Model model = ModelFactory.createDefaultModel();

            Path input= Paths.get(dataGraphFilePath);
            model.read(input.toUri().toString());

            StmtIterator dataTriples = model.listStatements();
            while (dataTriples.hasNext()) {

                Statement stmt = dataTriples.nextStatement();
                String subjectNodeURL = stmt.getSubject().getURI().toLowerCase();
                if (subjectNodeURL.length() > 16) {
                    subjectNodeURL = subjectNodeURL.substring(16);
                }

                var temp=subjectNodeURL.split("/");
                if(temp.length!=2)
                {
                    // Error!
                    continue;
                }
                String subjectType=temp[0];
                String subjectID=temp[1];

                // ignore the node if the type is not in the validTypes and
                // optimizedLoadingBasedOnTypes is true
                if(Config.optimizedLoadingBasedOnTypes && !validTypes.contains(subjectType))
                    continue;

                types.add(subjectType);
                //int nodeId = subject.hashCode();
                DataVertex subjectVertex= (DataVertex) graph.getNode(subjectID);

                if (subjectVertex==null) {
                    subjectVertex=new DataVertex(subjectID,subjectType);
                    graph.addVertex(subjectVertex);
                }
                else {
                    subjectVertex.addType(subjectType);
                }

                String predicate = stmt.getPredicate().getLocalName().toLowerCase();
                RDFNode object = stmt.getObject();
                String objectNodeURI;
                if (object.isLiteral())
                {
                    objectNodeURI = object.asLiteral().getString().toLowerCase();
                    if(Config.optimizedLoadingBasedOnTypes && validAttributes.contains(predicate)) {
                        subjectVertex.addAttribute(new Attribute(predicate, objectNodeURI));
                        graphSize++;
                    }
                }
                else
                {
                    objectNodeURI = object.toString().toLowerCase();
                    if (objectNodeURI.length() > 16)
                        objectNodeURI = objectNodeURI.substring(16);

                    temp=objectNodeURI.split("/");
                    if(temp.length!=2)
                    {
                        // Error!
                        continue;
                    }

                    String objectType=temp[0];
                    String objectID=temp[1];

                    // ignore the node if the type is not in the validTypes and
                    // optimizedLoadingBasedOnTGFD is true
                    if(Config.optimizedLoadingBasedOnTypes && !validTypes.contains(objectType))
                        continue;

                    types.add(objectType);
                    DataVertex objectVertex= (DataVertex) graph.getNode(objectID);
                    if (objectVertex==null) {
                        objectVertex=new DataVertex(objectID,objectType);
                        graph.addVertex(objectVertex);
                    }
                    else {
                        objectVertex.addType(objectType);
                    }
                    graph.addEdge(subjectVertex, objectVertex, new RelationshipEdge(predicate));
                    graphSize++;
                }
            }
            System.out.println("Done. Nodes: " + graph.getGraph().vertexSet().size() + ",  Edges: " +graph.getGraph().edgeSet().size());
            System.out.println("Number of types: " + types.size() + "\n");
            types.forEach(type -> System.out.print(type + " - "));
            //System.out.println("Done Loading DBPedia Graph.");
            //System.out.println("Number of subjects not found: " + numberOfSubjectsNotFound);
            //System.out.println("Number of loops found: " + numberOfLoops);

            model.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static void printWithTime(String message, long runTimeInMS)
    {
        System.out.println(message + " time: " + runTimeInMS + "(ms) ** " +
                TimeUnit.MILLISECONDS.toSeconds(runTimeInMS) + "(sec) ** " +
                TimeUnit.MILLISECONDS.toMinutes(runTimeInMS) +  "(min)");
    }
}
