/*

Name: Marcel Aguilar Garcia
Student ID: 20235620

 */

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Serializable;

public class MyClass_Q1 {

    public static void main(String[] args) {

        /*

        MAIN:

        I have used the same setup than in Assignment 2. I have removed the comments from the previous assignment
        for simplicity.

        Summary: I am initiating three different Weather Stations (Barcelona, Madrid, and Valencia) with
        different temperatures that were recorded on different times (encoded as epochtimes)
        on February 28th, 2021.

        Finally, I call the method countTemperature(t) to count the approximate number of temperatures
        for the given t. In my example t = 10, and therefore, countTemperature checks how many times t
        is the interval [9,11].

        Additional comments can be found on the method countTemperature(t).

         */

        long[] epochtimes = {1614470400,1614474000,1614477600,1614481200,1614484800,1614488400,1614492000,
                1614495600,1614499200,1614502800,1614506400,1614510000,1614513600,1614517200,1614520800,
                1614524400, 1614528000,1614531600,1614535200,1614538800,1614542400,1614546000,1614549600,
                1614553200};

        double[] temperaturesBarcelona = {18.8,19.3,17.4,20.5,14.2,21.1,22.0,17.3,19.10,18.8,20.1,20.2,18.7,17.8,19.4,
                24.5,17.2,26.1,25.0,17.3,19.10,18.8,20.1,20.2};

        double[] temperaturesMadrid = {20.5,19.1,17.4,21.5,15.2,19.1,33,27.3,19.10,18.8,30.1,30.5, 12.7,12.8,39.4,26.5,
                17.4,24.1,25.4,11.3,15.10,16.8,19.1,1};

        double[] temperaturesValencia = {3.5,8.1,7.4,1.5,5.6,9.1,3,2.3,9.10,8.8,3.1,3.6,1.7,1.8,3.7,2.5,7.4,2.7,5.4,
                1.5,5.10,1.8,9.1,1.5};

        ArrayList<Measurements> measurementsBarcelona = new ArrayList<>();
        ArrayList<Measurements> measurementsMadrid = new ArrayList<>();
        ArrayList<Measurements> measurementsValencia = new ArrayList<>();

        for(int i=0;i<epochtimes.length;i++){
            measurementsBarcelona.add(new Measurements(epochtimes[i],temperaturesBarcelona[i]));
            measurementsMadrid.add(new Measurements(epochtimes[i],temperaturesMadrid[i]));
            measurementsValencia.add(new Measurements(epochtimes[i],temperaturesValencia[i]));
        }

        WeatherStation WSBarcelona = new WeatherStation("Barcelona",measurementsBarcelona);
        WeatherStation WSMadrid = new WeatherStation("Madrid",measurementsMadrid);
        WeatherStation WSValencia = new WeatherStation("Valencia",measurementsValencia);

        /*
        For this assignment, I have used t = 10. In this case, countTemperature(t) returns
        the number of times that the temperature t = 10 has been approximately recorded within
        the three Weather Stations. As seen in the result, it happens to be 3 times.
         */

        int t = 10;

        System.out.println("The number of times that temperature " + t +" " +
                "has been approximately measured by any of the weather stations is " +
                + WeatherStation.countTemperature(t) + " times");

    }
}

/*
WEATHER STATION:

In order to use JavaRDDs with instances of the class Weather Station
I had to make Weather Station serializable. For the same reason,
I had to make the class Measurements serializable.

 */

class WeatherStation implements Serializable {

    private String city;
    private ArrayList<Measurements> measurements;
    static ArrayList<WeatherStation> stations = new ArrayList<>();

    WeatherStation(String city, ArrayList<Measurements> measurements) {
        this.city = city;
        this.measurements = measurements;
        stations.add(this);
    }

    public ArrayList<Measurements> getMeasurements() {
        return this.measurements;
    }

    /*

    I have defined countTemperature as a static method which
    should return an integer (the counting of the approximate
    temperatures)

     */

