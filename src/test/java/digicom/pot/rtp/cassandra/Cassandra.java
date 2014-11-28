package digicom.pot.rtp.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class Cassandra {

	private Cluster cluster;
	private static Session session;
	private final String KEYSPACE = "test";
	

	private void close() {
		cluster.close();
	}

	/**
	 * Connect to keyspace - "test"
	 * @param node
	 * @return cassandra session
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
	
	
	
	public void test() {
		Cassandra client = new Cassandra();
		session = client.connect("127.0.0.1");
		String cqlStatement = "SELECT * FROM test";
	    for (Row row : session.execute(cqlStatement)) {
	        System.out.println(row.toString());
	    }
		client.close();
	}
	
	public static void main(String[] args) {
		Cassandra cas = new Cassandra();
		cas.test();
	}
}
