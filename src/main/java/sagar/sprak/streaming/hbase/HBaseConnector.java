package sagar.sprak.streaming.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;

/*
 * Class that establishes a connection with hbase and returns an HBaseConfiguration object
 */
public class HBaseConnector {
	private HBaseConfiguration conf;
	
	private Configuration config;
	
	public final HBaseConfiguration getHBaseConf(final String pathToHBaseXMLFile) {
		conf = new HBaseConfiguration();
		conf.addResource(new Path(pathToHBaseXMLFile));
		return conf;
	}
	
	public final Configuration getHbaseConfig(final String pathToHBaseXMLFile) {
		config = HBaseConfiguration.create();
		config.addResource(new Path(pathToHBaseXMLFile));
		return config;
	}
}
