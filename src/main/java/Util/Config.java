package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Config {

    public static ArrayList<String> typesPaths = new ArrayList<>();
    public static ArrayList<String> dataPaths = new ArrayList<>();

    public static String language="N-Triples";
    public static String dataset="imdb";

    public static int entityID=1;
    public static HashSet<Double> delta= new HashSet<>();
    public static HashSet<Integer> k=new HashSet<>();
    public static HashSet<String> types=new HashSet<>();

    public static boolean optimize =true;
    public static boolean saveKeys=true;
    public static boolean debug =false;
    public static boolean preprocessOptimization = true;
    public static boolean saveSummaryGraph = true;
    public static boolean saveSummaryGraphBasedOnDelta = true;

    public static boolean parse(String input) {
        if(input.equals("--help")) {
            System.out.println("""
                     Expected arguments to parse:
                     -t <typeFile>
                     -d <dataFile>
                     -k List<integer> // List of K as each are for maximum size of the GKey
                     -delta List<double> // List of double values for minimum coverage threshold of the GKey (run multiple times). example: 0.1,0.2,0.3
                     -types List<names> // List of types for the center nodes (run multiple times). example: type1,type2,type3
                     -optimize <true-false> // perform optimization based on unique vectors
                     -debug <true-false> // print details of matching
                     -saveKeys <true-false> // Save the mined GKeys
                     -language <language name> // Names like "N-Triples", "TURTLE", "RDF/XML"
                     -dataset <dataset name> // Options: imdb (default), dbpedia, synthetic
                     -preprocessOptimization <true-false> // find uniqueness of attributes in the pre-process
                     -saveSummaryGraph <true-false> // Save the summary graph in a separate file to be loaded later
                     -saveSummaryGraphBasedOnDelta <true-false> // Save the summary graph based on delta in a separate file in a more readable format
                    """.indent(5));
            return false;
        }
        else
        {
            parseInputParams(input);
            return true;
        }
    }

    private static void parseInputParams(String pathToConfigFile) {
        Scanner scanner;
        try {
            scanner = new Scanner(new File(pathToConfigFile));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] conf = line.toLowerCase().split(" ");
                if (conf.length != 2)
                    continue;
                switch (conf[0].toLowerCase()) {
                    case "-t" -> typesPaths.add(conf[1]);
                    case "-d" -> dataPaths.add(conf[1]);
                    case "-k" -> Arrays.stream(conf[1].split(",")).forEach(val -> k.add(Integer.valueOf(val)));
                    case "-delta" -> Arrays.stream(conf[1].split(",")).forEach(val -> delta.add(Double.valueOf(val)));
                    case "-types" -> types.addAll(List.of(conf[1].split(",")));
                    case "-optimize" -> optimize = Boolean.parseBoolean(conf[1]);
                    case "-debug" -> debug = Boolean.parseBoolean(conf[1]);
                    case "-savekeys" -> saveKeys = Boolean.parseBoolean(conf[1]);
                    case "-language" -> language = conf[1];
                    case "-dataset" -> dataset = conf[1];
                    case "-preprocessoptimization" -> preprocessOptimization = Boolean.parseBoolean(conf[1]);
                    case "-savesummarygraph" -> saveSummaryGraph = Boolean.parseBoolean(conf[1]);
                    case "-savesummarygraphbasedondelta" -> saveSummaryGraphBasedOnDelta = Boolean.parseBoolean(conf[1]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
