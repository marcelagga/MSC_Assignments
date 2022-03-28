/*
Name: Marcel Aguilar Garcia
ID: 20235620
 */

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.clustering.KMeansModel;
import org.apache.spark.mllib.clustering.KMeans;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.Vector;
import scala.Tuple2;

public class Assignment4_2 {

    public static void main(String[] args) {

        /*

        I first define the configuration for Spark using SparkConf.
        I have used the same parameters as the WordCount example from
        the lectures.

         */

        System.setProperty("hadoop.home.dir", "C:/winutils");

        SparkConf sparkConf = new SparkConf()
                .setAppName("IMDBRreviews")
                .setMaster("local[4]").set("spark.executor.memory", "1g");

        JavaSparkContext sc = new JavaSparkContext(sparkConf);

        /*

        By using textFile I read the file twitter2D.txt into an RDD as a collection of lines.

        I then map each line to a Tuple2 that has a vector with the world coordinates
        in the first component and the tweet message in the second component.

        I store this information in a JavaPairRDD<Vector,String>.

        The world coordinates of each tweet are the first two values from the csv.
        In order to find the coordinates I split each line by ','.
        I then store the first two values and convert them to double.
        I store this result in a vector.

        In a similar way, I use regex to split the line by any number followed by ','.
        The last component of the split is the tweet. If a tweet was to contain
        this pattern this could be a problem and a more complex regex would be used
        (there is no tweet with this pattern in the data provided).

        Finally, this is returned as Tuple2 that has the first component as the coordinates
        and the second one as the extracted tweet.

         */

        JavaPairRDD<Vector,String> featuresTweets = sc.textFile("twitter2D.txt").mapToPair(line->{
            String[] words = line.split(",");
            String[] tweetTemp = line.split("[0-9]+,");
            double[] values = new double[2];
            values[0] = Double.parseDouble(words[0]);
            values[1] = Double.parseDouble(words[1]);
            return new Tuple2(Vectors.dense(values),tweetTemp[tweetTemp.length-1]);
            }
        );

        /*

        featureTweets RDD is used several times and therefore is efficient to cache it.

         */

        featuresTweets.cache();


        /*

        I train a KMeans model with 4 clusters and 30 iterations. I just used the keys of featureTweets
        which are exactly the world coordinates.

         */

        KMeansModel model = KMeans.train(featuresTweets.keys().rdd(),4,30);

        /*

        As we would like to know the clusters from the train set, I use the model to predict them.
        This information is returned as a JavaRDD of Integers. As I need to know the associated
        tweet to each cluster I use zip to join this RDD with the tweet messages from featuresTweets which
        are exactly the values of featureTweets. This result is returned as a JavaPairRDD<Integer,String>.

        As we would like the printing to be ordered by cluster, I use 'sortByKey' to sort the JavaRDD.
        However, I specify numPartitions to 1 as, if the data is partitioned, as in this case,
        this leads to problems when sorting. Each partition would get sorted and then printed.
        Therefore, the final order could be wrong. Finally, I print the data as required using 'for each'
        and System.out.println.

         */

        model.predict(featuresTweets.keys())
                .zip(featuresTweets.values())
                .sortByKey(true, 1)
                .foreach(line -> System.out.println("Tweet " + "\""+line._2()+"\"" + " is in cluster " + line._1()));

    }

}
