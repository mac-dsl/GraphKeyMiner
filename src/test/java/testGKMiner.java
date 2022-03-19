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

public class testGKMiner {

    public static void main(String []args) throws FileNotFoundException {

        long wallClockStart=System.currentTimeMillis();

        System.out.println("Test GKMiner algorithm.");

        if(!Config.parse(args[0]))
            return;

        System.out.println("Data files paths: " + Arrays.toString(Config.dataPaths.toArray()));
        System.out.println("Type files paths: " + Arrays.toString(Config.typesPaths.toArray()));
        System.out.println(Arrays.toString(Config.delta.toArray()));
        System.out.println(Arrays.toString(Config.k.toArray()));
        System.out.println("#Types: " + Config.types.size() + " -> " + Arrays.toString(Config.types.toArray()));

        HashMap<String,Long> runtimes_basedOnTypes = new HashMap<>();
        HashMap<String,Long> runtimes_basedOnDeltaAndK = new HashMap<>();

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
                    GKMiner miner = new GKMiner(graphLoader.getGraph(), summaryGraph, dependencyGraph, gKeys, type, delta, k, true);
                    startTime = System.currentTimeMillis();
                    miner.mine();
                    temp = System.currentTimeMillis()-startTime;
                    runtimes_basedOnTypes.put(type, runtimes_basedOnTypes.get(type) + temp);
                    runtimes_basedOnDeltaAndK.put(delta+"_"+k, runtimes_basedOnDeltaAndK.get(delta+"_"+k) + temp);
                    Helper.printWithTime("Mining (" + type + "," + delta + "," + k  + ","+ Config.optimize + ")", temp);

                    if (Config.saveKeys)
                        Helper.saveGKeys(type + "_" + delta + "_"+ Config.optimize + "_" + k, gKeys, temp);

                    runtimes.add(type, k, delta, temp);

                    graphLoader.getGraph().softResetGraph();
                }
            }
            graphLoader.getGraph().hardResetGraph();
        }
        runtimes_basedOnTypes.keySet().forEach(type -> Helper.printWithTime("(type) -> " + type, runtimes_basedOnTypes.get(type)));
        System.out.println("******************************************");
        runtimes_basedOnDeltaAndK.keySet().forEach(key -> Helper.printWithTime("(delta and k) -> " + key, runtimes_basedOnDeltaAndK.get(key)));

        List<Double> deltas = new ArrayList<>(Config.delta);
        Collections.sort(deltas);

        for (String type:Config.types) {
            StringBuilder sb=new StringBuilder();
            sb.append("delta| ");
            for (int i = 0, deltasSize = deltas.size(); i < deltasSize; i++) {
                sb.append(deltas.get(i)).append(", ");
            }
            sb.append("\n");
            for (int k : Config.k) {
                sb.append("K= ").append(k).append(" | ");
                for (int i = 0, deltasSize = deltas.size(); i < deltasSize; i++) {
                    sb.append(runtimes.getMilliSeconds(type,k,deltas.get(i))).append(", ");
                }
                sb.append("\n");
            }
            Helper.saveToFile("Runtime_MS_"+type,"txt",sb);
        }

        for (String type:Config.types) {
            StringBuilder sb=new StringBuilder();
            sb.append("delta| ");
            for (int i = 0, deltasSize = deltas.size(); i < deltasSize; i++) {
                sb.append(deltas.get(i)).append(", ");
            }
            sb.append("\n");
            for (int k : Config.k) {
                sb.append("K= ").append(k).append(" | ");
                for (int i = 0, deltasSize = deltas.size(); i < deltasSize; i++) {
                    sb.append(runtimes.getSeconds(type,k,deltas.get(i))).append(", ");
                }
                sb.append("\n");
            }
            Helper.saveToFile("Runtime_S_"+type,"txt",sb);
        }

        for (String type:Config.types) {
            StringBuilder sb=new StringBuilder();
            sb.append("delta| ");
            for (int i = 0, deltasSize = deltas.size(); i < deltasSize; i++) {
                sb.append(deltas.get(i)).append(", ");
            }
            sb.append("\n");
            for (int k : Config.k) {
                sb.append("K= ").append(k).append(" | ");
                for (int i = 0, deltasSize = deltas.size(); i < deltasSize; i++) {
                    sb.append(runtimes.getMinutes(type,k,deltas.get(i))).append(", ");
                }
                sb.append("\n");
            }
            Helper.saveToFile("Runtime_M_"+type,"txt",sb);
        }

        Helper.printWithTime("Total wall clock time: ", System.currentTimeMillis()-wallClockStart);
    }

}