    static int countTemperature(double t){

        /*

        I first define the configuration for Spark using SparkConf.
        I have used the same parameters as the WordCount example from
        the lectures.

         */

        System.setProperty("hadoop.home.dir", "C:/winutils");

        SparkConf sparkConf = new SparkConf()
                .setAppName("countTemperature")
                .setMaster("local[4]").set("spark.executor.memory", "1g");

        /*

        I then initialize an entry point to Spark that will be used
        to JavaRDD classes.

         */

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        /*

        Using a JavaRDD, I map stations to the related array of Measures.
        As it is using flatMap, the final RDD will have the measurements
        of all stations (not the arrays).

        I then map each of these measurements to their related temperature.

        Where I can finally use filter to just gather temperatures within
        the range [t-1,t+1].

        Finally, I map each of these temperatures to 1. By adding up these
        temperatures I will have the final result.

        There may be the chance that no temperatures are in this interval.
        Therefore, I first check if the JavaRDD with the final mapping is empty
        and if it is, I return 0.

        Otherwise, I use reduce to add up all the elements from the JavaRDD
        which returns the final counting of the temperatures.
         */

        JavaRDD<Integer> temperaturesFiltered =
               ctx.parallelize(stations)
                       .flatMap(station -> (station.getMeasurements()).iterator())
                       .map(c -> c.getTemperature())
                       .filter(k-> k <= t+1 & k >= t-1)
                       .map(x-> 1);

        temperaturesFiltered.cache();

        if (temperaturesFiltered.isEmpty()){
            return 0;
        }
        else{
            return temperaturesFiltered.reduce((a,b)->a+b);
        }

    }
    public double maxTemperature(int startTime, int endTime) {
        return measurements
                .stream()
                .filter(measurement->measurement.getTime() >= startTime & measurement.getTime() <= endTime)
                .mapToDouble(measurement->measurement.getTemperature())
                .max().getAsDouble();
    }

    static class Pair{
        double temperatureRange;
        int temperature;

        Pair(double temperatureRange,int temperature){
            this.temperatureRange = temperatureRange;
            this.temperature = temperature;
        }
    }

    static class TemperatureCountings{
        double temperatureRange;
        int counting;
        TemperatureCountings(double temperatureRange, int counting){
            this.temperatureRange = temperatureRange;
            this.counting = counting;
        }
    }


    static ArrayList<TemperatureCountings> countTemperatures(double t1,double t2,double r){

        List<Pair> MapPhase = stations.parallelStream().
                flatMap(station -> station.getMeasurements().parallelStream()).
                map(c->c.getTemperature()).
                flatMap(s -> {
                    ArrayList<Pair> ArrayPairs = new ArrayList<>();
                    if (s >= t1-r & s <=t1+r) {
                        ArrayPairs.add(new Pair(t1,1));
                    }
                    if (s >= t2-r & s <=t2+r) {
                        ArrayPairs.add(new Pair(t2,1));
                    }
                    return ArrayPairs.stream();
                }).collect(Collectors.toList());



        Stream<Stream> ShufflePhase = Stream.of(
                MapPhase.parallelStream().filter(x->x.temperatureRange == t1),
                MapPhase.parallelStream().filter(x->x.temperatureRange == t2));


        List<Long> ReducePhase = ShufflePhase.parallel().
                map(s-> s.count())
                .collect(Collectors.toList());

        ArrayList<TemperatureCountings> ResultTemperatures = new ArrayList<>();
        ResultTemperatures.add(new TemperatureCountings(t1,ReducePhase.get(0).intValue()));
        ResultTemperatures.add(new TemperatureCountings(t2,ReducePhase.get(1).intValue()));

        return ResultTemperatures;
    }
}

/*
MEASUREMENTS:

In order to use JavaRDDs with instances of the class Measurements
I had to make Measurements serializable.

 */
class Measurements implements Serializable{
    private long time;
    private double temperature;

    Measurements(long time, double temperature){
        this.time = time;
        this.temperature = temperature;
    }
    public long getTime(){
        return time;
    }
    public double getTemperature(){
        return temperature;
    }
}
