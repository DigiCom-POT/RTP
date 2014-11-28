package digicom.pot.rtp.cassandra;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraConnector implements Serializable{
	private static final long serialVersionUID = 1L;
	private static final String KEYSPACE = "test";
	private static Cluster cluster;
	private static Session session;
	static CassandraConnector client = new CassandraConnector();

	private static PreparedStatement ps;
	private static PreparedStatement pbs;
	private static PreparedStatement load;

	Logger logger = LoggerFactory.getLogger(CassandraConnector.class);

	/*
	 * initialing the session and loading prepared statements
	 */
	public void init() {
		session = client.connect("127.0.0.1");
		ps = session
				.prepare("INSERT INTO top_movie (movieid, viewscnt, time) VALUES (?, ?, dateof(now()))");
		pbs = session
				.prepare("INSERT INTO top_movie_new (movieid, viewscnt, time) VALUES (?, ?, dateof(now()))");
		load = session
				.prepare("select viewscnt from top_movie where movieid=?");
	}

	/**
	 * Persisting (updating) the real time data 
	 * (from Spark Streaming)
	 * @param movieid
	 * @param count
	 */
	public void persistRealTimeRatings(String movieid, Integer count) {

		try {
			if (null == session) {
				init();
			}
			long existingcount = getExistingCount(movieid);
			long l = count + existingcount;

			Long viewscnt = new Long(l);
			BoundStatement bind = ps.bind(movieid, viewscnt);
			session.execute(bind);

			if (existingcount == 0) {
				logger.info("Inserted the data for " + movieid
						+ "with value : " + l);
			} else {
				logger.info("Updating the data for " + movieid
						+ "with value : " + l);
			}

		} catch (Exception e) {
			logger.error(" Error while persisting the data in cassandra " + e);
		}
	}

	/**
	 * Persisting the batch data (from spark)
	 * @param movieid
	 * @param count
	 */
	public void persistBatchRatings(String movieid, Integer count) {
		try {
			if (null == session) {
				init();
			}
			long existingcount = getExistingCount(movieid);
			long l = count + existingcount;

			Long viewscnt = new Long(l);
			BoundStatement bind = pbs.bind(movieid, viewscnt);
			session.execute(bind);

			if (existingcount != 0) {
				logger.info("Inserted the data for " + movieid
						+ "with value : " + l);
			}

		} catch (Exception e) {
			logger.error(" Error while persisting the data in cassandra " + e);
		}
	}
	
	private long getExistingCount(String movieid) {
		long value = 0;
		try {
			ResultSet result = session.execute(load.bind(movieid));
			if (!result.isExhausted()) {
				Row one = result.one();
				value = one.getLong("viewscnt");
				logger.info(" Got the value for movie " + movieid + " value :"
						+ value);
			}
		} catch (Exception e) {
			logger.info(" Exception while getting the count for movie "
					+ movieid);
		}
		return value;
	}

	/**
	 * Creating Session for the Keyspace "test"
	 * @param node
	 * @return
	 */
	private Session connect(String node) {
		cluster = Cluster.builder().addContactPoint(node).build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}
		return cluster.connect(KEYSPACE);
	}

	private void close() {
		cluster.close();
	}

}
