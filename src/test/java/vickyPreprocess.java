import Infra.Attribute;
import Infra.DataVertex;
import Infra.RelationshipEdge;
import Loader.DBPediaLoader;
import Loader.GraphLoader;
import Loader.IMDBLoader;
import Util.Config;
import Util.Helper;

import java.io.FileNotFoundException;
import java.util.Arrays;

public class vickyPreprocess {



    public static void main(String []args) throws FileNotFoundException {

        System.out.println("Preprocess the dataset for Vickey");

        if (!Config.parse(args[0]))
            return;

        System.out.println("Data files paths: " + Arrays.toString(Config.dataPaths.toArray()));
        System.out.println("Type files paths: " + Arrays.toString(Config.typesPaths.toArray()));
        System.out.println("#Types: " + Config.types.size() + " -> " + Arrays.toString(Config.types.toArray()));


        System.out.println("Loading the dataset.");
        long startTime=System.currentTimeMillis();
        GraphLoader graphLoader;
        if(Config.dataset.equals("dbpedia"))
            graphLoader = new DBPediaLoader(Config.typesPaths, Config.dataPaths);
        else if (Config.dataset.equals("imdb"))
            graphLoader = new IMDBLoader(Config.dataPaths);
        else {
            System.out.println("Please provide the dataset name in the config file as the help below.");
            Config.printHelp();
            return;
        }
        Helper.printWithTime("Loading time: ", System.currentTimeMillis()-startTime);

        for (String type:Config.types) {
            System.out.println("Creating vicky file for the type: " + type);
            StringBuilder sb =new StringBuilder();
            for (DataVertex v:graphLoader.getGraph().getGraph().vertexSet()) {
                if(v.getTypes().contains(type))
                {
                    for (Attribute attr:v.getAllAttributesList()) {
                        sb.append(v.getVertexURI())
                                .append("\t")
                                .append(attr.getAttrName())
                                .append("\t")
                                .append(attr.getAttrValue())
                                .append("\n");
                    }
                    for (RelationshipEdge e:graphLoader.getGraph().getGraph().outgoingEdgesOf(v)) {
                        sb.append(v.getVertexURI())
                                .append("\t")
                                .append(e.getLabel())
                                .append("\t")
                                .append(((DataVertex)e.getTarget()).getVertexURI())
                                .append("\n");
                    }
                }
            }
            Helper.saveToFile("Vickey_"+type,"tsv",sb);
        }

    }

}
