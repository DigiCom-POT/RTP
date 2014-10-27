package sagar.spark.example;

import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import scala.Tuple2;
import au.com.bytecode.opencsv.CSVReader;

public class SimpleAggregatorApp {
	
		public static class ParseLine implements Function<String, String[]> {
			public String[] call(String line) throws Exception {
				CSVReader reader = new CSVReader(new StringReader(line), '\t');
				return reader.readNext();
			}
		}
	
				
	  public static void main(String[] args) {
		    String tsvFile = "/tmp/0.tsv"; // Should be some file on your system
		    SparkConf conf = new SparkConf().setAppName("Simple Aggregator Application");
		    JavaSparkContext sc = new JavaSparkContext(conf);
		    
		    JavaRDD<String> logData = sc.textFile(tsvFile);
		    JavaRDD<String[]> csvData = logData.map(new ParseLine());

		    JavaPairRDD<String, Integer> rdd = csvData.mapToPair(new PairFunction<String[], String, Integer>() {
				  public Tuple2<String, Integer> call(String[] x) {
					    return new Tuple2(x[12], 1);
					  }}).reduceByKey(
							  new Function2<Integer, Integer, Integer>() {
								    public Integer call(Integer a, Integer b) { return a + b; }
				});
		    
		    rdd.saveAsTextFile("/tmp/output");
		  }
}
