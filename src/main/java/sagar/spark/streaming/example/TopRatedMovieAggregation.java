package sagar.spark.streaming.example;

import java.io.StringReader;
import java.util.ArrayList;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;

import sagar.cassandra.CassandraConnector;
import sagar.sprak.streaming.hbase.HBaseUtil;
import scala.Tuple2;
import au.com.bytecode.opencsv.CSVReader;

public class TopRatedMovieAggregation {
	 private TopRatedMovieAggregation() {
	  }

	  public static void main(String[] args) {
	    if (args.length < 2) {
	      System.err.println("Usage: JavaFlumeEventCount <host> <port>");
	      System.exit(1);
	    }

	   // StreamingExamples.setStreamingLogLevels();
	    
	    // For Hbase
	    String tableName = "sparkTest";
		String hbaseXmlFileLocation = "/usr/lib/hbase/conf/hbase-site.xml";
		ArrayList<String> columnNames = new ArrayList<String>();
		ArrayList<String> columnFamilyNames = new ArrayList<String>();
		ArrayList<ArrayList<String>> columnNamesList  = new ArrayList<ArrayList<String>>();
		columnFamilyNames.add("movcounter");
		columnNames.add("counter");
		columnNamesList.add(columnNames);
		String rowKey = "movieID";
	    //
	    
		HBaseUtil util = null;

	    String host = args[0];
	    int port = Integer.parseInt(args[1]);

	    String hbaseUse = args.length == 3 ? args[2] : "";
	    boolean useHbase = false;
	    if(hbaseUse.equalsIgnoreCase("hbase")) {
	    	useHbase = true;
	    }
	    
	    if(useHbase) {
	    	util = new HBaseUtil(hbaseXmlFileLocation, tableName, rowKey, columnFamilyNames, columnNamesList);
	    }
	    
	    Duration batchInterval = new Duration(2000);
	    SparkConf sparkConf = new SparkConf().setAppName("MovieFlumeAggregator");
	    JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, batchInterval);
	    JavaReceiverInputDStream<SparkFlumeEvent> flumeStream = FlumeUtils.createStream(ssc, host, port);

	    final Broadcast<String> broadcastTableName = ssc.sparkContext().broadcast("sparkhbase");
		final Broadcast<String> broadcastColumnFamily = ssc.sparkContext().broadcast("c");

	    
	    //processFlumeStream(flumeStream).print();
		JavaPairDStream<String, Integer> rdd = processFlumeStream(flumeStream);
		
		System.out.println("---Printing the results ---");
		rdd.print();
		persistToCassandra(rdd);
		if(useHbase) {
			System.out.println("---Persisting to Hbase ---");
			//persistToHbase(broadcastTableName, broadcastColumnFamily, rdd);
			persistToHbase(broadcastTableName, broadcastColumnFamily, rdd, util);
		}
	    ssc.start();
	    ssc.awaitTermination();
	  }

	/*private static void persistToHbase(
			final Broadcast<String> broadcastTableName,
			final Broadcast<String> broadcastColumnFamily,
			JavaPairDStream<String, Integer> rdd) {
		try {
			rdd.foreach(new Function2<JavaPairRDD<String,Integer>, Time, Void>() {
				public Void call(JavaPairRDD<String, Integer> value, Time time)
						throws Exception {
					value.foreach(new VoidFunction<Tuple2<String,Integer>>(){
						public void call(Tuple2<String, Integer> tuple)
								throws Exception {
							    System.out.println("Counter1 :" + tuple._1() + "," + tuple._2());
								HbaseUpdate update = 
										HbaseUpdate.getInstance(broadcastTableName.value(), broadcastColumnFamily.value());
								System.out.println("Counter2:" + tuple._1() + "," + tuple._2());
								update.increment("Counter", tuple._1(), tuple._2());
								System.out.println("Counter3:" + tuple._1() + "," + tuple._2());
						}
					});
					return null;
				}
		    });
		} catch (Exception e) {
			System.out.println("Error in Persisting to Hbase - " + e);
		}
		
	}*/

	  private static void persistToHbase(
				final Broadcast<String> broadcastTableName,
				final Broadcast<String> broadcastColumnFamily,
				JavaPairDStream<String, Integer> rdd, final HBaseUtil util) {
			try {
				rdd.foreach(new Function2<JavaPairRDD<String,Integer>, Time, Void>() {
					public Void call(JavaPairRDD<String, Integer> value, Time time)
							throws Exception {
						value.foreach(new VoidFunction<Tuple2<String,Integer>>(){
							public void call(Tuple2<String, Integer> tuple)
									throws Exception {
								    System.out.println("Counter1 :" + tuple._1() + "," + tuple._2());
								    util.addEntry(tuple._1(), tuple._2());
									System.out.println("Counter3:" + tuple._1() + "," + tuple._2());
							}
						});
						return null;
					}
			    });
			} catch (Exception e) {
				System.out.println("Error in Persisting to Hbase - " + e);
			}
			
		}  
	  
	  private static void persistToCassandra(JavaPairDStream<String, Integer> rdd) {
		  System.out.println("Trying to persisting to Cassandra");
			try {
				rdd.foreach(new Function2<JavaPairRDD<String,Integer>, Time, Void>() {
					public Void call(JavaPairRDD<String, Integer> value, Time time)
							throws Exception {
						value.foreach(new VoidFunction<Tuple2<String,Integer>>(){
							public void call(Tuple2<String, Integer> tuple)
									throws Exception {
								   // System.out.println("Counter1 :" + tuple._1() + "," + tuple._2());
								    CassandraConnector.persist(tuple._1(), tuple._2());
							}
						});
						return null;
					}
			    });
			} catch (Exception e) {
				System.out.println("Error in Persisting to Cassandra - " + e);
			}
			
		}

	private static JavaPairDStream<String, Integer> processFlumeStream(
			JavaReceiverInputDStream<SparkFlumeEvent> flumeStream) {
		
		JavaDStream<String[]> csvData = flumeStream.map(new Function<SparkFlumeEvent, String[]>() {
			public String[] call(SparkFlumeEvent event) throws Exception {
				String line = new String(event.event().getBody().array());
				System.out.println("STRING READ---->" +line);
				@SuppressWarnings("deprecation")
				CSVReader reader = new CSVReader(new StringReader(line), ',');
				String[] result = reader.readNext();
				return result;
			}
		});
	    JavaPairDStream<String, Integer> rdd = csvData.mapToPair(new PairFunction<String[], String, Integer>() {
			  public Tuple2<String, Integer> call(String[] x) {
				    return new Tuple2(x[1], 1);
				  }}).reduceByKey(
						  new Function2<Integer, Integer, Integer>() {
							    public Integer call(Integer a, Integer b) { return a + b; }
			});
	    
	    System.out.println("******** T E S T I N G *********");
	    printCassandraConnection();
	    
	    return rdd;
	}

	private static void printCassandraConnection() {
		System.out.println("Printing Cassandra Connection");
		try {
			CassandraConnector cc = new CassandraConnector();
			cc.init();
		} catch (Exception e) {
			System.out.println("Error connect the cassandra" + e);
		}
		
		
	}
}
