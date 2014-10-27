package sagar.sprak.streaming.hbase;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
/*
 * Bolt for dumping stream data into HBase table
 */
public class HBaseUtil {

	private static final long serialVersionUID = 1L;
	
	private String rowKeyCheck = null, rowKey = null, fieldValue = null, tableName = null;;
	
	private ArrayList<String> colFamilyNames = new ArrayList<String>();
	private ArrayList<ArrayList<String>>  colNames = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> colValues = new ArrayList<ArrayList<String>>();
	private ArrayList<String> colFamilyValues = new ArrayList<String>();


	private static transient HBaseConnector connector = null;
	private static transient HBaseConfiguration conf = null;
	private static transient HBaseCommunicator communicator = null;
	private static transient Configuration config =null; 

	/*
	 * Constructor initializes the variables storing the hbase table information, connects to hbase and checks if the table already exists
	 */
	public HBaseUtil(final String hbaseXmlLocation, final String tableName, final String rowKeyCheck, final ArrayList<String> colFamilyNames, final ArrayList<ArrayList<String>> colNames) {
		this.tableName = tableName;
		this.colFamilyNames = colFamilyNames;
		this.colNames = colNames;
		this.rowKeyCheck = rowKeyCheck;
		connector = new HBaseConnector();
		
		/** Testing **/
		/*Configuration nconf = HBaseConfiguration.create();
		nconf.set("hbase.zookeeper.quorum", "sandbox.hortonworks.com");
		nconf.set("hbase.zookeeper.property.clientPort", Integer.toString(2181));
        nconf.set("hbase.client.retries.number", Integer.toString(0));
        nconf.set("zookeeper.session.timeout", Integer.toString(60000));
        nconf.set("zookeeper.recovery.retry", Integer.toString(0));
		nconf.setStrings("log4j.logger.org.apache.hadoop.hbase", "DEBUG");		
		try {
			System.out.println(" <--> ");
			HBaseAdmin admin = new HBaseAdmin(nconf);
			System.out.println(" -- " + admin.tableExists("sparkTable"));
		} catch (MasterNotRunningException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		/** Testing **/
		System.out.println("1-----------------------------");
		//conf = connector.getHBaseConf(hbaseXmlLocation);
		config = connector.getHbaseConfig(hbaseXmlLocation);
		System.out.println("2-----------------------------");
		communicator = new HBaseCommunicator(config);
		//check if tableName already exists
		System.out.println("3-----------------------------");
		if (colFamilyNames.size() == colNames.size()) {
			System.out.println("4-----------------------------");
			if (!communicator.tableExists(tableName)) {
				System.out.println(" Creatting Table -" +  tableName);
				communicator.createTable(tableName, colFamilyNames);
			}
		}
		System.out.println(" Step 6");
	}

	/*
	 * For every input tuple creates a list of values to be dumped as a row, depending upon the column names of the hbase table
	 * @see backtype.storm.topology.IBasicBolt#execute(backtype.storm.tuple.Tuple, backtype.storm.topology.BasicOutputCollector)
	 */
	public void addEntry(String movieID, Integer count) {
		System.out.println("Writing to HBASE");
		ArrayList<String> internalcolValues = new ArrayList<String>();
		internalcolValues.add(Integer.toString(count));
		colValues.add(internalcolValues);
		communicator.addRow(movieID, tableName, colFamilyNames, colNames, colValues);
		System.out.println("Written to HBASE");
	}

}

