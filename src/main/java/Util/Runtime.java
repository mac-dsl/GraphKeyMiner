package Util;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Runtime {

    private HashMap<String,HashMap<Integer,HashMap<Double,Integer>>> msMap;
    private HashMap<String,HashMap<Integer,HashMap<Double,Integer>>> secondsMap;
    private HashMap<String,HashMap<Integer,HashMap<Double,Integer>>> minutesMap;

    public Runtime()
    {
        msMap=new HashMap<>();
        secondsMap=new HashMap<>();
        minutesMap=new HashMap<>();
    }

    public void add(String type, int k, double delta, long time)
    {
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(time);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(time);

        if(!msMap.containsKey(type))
            msMap.put(type, new HashMap<>());
        if(!msMap.get(type).containsKey(k))
            msMap.get(type).put(k,new HashMap<>());
        msMap.get(type).get(k).put(delta, (int) time);

        if(!secondsMap.containsKey(type))
            secondsMap.put(type, new HashMap<>());
        if(!secondsMap.get(type).containsKey(k))
            secondsMap.get(type).put(k,new HashMap<>());
        secondsMap.get(type).get(k).put(delta, seconds);

        if(!minutesMap.containsKey(type))
            minutesMap.put(type, new HashMap<>());
        if(!minutesMap.get(type).containsKey(k))
            minutesMap.get(type).put(k,new HashMap<>());
        minutesMap.get(type).get(k).put(delta, minutes);
    }

    public int getMilliSeconds(String type, int k, double delta)
    {
        return msMap.get(type).get(k).get(delta);
    }

    public int getSeconds(String type, int k, double delta)
    {
        return secondsMap.get(type).get(k).get(delta);
    }

    public int getMinutes(String type, int k, double delta)
    {
        return minutesMap.get(type).get(k).get(delta);
    }

}
