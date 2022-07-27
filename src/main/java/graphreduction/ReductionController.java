package main.java.graphreduction;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;
import static org.neo4j.driver.Values.parameters;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketImpl;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryRunner;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionWork;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

/**
 * 
 * 
 * @author (classes and interfaces only, required)
 * @version (classes and interfaces only, required. See footnote 1)
 * @param (methods and constructors only)
 * @return (methods only)
 * @exception (@throws is a synonym added in Javadoc 1.2)
 * @see
*/
public class ReductionController implements AutoCloseable {

	private static Driver driver;

	public ReductionController(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	private enum RelTypes implements RelationshipType {
		KNOWS
	}

	static GraphDatabaseService graphDb;
	static Node firstNode;
	static Node secondNode;
	static Relationship relationship;
	static int mode = 0;
	//private static DatabaseManagementService managementService;

	private static long pid;

	public static void main(String... args) throws Exception {
		// TODO Auto-generated method stub

		// Ort wo die DB liegt: cooccsdatabase/data
		// Im Ordner databases
		// Im Ordner transactions

		/*
		 * String db_path = System.getProperty("user.dir") + "/cooccsdatabase/tests";
		 * File database = new File(db_path);
		 * 
		 * managementService = new DatabaseManagementServiceBuilder(database).build();
		 * graphDb = managementService.database(DEFAULT_DATABASE_NAME);
		 * 
		 * registerShutdownHook(managementService);
		 */

		boolean isAlive = isSocketAlive("localhost", 7687);

		System.out.println(isAlive);
		if (!isAlive) {

			System.out.println("Neo4j console muss zunächst gestartet werden.");
			Process p;
			try {

				Runtime rt = Runtime.getRuntime();
				p = rt.exec("/Users/kamir/Desktop/master/neo4j-community-4.4.4/bin/neo4j console");
				pid = p.pid();

				BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = reader.readLine()) != null) {
					System.out.println(line);
				}

				p.waitFor();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				/*
				 * try (ReductionController controller = new
				 * ReductionController("bolt://localhost:7687", "neo4j", "password")) {
				 * controller.printResult("hello, world"); }
				 */
			}
		}
		try (ReductionController controller = new ReductionController("bolt://localhost:7687", "neo4j", "password")) {
			controller.giveNodesClusterID();

			controller.getClusterIDsSizeDESC();
			int max = controller.getMaxSizeClusterID();

			// controller.deleteClustersThatAreNotID(max);

			/*
			 * String db_path = System.getProperty("user.dir") + "/cooccsdatabase/tests";
			 * File database = new File(db_path);
			 * 
			 * managementService = new DatabaseManagementServiceBuilder(database).build();
			 * graphDb = managementService.database(DEFAULT_DATABASE_NAME);
			 * 
			 * System.out.println("Co-occurrence database opened/created");
			 * 
			 * try (Transaction tx = graphDb.beginTx()) { org.neo4j.driver.Result result =
			 * ((QueryRunner) tx).run( "call gds.wcc.write(\n" + "{\n" +
			 * "nodeQuery: 'match (n) return id(n) as id',\n" +
			 * "relationshipQuery:'MATCH (a)-->(b) RETURN id(a) as source, id(b) as target',\n"
			 * + "writeProperty:'group',\n" + "consecutiveIds:true\n" + "}\n" + ")\n" +
			 * "YIELD nodePropertiesWritten\n" + "return nodePropertiesWritten;");
			 * System.out.println(result); return;
			 * 
			 * }
			 */
		}

	}

	private static void killProcess(long pid) {
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("kill " + pid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void giveNodesClusterID() {
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("call gds.wcc.write(\n" + "{\n" + "nodeQuery: 'match (n) return id(n) as id',\n"
								+ "relationshipQuery:'MATCH (a)-->(b) RETURN id(a) as source, id(b) as target',\n"
								+ "writeProperty:'group',\n" + "consecutiveIds:true\n" + "}\n" + ")\n"
								+ "YIELD nodePropertiesWritten\n" + "return nodePropertiesWritten;");
				return result.single().get(0);
			});
			System.out.println("Es wurde " + nodePropertiesWritten + " Knoten eine Cluster-ID vergeben.");
		}
	}

	class Cluster {
		public Cluster(int clusterId, int clusterSize) {
			this.clusterId = clusterId;
			this.clusterSize = clusterSize;
		}

		int clusterId;
		int clusterSize;
	}

	private static int getMaxSizeClusterID() {
		try (Session session = driver.session()) {

			// get(0) liefert n.group, get(1) liefert group_size
			int max = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("match (n) return n.group, count(n) as group_size order by group_size desc limit 1");
				return result.single().get(0).asInt();
			});
			System.out.println("Das Cluster mit der ID " + max + " ist das größte Cluster.");
			return max;
		}
	}

	private static List<Object> getClusterIDsSizeDESC() {
		try (Session session = driver.session()) {
			List<Object> names = new ArrayList<>();
			Object resultStr = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("match (n)\n" + "return n.group, count(n) as group_size\n" + "order by group_size desc");
				while (result.hasNext()) {
					names.add(result.next().get(0).asObject());
				}
				return names;
			});
			System.out.println("Generierte Cluster-IDs: " + resultStr);
			return names;
		}
	}

	private static void deleteClustersThatAreNotID(int id) {
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n) WHERE n.group <> " + id + " DETACH DELETE n");
				return result.single().get(0);
			});
			System.out.println("Es wurde alle Knoten die nicht zu Cluster " + id + " gehören gelöscht.");
		}
	}

	private static void registerShutdownHook(final DatabaseManagementService managementService) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				managementService.shutdown();
			}
		});
	}

	/**
	* Returns an Image object that can then be painted on the screen. 
	* The url argument must specify an absolute <a href="#{@link}">{@link URL}</a>. The name
	* argument is a specifier that is relative to the url argument. 
	* <p>
	* This method always returns immediately, whether or not the 
	* image exists. When this applet attempts to draw the image on
	* the screen, the data will be loaded. The graphics primitives 
	* that draw the image will incrementally paint on the screen. 
	*
	* @param  url  an absolute URL giving the base location of the image
	* @param  name the location of the image, relative to the url argument
	* @return      the image at the specified URL
	* @see         Image
	*/
	public static boolean isSocketAlive(String hostName, int port) {
		boolean isAlive = false;

		SocketAddress socketAddress = new InetSocketAddress(hostName, port);
		Socket socket = new Socket();

		int timeout = 2000;

		try {
			socket.connect(socketAddress, timeout);
			socket.close();
			isAlive = true;

		} catch (SocketTimeoutException exception) {
			System.out.println("SocketTimeoutException " + hostName + ":" + port + ". " + exception.getMessage());
		} catch (IOException exception) {
			System.out.println(
					"IOException - Unable to connect to " + hostName + ":" + port + ". " + exception.getMessage());

		}
		return isAlive;
	}

}
