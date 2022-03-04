import Dependency.DependencyGraph;
import Infra.CandidateGKey;
import Loader.DBPediaLoader;
import Miner.GKMiner;
import Summary.SummaryGraph;
import Util.Config;
import Util.Helper;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class testDBPedia {


    public static void main(String []args) throws FileNotFoundException {

        long wallClockStart=System.currentTimeMillis();

        System.out.println("Test GKMier algorithm for the DBPedia dataset");

        if(!Config.parse(args[0]))
            return;

        System.out.println(Arrays.toString(Config.dataPaths.toArray()));
        System.out.println(Arrays.toString(Config.typesPaths.toArray()));
        System.out.println(Arrays.toString(Config.delta.toArray()));
        System.out.println(Arrays.toString(Config.types.toArray()));

        HashMap<String,Long> runtimes = new HashMap<>();

        System.out.println("Loading the dataset.");
        long startTime=System.currentTimeMillis();
        DBPediaLoader dbpedia = new DBPediaLoader(Config.typesPaths, Config.dataPaths);
        Helper.printWithTime("Loading time: ", System.currentTimeMillis()-startTime);

        System.out.println("Creating Summary Graph.");
        startTime=System.currentTimeMillis();
        SummaryGraph summaryGraph=new SummaryGraph(dbpedia.getGraph());
        summaryGraph.summary();
        if(Config.preprocessOptimization)
            summaryGraph.findUniqueness();
        if(Config.saveSummaryGraph)
            summaryGraph.saveToFile();
        if(Config.saveSummaryGraphBasedOnDelta)
            Config.delta.forEach(summaryGraph::saveToFile);

        Helper.printWithTime("Summary Graph (total time): ", System.currentTimeMillis()-startTime);

        for (String type:Config.types) {
            runtimes.put(type, 0L);
            for (double delta:Config.delta) {
                DependencyGraph dependencyGraph = new DependencyGraph();

                //System.out.println("Mining graph keys for type: " + type);
                startTime=System.currentTimeMillis();
                HashMap<String, ArrayList<CandidateGKey>> gKeys=new HashMap<>();
                GKMiner miner = new GKMiner(dbpedia.getGraph(), summaryGraph,dependencyGraph,gKeys,type,delta,Config.k, true);
                miner.mine();
                runtimes.put(type,runtimes.get(type)+System.currentTimeMillis()-startTime);
                Helper.printWithTime("Mining ("+type+","+delta+")", System.currentTimeMillis()-startTime);

                if(Config.saveKeys)
                    Helper.saveGKeys(type + "_" + delta,gKeys);

                dbpedia.getGraph().resetGraph();
            }
        }
        runtimes.keySet().forEach(type -> Helper.printWithTime("Type: " + type, runtimes.get(type)));

        Helper.printWithTime("Total wall clock time: ", System.currentTimeMillis()-wallClockStart);
    }

}
