package digicom.pot.batch.spark;

import java.io.StringReader;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;

import scala.Tuple2;
import au.com.bytecode.opencsv.CSVReader;
import digicom.pot.rtp.cassandra.CassandraConnector;

public class TopRatedMovieAggregation {
	
	private TopRatedMovieAggregation() {
	}

	public static void main(String[] args) {
		
		// Should change the input and out medium to hdfs. 
		 String tsvFile = "/tmp/movielens/0.tsv"; // Should be some file on your system
		 SparkConf conf = new SparkConf().setAppName("Batch Ratings Counts");
		 JavaSparkContext sc = new JavaSparkContext(conf);
		 JavaRDD<String> ratingsData = sc.textFile(tsvFile);
		 
		 JavaRDD<String[]> csvData = ratingsData.map(new ParseLine());

		JavaPairRDD<String, Integer> rdd = csvData.mapToPair(new PairFunction<String[], String, Integer>() {
				  public Tuple2<String, Integer> call(String[] x) {
					    return new Tuple2(x[1], 1);
					  }}).reduceByKey(
							  new Function2<Integer, Integer, Integer>() {
								    public Integer call(Integer a, Integer b) { return a + b; }
		});
	
		System.out.println("---Printing the results ---" +rdd.count());
		persistToCassandra(rdd);
		sc.stop();
	}


	public static class ParseLine implements Function<String, String[]> {
		private static final long serialVersionUID = 1L;

		public String[] call(String line) throws Exception {
			//System.out.println("Line " + line);
			@SuppressWarnings("resource")
			CSVReader reader = new CSVReader(new StringReader(line), ',');
			String[] arr = reader.readNext();
			//System.out.println("ARRAY --" + Arrays.toString(arr));
			return arr;
		}
	}

	/**
	 * Persisting the data tuple to Cassandra
	 * @param rdd
	 */
	@SuppressWarnings("serial")
	private static void persistToCassandra(JavaPairRDD<String, Integer> rdd) {
		System.out.println("Trying to persisting to Cassandra");
		try {
			rdd.foreach(new VoidFunction<Tuple2<String,Integer>>() {
				public void call(Tuple2<String, Integer> tuple) throws Exception {
					CassandraConnector.persistnew(tuple._1(), tuple._2());
				}
			});
			
		} catch (Exception e) {
			System.out.println("Error in Persisting to Cassandra - " + e);
		}

	}


	/** 
	 * Initializing the Cassandra data connection
	 */
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
