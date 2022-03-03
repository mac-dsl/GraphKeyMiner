package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Config {

    public static ArrayList<String> typesPaths = new ArrayList<>();
    public static ArrayList<String> dataPaths = new ArrayList<>();

    public static String language="N-Triples";
    public static String dataset="imdb";

    public static int entityID=1;
    public static double delta;
    public static int k;
    public static String type;

    public static boolean optimizedLoadingBasedOnTypes =false;
    public static boolean saveKeys=true;
    public static boolean debug =false;
    public static boolean preprocessOptimization = true;
    public static boolean saveSummaryGraph = false;
    public static boolean saveSummaryGraphBasedOnDelta = true;

    public static void parse(String input) throws FileNotFoundException {
        if(input.equals("--help")) {
            System.out.println("""
                     Expected arguments to parse:
                     -t <typeFile>
                     -d <dataFile>
                     -k <integer> // Maximum size of the GKey
                     -delta <double> // Minimum coverage threshold of the GKey
                     -type <String> // Type of the center node
                     -optgraphload <true-false> // load based on specific types?
                     -debug <true-false> // print details of matching
                     -save <true-false> // Save the mined GKeys
                     -language <language name> // Names like "N-Triples", "TURTLE", "RDF/XML"
                     -dataset <dataset name> // Options: imdb (default), dbpedia, synthetic
                    """.indent(5));
        } else
            parseInputParams(input);
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
                switch (conf[0]) {
                    case "-t" -> typesPaths.add(conf[1]);
                    case "-d" -> dataPaths.add(conf[1]);
                    case "-k" -> k = Integer.parseInt(conf[1]);
                    case "-delta" -> delta = Double.parseDouble(conf[1]);
                    case "-type" -> type = conf[1];
                    case "-optgraphload" -> optimizedLoadingBasedOnTypes = Boolean.parseBoolean(conf[1]);
                    case "-debug" -> debug = Boolean.parseBoolean(conf[1]);
                    case "-save" -> saveKeys = Boolean.parseBoolean(conf[1]);
                    case "-language" -> language = conf[1];
                    case "-dataset" -> dataset = conf[1];
                    case "-preopt" -> preprocessOptimization = Boolean.parseBoolean(conf[1]);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
