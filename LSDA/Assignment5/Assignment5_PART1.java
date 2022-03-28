import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.*;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;
import scala.Tuple2;
import twitter4j.Status;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class Assignment5_PART1 {
    public static void main(String[] args) {
                    /*

        I first define the configuration for Spark using SparkConf.
        I have used the same parameters as the WordCount example from
        the lectures.

         */

        System.setProperty("hadoop.home.dir", "C:/winutils");

        SparkConf sparkConf = new SparkConf()
                .setAppName("SparkTwitter")
                .setMaster("local[4]").set("spark.executor.memory", "1g");

        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(1000));

        /*
        Initializing Twitter Keys and Tokens from my dev account to
        authenticate App request and gain access to data
         */

        /* API KEY */
        final String consumerKey = "";
        /* API SECRET KEY */
        final String consumerSecret = "";
        /* ACCESS TOKEN*/
        final String accessToken = "";
        /* ACCESS TOKEN SECRET*/
        final String accessTokenSecret = "";

        /*
        Setting oAuth configuration from Twitter dev account
         */
        System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret);

        /*
        Creating an input stream of tweets received from the authorised Twitter account
         using library Twitter4J
         */
        JavaReceiverInputDStream<Status> twitterStream = TwitterUtils.createStream(jssc);

        /*
        ###########################################################################################
        PART A - Streaming - QUESTION 1
        ###########################################################################################

        twitter4j.status object contains many information. We are just interested
        in the text of each tweet. I map each status to its text by using
        method getText().

        Note that JavaStreamingContext Duration is set to 1000ms = 1s.

         */

        JavaDStream<String> tweets = twitterStream.map((Function<Status, String>) Status::getText);
        tweets.print();

       /*
        ###########################################################################################
        PART A - Streaming - QUESTION 2
        ###########################################################################################

        From the text of the Twitter we can get the following attributes:
             - Number of characters: By calculating the length of the tweet (string)
             - Number of words: By splitting each tweet by spaces and calculating the number of elements
             - Words Starting By HashTag: Again, by splitting each tweet by spaces but then, filtering
                each element that starts by #
        */

        tweets.map(String::length).print();
        tweets.map(tweet->tweet.split(" ").length).print();
        tweets.flatMap(tweet -> Arrays.asList(tweet.split(" ")).iterator())
                .filter(word -> word.startsWith("#")).print();

        /*

        ###########################################################################################
        PART A - Streaming - QUESTION 3
        ###########################################################################################

        To calculate the average number of characters and words per tweet, I have used Atomic Variables.
        As otherwise there could be a problem of multiple threads updating the value at the same time.

        I have used the following variables:
            countTotal: which counts the total number of tweets in each RDD
            charsTotal: which sum the total number of characters in each tweet per RDD
            wordsTotal: which sums the total number of words in each tweet per RDD


        */

        AtomicLong countTotal = new AtomicLong();
        AtomicLong charsTotal = new AtomicLong();
        AtomicLong wordsTotal = new AtomicLong();

        /*

        Now, if an RDD is not empty,  I increase each variable with the expected value.
            countTotal: increased by the total count of tweets in each RDD
            charsTotal: Each tweet is map to its length, finally, all values are sum using reduce
            charsWord: Each tweet is map to the number of words, finally, all values are sum using reduce

         */

        tweets.foreachRDD(RDDTweets-> {
            if(RDDTweets.count() > 0) {
                countTotal.addAndGet(RDDTweets.count());
                charsTotal.addAndGet(RDDTweets.map(String::length).reduce(Integer::sum));
                wordsTotal.addAndGet(RDDTweets.map(tweet -> tweet.split(" ").length).reduce(Integer::sum));

                float averageChars = charsTotal.floatValue() / countTotal.floatValue();
                float averageWords = wordsTotal.floatValue() / countTotal.floatValue();

                System.out.println("The total average of characters for all the tweets is : " + averageChars +
                        " The total average of words for all the tweets is " + averageWords);
            }});

      /*

           Now, it is easy to calculate the same information for the last 5 minutes of tweets and
           repeat the computation every 30 seconds.

           Everything is going to be the same as above, so I am not going to comment much. However,
           the main difference is here is that we are using window with two parameters:
           new Duration(60 * 5 * 1000) and new Duration(30 * 1000)) to consider just the tweets
           in the time range that we expected


       */

        AtomicLong countTotalWindow = new AtomicLong();
        AtomicLong charsTotalWindow = new AtomicLong();
        AtomicLong wordsTotalWindow = new AtomicLong();

        tweets.window(new Duration(60 * 5 * 1000),new Duration(30 * 1000))
                .foreachRDD(RDDTweets-> {
                    if(RDDTweets.count() > 0) {
                        countTotalWindow.addAndGet(RDDTweets.count());
                        charsTotalWindow.addAndGet(RDDTweets.map(String::length).reduce(Integer::sum));
                        wordsTotalWindow.addAndGet(RDDTweets.map(tweet -> tweet.split(" ").length)
                                .reduce(Integer::sum));

                        float averageChars = charsTotalWindow.floatValue() / countTotalWindow.floatValue();
                        float averageWords = wordsTotalWindow.floatValue() / countTotalWindow.floatValue();

                        System.out.println("The windowed average of characters for all the tweets is : " + averageChars +
                                " The windowed average of words for all the tweets is " + averageWords);
                    }});

        /*

         In order to count the top 10 Hashtags I have used the following methodology (similar to the
         example provided in the assignment):

         1. I first map all tweets to its words
         2. Then, this is filtered by words that start by #
         3. Each of this words is then map to a tuple (hashTag,1)
         4. Then, reduce is applied to calculate the total number of occurrences per hashTag (hashTag,TotalCount)
         5. As we are going to apply sortByKey - this tuples needs to first be swapped in this step (1,hashTag)
         6. Then, we can finally sortByKey
         7. Now, the information is ready to be printed. This can be done with foreachRDD. Note that take(10)
         is used to limit this information to the first 10 elements of the list.

        I had a problem using this approach to calculate the totals (not window). I found the following resources:
        https://stackoverflow.com/questions/43774157/kafka-stream-to-spark-does-not-reduce-counts
        which had similar problems using kafka and says:

        "I set the interval between successive reads from the stream as 2 seconds. I realized that I am not
        producing enough content of the same string within this interval (2 seconds) at the producer end to get
        an aggregated result. However, when I increase this interval to like 10000 millis (10 sec), then I could produce
        multiple lines of data from the kafka-producer. These lines are duly processed by the job and similar string
        counts are nicely aggregated for that specific interval."

        It seems that when I was using interval as 10000 milliseconds I was getting correct values as well.
        However, by using the commented code below I was able to print the correct values for this case (the code
        is not too clean and should be refactor):


        JavaRDD<Tuple2<String, Integer>> hashTagCountsTemp = jssc.sparkContext().emptyRDD();
        AtomicReference<JavaPairRDD<String, Integer>>
                hashTagCountsTotal = new AtomicReference<>(hashTagCountsTemp
                .mapToPair(tuple -> new Tuple2(tuple._1,tuple._2)));

        tweets
                .foreachRDD(RDDTweets ->{
                    if(RDDTweets.count() > 0) {

                        hashTagCountsTotal.getAndSet(
                                hashTagCountsTotal.get()
                                        .union(RDDTweets
                                                .flatMap(tweet -> Arrays.asList(tweet.split(" ")).iterator())
                                                .filter(word -> word.startsWith("#"))
                                                .mapToPair(hashTag -> new Tuple2<>(hashTag,1))
                                                .reduceByKey(Integer::sum))
                                        .reduceByKey(Integer::sum));



                        hashTagCountsTotal.get()
                                .mapToPair(Tuple2::swap)
                                .sortByKey(false)
                                .take(10)
                                .forEach(System.out::println);


                    }
                });




     I left uncommented the code that I would prefer, despite that due to some bug is not sorting properly:
*/

        tweets.flatMap(tweet -> Arrays.asList(tweet.split(" ")).iterator())
                .filter(word -> word.startsWith("#"))
                .mapToPair(hashTag -> new Tuple2<>(hashTag,1))
                .reduceByKey((i1, i2) -> i1 + i2)
                .mapToPair(pair -> pair.swap())
                .transformToPair(pair -> pair.sortByKey(false))
                .foreachRDD(
                        rdd -> {
                            String out = "\nTop 10 hashtags (totals):\n";
                            for (Tuple2<Integer, String> t: rdd.take(10)) {
                                out = out + t.toString() + "\n";
                            }
                            System.out.println(out);
                        }
                );

        /*

        The same methodology is used to calculate the top 10 hashTags for the last 5 minutes of tweets, and
        continuously repeating this computation every 30 seconds. However, in this case reduceByKeyAndWindow is used.
        We would like to increase the information for new tweets and remove information from tweets outside the window
        range. This can be done using two different functions as seen below:

       */

        tweets.flatMap(tweet -> Arrays.asList(tweet.split(" ")).iterator())
                .filter(word -> word.startsWith("#"))
                .mapToPair(hashTag -> new Tuple2<>(hashTag,1))
                .reduceByKeyAndWindow(
                (i1, i2) -> i1 + i2,
                (i1, i2) -> i1 - i2,
                new Duration(60 * 5 * 1000),
                new Duration(30 * 1000))
                .mapToPair(pair -> pair.swap())
                .transformToPair(pair -> pair.sortByKey(false))
                .foreachRDD(
                        rdd -> {
                            String out = "\nTop 10 hashtags (windowed):\n";
                            for (Tuple2<Integer, String> t: rdd.take(10)) {
                                out = out + t.toString() + "\n";
                            }
                            System.out.println(out);
                        }
                );


        String checkpointDir = "/data";
        jssc.checkpoint(checkpointDir);
        jssc.start();

        try {
            jssc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
