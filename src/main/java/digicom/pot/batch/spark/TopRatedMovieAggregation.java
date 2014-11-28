package digicom.pot.batch.spark;

import java.io.Serializable;
import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;
import au.com.bytecode.opencsv.CSVReader;
import digicom.pot.rtp.cassandra.CassandraConnector;

public class TopRatedMovieAggregation implements Serializable{

	private static final long serialVersionUID = 1L;
	CassandraConnector cassandraConnector = new CassandraConnector();
	Logger logger = LoggerFactory
			.getLogger(TopRatedMovieAggregation.class);

	private TopRatedMovieAggregation() {
	}

	public static void main(String[] args) {
		// can change the input and out medium to hdfs.
		String tsvFile = "/home/sapepot/dataset/ratings.csv";

		TopRatedMovieAggregation topmovAggregation = new TopRatedMovieAggregation();
		topmovAggregation.processBatchRatings(tsvFile, topmovAggregation);
	}

	@SuppressWarnings("serial")
	private void processBatchRatings(String tsvFile,
			TopRatedMovieAggregation topmovAggregation) {
		SparkConf conf = new SparkConf().setAppName("Batch Ratings Counts");
		JavaSparkContext sc = new JavaSparkContext(conf);
		JavaRDD<String> ratingsData = sc.textFile(tsvFile);

		JavaRDD<String[]> csvData = ratingsData.map(new ParseLine());

		JavaPairRDD<String, Integer> rdd = csvData.mapToPair(
				new PairFunction<String[], String, Integer>() {
					public Tuple2<String, Integer> call(String[] x) {
						return new Tuple2(x[1], 1);
					}
				}).reduceByKey(new Function2<Integer, Integer, Integer>() {
			public Integer call(Integer a, Integer b) {
				return a + b;
			}
		});

		logger.info("Printing the results ---" + rdd.count());
		topmovAggregation.persistToCassandra(rdd);
		sc.stop();
	}

	/**
	 * Inner class to read input stream as csv
	 * 
	 *
	 */
	public class ParseLine implements Function<String, String[]> {
		private static final long serialVersionUID = 1L;

		public String[] call(String line) throws Exception {
			@SuppressWarnings("resource")
			CSVReader reader = new CSVReader(new StringReader(line), ',');
			String[] arr = reader.readNext();
			return arr;
		}
	}

	/**
	 * Persisting the data tuple to Cassandra
	 * 
	 * @param rdd
	 */
	@SuppressWarnings("serial")
	private void persistToCassandra(JavaPairRDD<String, Integer> rdd) {
		logger.info("Trying to persisting to Cassandra");
		try {
			rdd.foreach(new VoidFunction<Tuple2<String, Integer>>() {
				public void call(Tuple2<String, Integer> tuple)
						throws Exception {
					cassandraConnector.persistBatchRatings(tuple._1(),
							tuple._2());
				}
			});

		} catch (Exception e) {
			logger.info("Error in Persisting to Cassandra - " + e);
		}

	}

}
