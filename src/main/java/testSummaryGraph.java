import Dependency.DependencyGraph;
import Infra.CandidateGKey;
import Loader.DBPediaLoader;
import Loader.GraphLoader;
import Loader.IMDBLoader;
import Miner.GKMiner;
import Summary.SummaryGraph;
import Util.Config;
import Util.Helper;
import Util.Runtime;

import java.io.FileNotFoundException;
import java.util.*;

public class testSummaryGraph {

    public static void main(String []args) throws FileNotFoundException {

        long wallClockStart=System.currentTimeMillis();

        System.out.println("Test Summary Graph Generator.");

        if(args.length==0)
        {
            Config.printHelp();
            return;
        }
        if(!Config.parse(args[0]))
            return;

        System.out.println("Data files paths: " + Arrays.toString(Config.dataPaths.toArray()));
        System.out.println("Type files paths: " + Arrays.toString(Config.typesPaths.toArray()));


        System.out.println("Loading the dataset.");
        long startTime=System.currentTimeMillis(),temp;
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

        Runtime runtimes=new Runtime();
        System.out.println("Creating Summary Graph.");
        startTime=System.currentTimeMillis();
        SummaryGraph summaryGraph=new SummaryGraph(graphLoader.getGraph());
        summaryGraph.summary();

        summaryGraph.saveToFile(0.0001);

        Helper.printWithTime("Summary Graph (total time): ", System.currentTimeMillis()-startTime);
        System.out.println("Done!");
        Helper.printWithTime("Total wall clock time: ", System.currentTimeMillis()-wallClockStart);
    }

}
