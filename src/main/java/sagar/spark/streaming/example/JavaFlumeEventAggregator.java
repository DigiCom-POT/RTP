package sagar.spark.streaming.example;

import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;

import scala.Tuple2;
import au.com.bytecode.opencsv.CSVReader;

public class JavaFlumeEventAggregator {
	 private JavaFlumeEventAggregator() {
	  }

	  public static void main(String[] args) {
	    if (args.length != 2) {
	      System.err.println("Usage: JavaFlumeEventCount <host> <port>");
	      System.exit(1);
	    }

	   // StreamingExamples.setStreamingLogLevels();

	    String host = args[0];
	    int port = Integer.parseInt(args[1]);

	    Duration batchInterval = new Duration(2000);
	    SparkConf sparkConf = new SparkConf().setAppName("JavaFlumeAggregator");
	    JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, batchInterval);
	    JavaReceiverInputDStream<SparkFlumeEvent> flumeStream = FlumeUtils.createStream(ssc, host, port);
	    
	    processFlumeStream(flumeStream).print();

	    ssc.start();
	    ssc.awaitTermination();
	  }

	private static JavaPairDStream<String, Integer> processFlumeStream(
			JavaReceiverInputDStream<SparkFlumeEvent> flumeStream) {
		
		JavaDStream<String[]> csvData = flumeStream.map(new Function<SparkFlumeEvent, String[]>() {
			public String[] call(SparkFlumeEvent event) throws Exception {
				String line = new String(event.event().getBody().array());
				System.out.println("STRING READ---->" +line);
				@SuppressWarnings("deprecation")
				CSVReader reader = new CSVReader(new StringReader(line), '\t');
				String[] result = reader.readNext();
				return result;
			}
		});
	    JavaPairDStream<String, Integer> rdd = csvData.mapToPair(new PairFunction<String[], String, Integer>() {
			  public Tuple2<String, Integer> call(String[] x) {
				    return new Tuple2(x[12], 1);
				  }}).reduceByKey(
						  new Function2<Integer, Integer, Integer>() {
							    public Integer call(Integer a, Integer b) { return a + b; }
			});
	    
	    System.out.println("******** T E S T I N G *********");
	    return rdd;
	}
}
