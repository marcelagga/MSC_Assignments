/*

Name: Marcel Aguilar Garcia
Student ID: 20235620

 */

import java.util.Arrays;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.classification.SVMWithSGD;
import org.apache.spark.mllib.classification.SVMModel;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.feature.HashingTF;
import scala.Tuple2;
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics;

public class MyClass_Q2 {

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

        By using textFile I am reading the file imdb_labelled.txt
        into an RDD as a collection of lines

         */

        JavaRDD<String> data = sc.textFile("imdb_labelled.txt");

        /*

        HashingTF will be use to map a sequence of word to their term frequencies

         */

        final HashingTF tf = new HashingTF(100);


        /*

        I am using a JavaRDD of class LabeledPoint in order to store each
        of the lines from the text file in the format of: (label,sentence_features),
        where the label can be 0 (negative sentiment) or 1 (positive sentiment).

        For this assignment I have used HashingTF to retrieve the sentence features
        (in a similar way than the SPAM filter from the lectures).

        Therefore, the sentence features will be given by a numerical vector
        with the term frequencies of each sentence.

         */

       JavaRDD<LabeledPoint> labeledData = data.map(line-> {
           Integer label = Integer.parseInt(line.substring(line.length()-1));
           String text = line.substring(0,line.length()-1).trim();
           return new LabeledPoint(label, tf.transform(Arrays.asList(text.split(" "))));
       });

       /*

       The dataset can be split with the training set (60% of the data) and the test set (40% of the data).

        */

       JavaRDD<LabeledPoint>[] splits = labeledData.randomSplit(new double[]{0.6,0.4},10L);
       JavaRDD<LabeledPoint> trainingData = splits[0];
       JavaRDD<LabeledPoint> testData = splits[1];

       /*

        Finally, I am training a Support Vector Machine with Stochastic Gradient Descent and 100 iterations
        with the obtained train data

        */

       SVMModel SVMlearner = SVMWithSGD.train(trainingData.rdd(),100);

       /*

        Now, I would like to store the predictions for the testData and,
        at the same time, compare them with the real labels. In order to do
        this I first store each prediction with their related target value
        in a Tuple2 as seen below.

        Note: testData is being cached as I need to use this RDD twice in each
        of the iterations.

        */

       testData.cache();

       JavaPairRDD<Object,Object> predictionAndLabels = testData.mapToPair(example->
               new Tuple2<>(SVMlearner.predict(example.features()),example.label()));

       /*

       Finally, I am creating a new instance of BinaryClassificationsMetrics that will be used
       to compute the metric AUROC with the Tuple2 that we have created. In this case,
       the AUROC is of 0.5908...While this AUROC is greater than 0.5 we would like ideally
       an AUROC of 0.7 or greater.

        */

       BinaryClassificationMetrics metrics = new BinaryClassificationMetrics(predictionAndLabels.rdd());
       System.out.println("AUROC for the model is  " + metrics.areaUnderROC());

    }
}
