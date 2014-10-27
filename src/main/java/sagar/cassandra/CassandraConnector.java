package sagar.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class CassandraConnector {
	private static Cluster cluster;
	private static Session session;
	static CassandraConnector client = new CassandraConnector();

	private static PreparedStatement ps;
	private static PreparedStatement load;
	private static PreparedStatement update;
	
	public void init() {
		session = client.connect("127.0.0.1");
		ps = session.prepare("INSERT INTO top_movie (movieid, viewscnt, time) VALUES (?, ?, dateof(now()))");
		load = session.prepare("select viewscnt from top_movie where movieid=?");
		String cqlStatement = "SELECT * FROM test";
		for (Row row : session.execute(cqlStatement)) {
			System.out.println(row.toString());
		}
		// client.close();
	}

	public static void persist(String movieid, Integer count) {

		try {
			if (null == session) {
				session = client.connect("127.0.0.1");
				ps = session.prepare("INSERT INTO top_movie (movieid, viewscnt, time) VALUES (?, ?, dateof(now()))");
				load = session.prepare("select viewscnt from top_movie where movieid=?");
			}
			long existingcount = getExistingCount(movieid);
			long l = count + existingcount;
			
			Long viewscnt = new Long(l);
			BoundStatement bind = ps.bind(movieid, viewscnt);
			session.execute(bind);
			
			if(existingcount == 0) {
				System.out.println("Inserted the data for " + movieid +  "with value : "+l);
			}else{
				System.out.println("Updating the data for " + movieid+  "with value : "+l);
			}
			

		} catch (Exception e) {
			System.out.println(" Error while persisting the data in cassandra "
					+ e);
			e.printStackTrace();
		}
	}

	private static long getExistingCount(String movieid) {
		long value = 0;
		try {
			ResultSet result = session.execute(load.bind(movieid));
			if( !result.isExhausted()) {
				Row one = result.one();
				value = one.getLong("viewscnt");
				System.out.println(" Got the value for movie " + movieid + " value :" + value);
			}
		} catch (Exception e) {
			System.out.println(" Exception while getting the count for movie " + movieid);
		}
		return value;
	}

	private void close() {
		cluster.close();
	}

	private Session connect(String node) {
		cluster = Cluster.builder().addContactPoint(node).build();
		Metadata metadata = cluster.getMetadata();
		System.out.printf("Connected to cluster: %s\n",
				metadata.getClusterName());
		for (Host host : metadata.getAllHosts()) {
			System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
					host.getDatacenter(), host.getAddress(), host.getRack());
		}
		return cluster.connect("test");
	}

}
