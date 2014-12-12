package digicom.pot.rtp.spark.streaming;

import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.flume.FlumeUtils;
import org.apache.spark.streaming.flume.SparkFlumeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import au.com.bytecode.opencsv.CSVReader;
import digicom.pot.rtp.cassandra.CassandraConnector;

public class TopRatedMovieAggregation implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	private static final long POLLING_TIME_MS = 2000;
	Logger logger = LoggerFactory.getLogger(TopRatedMovieAggregation.class);

	private TopRatedMovieAggregation() {
	}

	/**
	 * Take the Avro flume host and port 
	 * where flume sinks the data
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: JavaFlumeEventCount <host> <port>");
			System.exit(1);
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		TopRatedMovieAggregation topRatedMovieAgg = new TopRatedMovieAggregation();
		topRatedMovieAgg.processStreamingRatings(host, port);
	}

	/**
	 * Process the realtime streaming data from Avro sink 
	 * @param host
	 * @param port
	 */
	private void processStreamingRatings(String host, int port) {
		// Time interval to poll from avro sink
		Duration batchInterval = new Duration(POLLING_TIME_MS);
		SparkConf sparkConf = new SparkConf()
				.setAppName("MovieFlumeAggregator");
		JavaStreamingContext ssc = new JavaStreamingContext(sparkConf,
				batchInterval);
		JavaReceiverInputDStream<SparkFlumeEvent> flumeStream = FlumeUtils
				.createStream(ssc, host, port);
		JavaPairDStream<String, Integer> rdd = processFlumeStream(flumeStream);

		logger.info("---Printing the results ---");
		rdd.print();
		persistToCassandra(rdd);
		ssc.start();
		ssc.awaitTermination();
	}

	/**
	 * Persisting the data tuple to Cassandra
	 * 
	 * @param rdd
	 */
	private void persistToCassandra(JavaPairDStream<String, Integer> rdd) {
		logger.info("Persisting data to Cassandra");
		final CassandraConnector cassandraConnector = new CassandraConnector();
		try {
			rdd.foreach(new Function2<JavaPairRDD<String, Integer>, Time, Void>() {
				public Void call(JavaPairRDD<String, Integer> value, Time time)
						throws Exception {
					value.foreach(new VoidFunction<Tuple2<String, Integer>>() {
						public void call(Tuple2<String, Integer> tuple)
								throws Exception {
							cassandraConnector.persistRealTimeRatings(
									tuple._1(), tuple._2());
						}
					});
					return null;
				}
			});
		} catch (Exception e) {
			logger.info("Error in Persisting to Cassandra - " + e);
		}
	}

	/**
	 * Processing the input data flow
	 * 
	 * @param flumeStream
	 * @return
	 */
	private JavaPairDStream<String, Integer> processFlumeStream(
			JavaReceiverInputDStream<SparkFlumeEvent> flumeStream) {

		JavaDStream<String[]> csvData = flumeStream
				.map(new Function<SparkFlumeEvent, String[]>() {
					public String[] call(SparkFlumeEvent event)
							throws Exception {
						String line = new String(event.event().getBody()
								.array());
						logger.info("Data input ---->" + line);
						CSVReader reader = new CSVReader(
								new StringReader(line), ',');
						String[] result = reader.readNext();
						return result;
					}
				});
		JavaPairDStream<String, Integer> rdd = csvData.mapToPair(
				new PairFunction<String[], String, Integer>() {
					public Tuple2<String, Integer> call(String[] x) {
						return new Tuple2(x[1], 1);
					}
				}).reduceByKey(new Function2<Integer, Integer, Integer>() {
			public Integer call(Integer a, Integer b) {
				return a + b;
			}
		});

		logger.info("******** Data consumption started *********");
		initializeCassandra();
		return rdd;
	}

	/**
	 * Initializing the Cassandra data connection
	 */
	private void initializeCassandra() {
		logger.info("Printing Cassandra Connection");
		try {
			CassandraConnector cc = new CassandraConnector();
			cc.init();
		} catch (Exception e) {
			logger.info("Error connect the cassandra" + e);
		}
	}
}
