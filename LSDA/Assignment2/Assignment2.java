import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Assignment2 {

    public static void main(String[] args) {

        //In order to encode the time as an integer I have decided to use
        //Epoch time (The number of seconds that have elapsed since January 1 1970)
        //I have defined an array of integers with different times from the same day (28 February 2021)

        long[] epochtimes = {1614470400,1614474000,1614477600,1614481200,1614484800,1614488400,1614492000,
                1614495600,1614499200,1614502800,1614506400,1614510000,1614513600,1614517200,1614520800,
                1614524400, 1614528000,1614531600,1614535200,1614538800,1614542400,1614546000,1614549600,
                1614553200};

        //I have used three different arrays to store the temperatures of three different cities (Barcelona, Madrid,
        // and Valencia). The position of the temperature is aligned with the position of the time.
        //E.g, temperature temperaturesBarcelona[0] was registered at epochtimes[0].

        double[] temperaturesBarcelona = {18.8,19.3,17.4,20.5,14.2,21.1,22.0,17.3,19.10,18.8,20.1,20.2,18.7,17.8,19.4,
                24.5,17.2,26.1,25.0,17.3,19.10,18.8,20.1,20.2};

        double[] temperaturesMadrid = {20.5,19.1,17.4,21.5,15.2,19.1,33,27.3,19.10,18.8,30.1,30.5, 12.7,12.8,39.4,26.5,
                17.4,24.1,25.4,11.3,15.10,16.8,19.1,1};

        double[] temperaturesValencia = {3.5,8.1,7.4,1.5,5.6,9.1,3,2.3,9.10,8.8,3.1,3.6,1.7,1.8,3.7,2.5,7.4,2.7,5.4,
                1.5,5.10,1.8,9.1,1.5};

        //I initialize three arrays of measurements,one per each of the cities
        ArrayList<Measurements> measurementsBarcelona = new ArrayList<>();
        ArrayList<Measurements> measurementsMadrid = new ArrayList<>();
        ArrayList<Measurements> measurementsValencia = new ArrayList<>();

        //I am adding each of the temperatures and respective time as an instance of Measurement in the arrays
        for(int i=0;i<epochtimes.length;i++){
            measurementsBarcelona.add(new Measurements(epochtimes[i],temperaturesBarcelona[i]));
            measurementsMadrid.add(new Measurements(epochtimes[i],temperaturesMadrid[i]));
            measurementsValencia.add(new Measurements(epochtimes[i],temperaturesValencia[i]));
        }

        //Finally I initialise three different stations with their respective measurements.
        WeatherStation WSBarcelona = new WeatherStation("Barcelona",measurementsBarcelona);
        WeatherStation WSMadrid = new WeatherStation("Madrid",measurementsMadrid);
        WeatherStation WSValencia = new WeatherStation("Valencia",measurementsValencia);

        //Using maxTemperature method to print the Maximum Temperature in Barcelona between 1614470400 and 1614488400
        System.out.println("The maximum temperature in " + WSBarcelona.getCity() +
                " between 1614470400 and 1614488400 was " + WSBarcelona.maxTemperature(1614470400,1614488400) );

        //Using countTemperatures to count the temperatures within the ranges. In this specific example
        //the ranges are for t1: [20.5-3.1,20.5+3.1] and for t2:[10.3-3.1,10.3+3.1]
        double t1 = 20.5;
        double t2 = 10.1;
        double r = 3.1;
        for(WeatherStation.TemperatureCountings Result:WeatherStation.countTemperatures(t1,t2,r)){
            System.out.println("There are " + Result.counting + " temperatures recorded in all the stations that " +
                    "are between " + (Result.temperatureRange-r) + " and " +
                    (Result.temperatureRange+r));
        }

    }
}

//Defining Class WeatherStation with three attributes: city, measurements, and a static station attribute
//to store all the instances created

class WeatherStation {
    private String city;
    private ArrayList<Measurements> measurements;
    static ArrayList<WeatherStation> stations = new ArrayList<>();

    //Constructor  setting attributes and adding new instances to stations array
    WeatherStation(String city, ArrayList<Measurements> measurements) {
        this.city = city;
        this.measurements = measurements;
        stations.add(this);
    }

    public ArrayList<Measurements> getMeasurements() {
        return this.measurements;
    }

    public String getCity(){
        return this.city;
    }

    //This method returns the maximum temperature between startTime and endTime using streams:
    //1. First filters all measurements between these two times, then
    //2. Maps each measurement to their temperature: (time,temperature) -> temperature, then
    //3. Uses 'max()' to return the maximum value
    public double maxTemperature(int startTime, int endTime) {
        return measurements
                .stream()
                .filter(measurement->measurement.getTime() >= startTime & measurement.getTime() <= endTime)
                .mapToDouble(measurement->measurement.getTemperature())
                .max().getAsDouble();
    }

    //This static class is used to define a datastructure such as (t1,1) and (t2,1)
    //for the phase of mapping. Each temperature t is map to (t1,1) and/or (t2,1)
    //if it satisfies the condition of being in the specific ranges [t1-r,t1+r] and [t2-r,t2+r] respectively
    static class Pair{
        double temperatureRange;
        int temperature;

        Pair(double temperatureRange,int temperature){
            this.temperatureRange = temperatureRange;
            this.temperature = temperature;
        }
    }

    //This static class will be used to construct datastructures such as (t1,c1) and (t2,c1)
    //where t1 and t2 are the temperatures passed as parameters to countTemperatures and
    // c1 and c2 are the counts of each temperature in each of the ranges
    static class TemperatureCountings{
        double temperatureRange;
        int counting;
        TemperatureCountings(double temperatureRange, int counting){
            this.temperatureRange = temperatureRange;
            this.counting = counting;
        }
    }

    //this static method returns an array of temperature counting which is the final result from Question 2.
    //I have implemented this method in three different phases: Map phase, Shuffle phase and Reduce phase

    static ArrayList<TemperatureCountings> countTemperatures(double t1,double t2,double r){

        //MAP

        //MapPhase maps each temperature t to (t1,1) and/or (t2,1)
        //in case that t is in the range [t1-r,t1+r] and/or [t2-r,t2+r] respectively
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


        //SHUFFLE

        //ShufflePhase splits the array from the MapPhase into two separated streams
        //one per each temperature.
        Stream<Stream> ShufflePhase = Stream.of(
                MapPhase.parallelStream().filter(x->x.temperatureRange == t1),
                MapPhase.parallelStream().filter(x->x.temperatureRange == t2));


        //REDUCE

        //Finally, ReducePhase counts the elements in each of the ranges
        //It uses parallel to perform the operation in different threads
        List<Long> ReducePhase = ShufflePhase.parallel().
                map(s-> s.count())
                .collect(Collectors.toList());

        //The counts given by the ReducePhase can be added in an array
        //with the required datastructures for the result:
        //[(t1,count1),(t2,count2)]
        ArrayList<TemperatureCountings> ResultTemperatures = new ArrayList<>();
        ResultTemperatures.add(new TemperatureCountings(t1,ReducePhase.get(0).intValue()));
        ResultTemperatures.add(new TemperatureCountings(t2,ReducePhase.get(1).intValue()));

        return ResultTemperatures;
    }
}
//Measurement class defined with attributes for time (integer) and temperature (double)
class Measurements{
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
