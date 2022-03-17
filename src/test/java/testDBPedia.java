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

        HashMap<String,Long> runtimes_basedOnTypes = new HashMap<>();
        HashMap<String,Long> runtimes_basedOnDeltaAndK = new HashMap<>();

        System.out.println("Loading the dataset.");
        long startTime=System.currentTimeMillis(),temp;
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
            runtimes_basedOnTypes.put(type, 0L);
            for (double delta:Config.delta) {
                for (int k:Config.k) {
                    if(!runtimes_basedOnDeltaAndK.containsKey(delta+"_"+k))
                        runtimes_basedOnDeltaAndK.put(delta+"_"+k,0L);
                    DependencyGraph dependencyGraph = new DependencyGraph();

                    //System.out.println("Mining graph keys for type: " + type);
                    HashMap<String, ArrayList<CandidateGKey>> gKeys = new HashMap<>();
                    GKMiner miner = new GKMiner(dbpedia.getGraph(), summaryGraph, dependencyGraph, gKeys, type, delta, k, true);
                    startTime = System.currentTimeMillis();
                    miner.mine();
                    temp = System.currentTimeMillis()-startTime;
                    runtimes_basedOnTypes.put(type, runtimes_basedOnTypes.get(type) + temp);
                    runtimes_basedOnDeltaAndK.put(delta+"_"+k, runtimes_basedOnDeltaAndK.get(delta+"_"+k) + temp);
                    Helper.printWithTime("Mining (" + type + "," + delta + "," + k  + ","+ Config.optimize + ")", temp);

                    if (Config.saveKeys)
                        Helper.saveGKeys(type + "_" + delta + "_"+ Config.optimize + "_" + k, gKeys, temp);

                    dbpedia.getGraph().softResetGraph();
                }
            }
            dbpedia.getGraph().hardResetGraph();
        }
        runtimes_basedOnTypes.keySet().forEach(type -> Helper.printWithTime("(type) -> " + type, runtimes_basedOnTypes.get(type)));
        System.out.println("******************************************");
        runtimes_basedOnDeltaAndK.keySet().forEach(key -> Helper.printWithTime("(delta and k) -> " + key, runtimes_basedOnDeltaAndK.get(key)));

        Helper.printWithTime("Total wall clock time: ", System.currentTimeMillis()-wallClockStart);
    }

}
