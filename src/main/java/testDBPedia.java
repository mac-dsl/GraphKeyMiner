import Dependency.DependencyGraph;
import Infra.CandidateGKey;
import Loader.DBPediaLoader;
import Miner.GKMiner;
import Summary.SummaryGraph;
import Util.Config;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class testDBPedia {


    public static void main(String []args) throws FileNotFoundException {

        long wallClockStart=System.currentTimeMillis();

        System.out.println("Test GKMier algorithm for the DBPedia dataset");

        Config.parse(args[0]);

        System.out.println(Arrays.toString(Config.dataPaths.toArray()));
        System.out.println(Arrays.toString(Config.typesPaths.toArray()));

        System.out.println("Loading the dataset.");
        long startTime=System.currentTimeMillis();
        DBPediaLoader dbpedia = new DBPediaLoader(Config.typesPaths, Config.dataPaths);
        printWithTime("Loading time: ", System.currentTimeMillis()-startTime);

        System.out.println("Creating summary graph.");
        startTime=System.currentTimeMillis();
        SummaryGraph summaryGraph=new SummaryGraph(dbpedia.getGraph());
        summaryGraph.summary();
        printWithTime("Summary graph time: ", System.currentTimeMillis()-startTime);

        DependencyGraph dependencyGraph = new DependencyGraph();

        System.out.println("Mining graph keys.");
        startTime=System.currentTimeMillis();

        GKMiner miner = new GKMiner(dbpedia.getGraph(), summaryGraph,dependencyGraph,Config.type,Config.delta,Config.k);
        miner.mine();

        printWithTime("Mining time: ", System.currentTimeMillis()-startTime);

        if(Config.saveKeys)
        {
            HashMap<String, ArrayList<CandidateGKey>> gKeys = miner.getAllGKeys();
            saveGKeys(Config.type,gKeys);
        }

        printWithTime("Total wall clock time: ", System.currentTimeMillis()-wallClockStart);
    }

    private static void printWithTime(String message, long runTimeInMS)
    {
        System.out.println(message + " time: " + runTimeInMS + "(ms) ** " +
                TimeUnit.MILLISECONDS.toSeconds(runTimeInMS) + "(sec) ** " +
                TimeUnit.MILLISECONDS.toMinutes(runTimeInMS) +  "(min)");
    }

    private static void saveGKeys(String path, HashMap<String, ArrayList<CandidateGKey>> gKeys)
    {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            FileWriter file = new FileWriter("%s_%s.txt".formatted(path, time));
            for (String type:gKeys.keySet()) {
                file.write("***************Graph Keys for %s***************\n".formatted(type));
                for (CandidateGKey gkey:gKeys.get(type)) {
                    file.write(gkey.toString()+ "\n");
                }
                file.write("---------------------------------------------------\n");
            }
            file.close();
            System.out.println("Successfully wrote to the file: " + path);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
