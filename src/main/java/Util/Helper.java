package Util;

import Infra.CandidateGKey;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Helper {

    private static long tempRunTime = System.currentTimeMillis();

    public static void setTemporaryTimer()
    {
        tempRunTime = System.currentTimeMillis();
    }

    public static void printWithTime(String message)
    {
        long runTimeInMS = System.currentTimeMillis() - tempRunTime;
        System.out.println(message + " time: " + runTimeInMS + "(ms) ** " +
                TimeUnit.MILLISECONDS.toSeconds(runTimeInMS) + "(sec) ** " +
                TimeUnit.MILLISECONDS.toMinutes(runTimeInMS) +  "(min)");
    }

    public static void printWithTime(String message, long runTimeInMS)
    {
        System.out.println(message + " time: " + runTimeInMS + "(ms) ** " +
                TimeUnit.MILLISECONDS.toSeconds(runTimeInMS) + "(sec) ** " +
                TimeUnit.MILLISECONDS.toMinutes(runTimeInMS) +  "(min)");
    }

    public static void saveGKeys(String path, HashMap<String, ArrayList<CandidateGKey>> gKeys, Long runtime)
    {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            FileWriter file = new FileWriter("%s_%s.txt".formatted(path, time));
            file.write("Total runtime: " + runtime + "(ms) ** " +
                    TimeUnit.MILLISECONDS.toSeconds(runtime) + "(sec) ** " +
                    TimeUnit.MILLISECONDS.toMinutes(runtime) +  "(min)\n");
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

    public static void saveToFile(String name,String format, StringBuilder content, boolean putDate)
    {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
            LocalDateTime now = LocalDateTime.now();
            String time = dtf.format(now);
            FileWriter file;
            if(putDate)
                file = new FileWriter("%s_%s.%s".formatted(name, time,format));
            else
                file = new FileWriter("%s.%s".formatted(name,format));
            file.write(content.toString());
            file.close();
            System.out.println("Successfully wrote to the file: " + name);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
