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
import org.neo4j.driver.Record;
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
 * 
 * Vorgehen
 * Erstellen des Kookkurrenzgraphen
 * Auswahl der wichtigsten Knoten (Ranking-Klasse)
 * Markierung der wichtigsten Knoten von Hand (markNodes()-Methode)
 * 
 */
public class ReductionController implements AutoCloseable {

	private static Driver driver;
	
	private static Ranking ranking;

	public ReductionController(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
		ranking = new Ranking(driver);
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	static String alg = "";

	private static long pid;

	public static void main(String... args) throws Exception {

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

			// Set algorithm
			//
			// wcc - Weakly Connected Components Algorithm
			// coarse - Coarsening / Louvain Algorithmus
			// cluster - Clustering
			// kron - Kron Reduction

			alg = "";
			
			//createGraph();
			
			List<Record> rankingList = ranking.articleRank();
			
			markNodes(rankingList);
			
			showNodes();

			switch (alg) {
			case "wcc":
				WeaklyConnectedComponents wcc = new WeaklyConnectedComponents(driver);
				wcc.useWeaklyConnectedComponentsAlgorithm();
				break;
			case "coarse":

				break;
			case "cluster":

				break;
			case "kron":

				break;

			default:
				break;
			}
		}
	}
	
	private static void dropGraph(){
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("CALL gds.graph.drop('ukraine');");
				return result;
			});
			System.out.println(nodePropertiesWritten);
		}
	}
	
	private static void createGraph(){
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("CALL gds.graph.create('ukraine','SINGLE_NODE','IS_CONNECTED',{\n"
								+ "    relationshipProperties:['dice','cost']\n"
								+ "    })\n"
								+ "YIELD graphName, nodeCount, relationshipCount, createMillis;");
				return result;
			});
			System.out.println(nodePropertiesWritten);
		}
	}
	
	private static void markNodes(List<Record> nodeNames){
		for (Record record : nodeNames) {
			try (Session session = driver.session()) {
				Object nodes = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx
							.run("MATCH (n {name: " + record.get("name") + "})\n"
									+ "SET n.marked = true\n"
									+ "RETURN n");
					return result;
				});
			}

		}
		
	}
	
	private static void showNodes(){
	
		try (Session session = driver.session()) {
			Object nodes = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("MATCH (n) RETURN n.name, n.occur, n.marked LIMIT 10");
				return result.list();
			});
			System.out.println(nodes);
		}
	}

	/**
	 * Returns an Image object that can then be painted on the screen. The url
	 * argument must specify an absolute <a href="#{@link}">{@link URL}</a>. The
	 * name argument is a specifier that is relative to the url argument.
	 * <p>
	 * This method always returns immediately, whether or not the image exists. When
	 * this applet attempts to draw the image on the screen, the data will be
	 * loaded. The graphics primitives that draw the image will incrementally paint
	 * on the screen.
	 *
	 * @param url  an absolute URL giving the base location of the image
	 * @param name the location of the image, relative to the url argument
	 * @return the image at the specified URL
	 * @see Image
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
	
	// ------------------------------------------------------------------------------
	
	private static void killProcess(long pid) {
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("kill " + pid);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
