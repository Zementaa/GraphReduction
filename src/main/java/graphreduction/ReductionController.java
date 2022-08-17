package main.java.graphreduction;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

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
 * Auswahl der wichtigsten Knoten (Centrality-Klasse)
 * Markierung der wichtigsten Knoten von Hand (markNodes()-Methode)
 * 
 */
public class ReductionController implements AutoCloseable {

	private static Driver driver;
	
	private static Centrality centrality;
	
	private static PathFinding pathFinding;
	
	private static CommunityDetection communityDetection;
	
	private static final String GRAPH_NAME = "ukraine";


	public ReductionController(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
		centrality = new Centrality(driver);
		pathFinding = new PathFinding(driver);
		communityDetection = new CommunityDetection(driver);
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public static void main(String[] args) throws Exception {

		boolean isAlive = isSocketAlive("localhost", 7687);

		System.out.println("Neo4j Console wurde gestartet: " + isAlive);
		if (!isAlive) {
			System.out.println("Neo4j console muss zunächst gestartet werden.");
			return;
		}
		try (ReductionController controller = new ReductionController("bolt://localhost:7687", "neo4j", "password")) {

			/* Algorithms overview
			*
			* Preprocessing
			* 	wcc - Weakly Connnected Components
			*
			* Centrality
			* 	between - Betweenness
			* 	degree - Degree Centrality
			* 
			* Community Detection
			* 	louvain - Louvain Algorithmus
			* 	labelP - Label Propagation
			* 	modularity - Modularity Optimization
			* 	
			* Path Finding
			* 	randomW - Random Walker
			*/
			
			// Preprocessing
			// Find nodes that are not connected to main graph and delete those
			WeaklyConnectedComponents wcc = new WeaklyConnectedComponents(driver);
			//wcc.useWeaklyConnectedComponentsAlgorithm();
			
			// create graph if not exists
			createGraph(GRAPH_NAME);
			
			// load to be marked node names
			List<String> nodes = openList();
			
			// mark all interesting nodes
			markNodes(nodes);
			
			//showNodes(graphName);
			
			
			// Set algorithm
			String alg = "modularity";
			String mode = "stream";  // stream, write, stats, mutate
			
			useAlgorithm(alg, mode);	
			
		}
	}
	
	private static void useAlgorithm(String alg, String mode){
		List<Record> records = new ArrayList<Record>();
		String info = "";
		switch (alg) {
			case "between":
				records = centrality.betweenness(GRAPH_NAME);
				info = "centrality";
				break;
			case "degree":
				records = centrality.degreeCrentrality(GRAPH_NAME, "cost");
				info = "centrality";
				break;
			case "louvain":
				records = communityDetection.louvain(GRAPH_NAME);
				info = "community";
				break;
			case "labelP":
				records = communityDetection.labelPropagation(GRAPH_NAME);
				info = "community";
				break;
			case "modularity":
				
				if(mode=="stream") {
					communityDetection.modularityOptimization(GRAPH_NAME);
				}
				if(mode=="write") {
					communityDetection.modularityOptimizationWrite(GRAPH_NAME);
				}
				records = communityDetection.getIds(GRAPH_NAME);
				
				//info = "community";
				
				List<String> communities = new ArrayList<String>();
				//communities.add("SINGLE_NODE");
				
				records.forEach(record -> {
					
					List<Record> communityNodes = getNodesByCommunityId(record.get("communityId").asInt());
					communityNodes.forEach(node -> {
	
						setNodeLabel(node.get(0).asString(), node.get(3).asInt());
						if(!communities.contains("Community" + node.get(3).asInt())) {
							communities.add("Community" + node.get(3).asInt());
						}
						
					});
				});
				
				recreateGraph(communities);
				
				List<Record> list = new ArrayList<Record>();
				
				communities.forEach(com -> {
					list.addAll(centrality.betweennessWithNodeLabel(GRAPH_NAME, com, Integer.parseInt(com.replaceAll("\\D+",""))));
				});
				//exportToCSV(list, "modularity-between", "centrality");
				
				records = list;
				info = "centrality";
				alg = "modularity-between";
				
				break;
			case "randomW":
				records = pathFinding.randomWalk(GRAPH_NAME);
				info = "path";
				break;
			case "maus":
				records = centrality.betweenness(GRAPH_NAME);
				info = "centrality";
				break;
	
			default:
				break;
		}
		exportToCSV(records, alg, info);
	}
	
	private static void dropGraph(String graphName){
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("CALL gds.graph.drop('" + graphName + "');");
				return result;
			});
			System.out.println(nodePropertiesWritten);
		}
	}
	
	private static void createGraph(String graphName){
		
		String exists;
		
		dropGraph(GRAPH_NAME);
		
		try (Session session = driver.session()) {
			exists = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("RETURN gds.graph.exists('" + graphName + "')");
				return result.single().get(0).toString();
			});
			System.out.println("Graph 'ukraine' existiert bereits? " + exists);
		}
		
		if(exists!="TRUE") {
			try (Session session = driver.session()) {
				Object nodePropertiesWritten = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx
							.run("CALL gds.graph.create('" + graphName + "','SINGLE_NODE','IS_CONNECTED',{\n"
									+ "    relationshipProperties:['dice','cost']\n"
									+ "    })\n"
									+ "YIELD graphName, nodeCount, relationshipCount, createMillis;");
					return result;
				});
				System.out.println(nodePropertiesWritten);
			}
		}
		
	}
	
	private static void recreateGraph(List<String> communities){
		dropGraph(GRAPH_NAME);
		System.out.println(communities);
		
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("CALL gds.graph.create('ukraine', $communities,'IS_CONNECTED',{\n"
								+ "    relationshipProperties:['dice','cost']\n"
								+ "    })\n"
								+ "YIELD graphName, nodeCount, relationshipCount, createMillis;", Values.parameters( "communities", communities ) );
				return result;
			});
			System.out.println(nodePropertiesWritten);
		}
	}
	
	private static void setNodeLabel(String name, int id) {
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("MATCH (n {name: '" + name + "'})\n"
								+ "SET n:Community" + id + "\n"
								+ "RETURN n.name, labels(n) AS labels");
				return result.list();
			});
			//System.out.println(nodePropertiesWritten);
		}
	}
	
	private static List<String> openList(){
		List<String> nodes = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader("input/marked_nodes.csv"))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		        String value = line.split(",")[0];
		        nodes.add(value);
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nodes;
	}
	
	private static void markNodes(List<String> nodeNames){
		// reset marked nodes
		try (Session session = driver.session()) {
			Object node = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("MATCH (n)\n"
								+ "SET n.marked = false\n"
								+ "RETURN n.name");
				return result;
				
			});
			
			
		}
		
		// mark new nodes
		for (String record : nodeNames) {
			try (Session session = driver.session()) {
				Object node = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx
							.run("MATCH (n {name: '" + record + "'})\n"
									+ "SET n.marked = true\n"
									+ "RETURN n.name");
					return result.single().get(0);
					
				});
				System.out.println(node);
				
			}
			

		}
		
	}
	
	private static void showNodes(String graphName){
	
		try (Session session = driver.session()) {
			Object nodes = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("MATCH (n) RETURN n.name, n.occur, n.marked LIMIT 50");
				return result.list();
			});
			System.out.println(nodes);
		}
	}
	
	private static List<Record> getNodesByCommunityId(int id){
		List<Record> nodes = new ArrayList<Record>();
		try (Session session = driver.session()) {
			nodes = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("MATCH (n) WHERE n.communityId=" + id + " RETURN n.name, n.occur, n.marked, n.communityId");
				return result.list();
			});
			//System.out.println(nodes);
		}
		return nodes;
	}
	
	private static void exportToCSV(List<Record> records, String fileName, String info){
		File csvOutputFile = new File("output/" + fileName + ".csv");
	    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
	    	
	    	switch (info) {
			case "centrality":
		    	pw.println("name;score;marked");
		        records.stream().forEach(record -> {
		        	pw.println( record.get("name") + ";" + record.get("score").toString().replace(".", ",") + ";" + record.get("marked"));
		        });
				break;
			case "community":
		    	pw.println("name;communityId");		
		        records.stream().forEach(record -> {
		        	pw.println( record.get("name") + ";" + record.get("communityId"));
		        });
				break;
			case "path":
		    	pw.println("nodeIds;pages");
		        records.stream().forEach(record -> {
		        	pw.println( record.get("nodeIds") + ";" + record.get("pages"));
		        });
				break;

			default:
				break;
			}

	    }
	    catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    assertTrue(csvOutputFile.exists());
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

}
