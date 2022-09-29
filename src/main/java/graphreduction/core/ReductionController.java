package main.java.graphreduction.core;

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
import java.util.regex.Pattern;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import main.java.graphreduction.crawler.CrawlerStatistics;
import main.java.graphreduction.crawler.HtmlCrawler;
import org.apache.commons.io.FileUtils;
import org.neo4j.driver.*;

import main.java.graphreduction.centrality.Betweenness;
import main.java.graphreduction.centrality.Degree;
import main.java.graphreduction.communitydetection.CommunityDetectionImpl;
import main.java.graphreduction.communitydetection.LabelPropagation;
import main.java.graphreduction.communitydetection.Louvain;

/**
 * The reduction controller is the main class of the reduction project.
 * <p>
 * It traverses these steps:
 * <ul>
 *     <li> Create co-occurrence graph from neo4j DB </li>
 *     <li> Mark important handpicked nodes </li>
 *     <li> Execute specified algorithm(s) </li>
 *     <li> Export results to csv </li>
 * </ul>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class ReductionController implements AutoCloseable {

	private static Driver driver;
	
	private static final String GRAPH_NAME = "ukraine";


	public ReductionController(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public static void main(String[] args) throws Exception {

		//FileUtils.cleanDirectory(new File("src/test/resources/crawler4j/papers/"));
		//crawlNewspaper();

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
			* 	betweenness - Betweenness Centrality
			* 	degree - Degree Centrality
			* 
			* Community Detection
			* 	louvain - Louvain Algorithmus
			* 	labelP - Label Propagation
			* 	modularity - Modularity Optimization
			* 	
			*/
			
			// Preprocessing
			// Find nodes that are not connected to main graph and delete those
			// WeaklyConnectedComponents wcc = new WeaklyConnectedComponents(driver);
			// wcc.useWeaklyConnectedComponentsAlgorithm();
			
			init();
			
			// load "to be marked" node names from list that contains handpicked nodes
			List<String> nodes = openList();
			
			// mark all interesting nodes
			markNodes(nodes);
			
			// Set algorithm and mode
			String alg = "betweenness";
			// stream, write, stats, mutate
			String mode = "stream";
			
			// If community algorithm is used, set centrality algorithm here
			// centrality alg: betweenness, degree
			// hub node type: within, toOther
			String secondAlg = "betweenness";
			
			useAlgorithm(alg, mode, secondAlg);
			
		}
	}

	private static void crawlNewspaper() throws Exception {
		final int NUM_CRAWLERS = 10;
		final int MAX_DEPTH = 1;
		final int MAX_PAGES_TO_FETCH = -1; // no limit
		final boolean INCLUDE_HTTPS_PAGES = true;
		final boolean SHUT_DOWN_ON_EMPTY_QUEUE = true;
		final int THREAD_MONITORING_DELAY_SEC = 3;
		final int THREAD_SHUTDOWN_DELAY_SEC = 3;
		final int CLEANUP_DELAY_SEC = 5;

		File crawlStorage = new File("src/test/resources/crawler4j");
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorage.getAbsolutePath());

		config.setMaxDepthOfCrawling(MAX_DEPTH);
		config.setMaxPagesToFetch(MAX_PAGES_TO_FETCH);
		config.setIncludeHttpsPages(INCLUDE_HTTPS_PAGES);
		config.setShutdownOnEmptyQueue(SHUT_DOWN_ON_EMPTY_QUEUE);
		config.setThreadMonitoringDelaySeconds(THREAD_MONITORING_DELAY_SEC);
		config.setThreadShutdownDelaySeconds(THREAD_SHUTDOWN_DELAY_SEC);
		config.setCleanupDelaySeconds(CLEANUP_DELAY_SEC);

		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer= new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

		controller.addSeed("https://www.standard.co.uk/archive/2022-02-25/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-02-26/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-02-27/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-02-28/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-01/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-02/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-03/");

		controller.addSeed("https://www.standard.co.uk/archive/2022-03-04/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-05/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-06/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-07/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-08/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-09/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-10/");

		controller.addSeed("https://www.standard.co.uk/archive/2022-03-11/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-12/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-13/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-14/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-15/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-16/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-17/");

		controller.addSeed("https://www.standard.co.uk/archive/2022-03-18/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-19/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-20/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-21/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-22/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-23/");
		controller.addSeed("https://www.standard.co.uk/archive/2022-03-24/");

		CrawlerStatistics stats = new CrawlerStatistics();
		CrawlController.WebCrawlerFactory<HtmlCrawler> factory = () -> new HtmlCrawler(stats);

		controller.start(factory, NUM_CRAWLERS);
	}

	/**
	 * Initialisiert den Graphen
	 * <p>
	 * Es werden alle Node Labels gelöscht und ein frischer Graph erstellt.
	 *
	 */
	private static void init() {
		
		List<Record> nodeLabels = getAllLabels();
		
		deleteAllLabels(nodeLabels);

		setNodeLabelSingleNode();
		
		// create graph if not exists
		createGraph(GRAPH_NAME);
	}

	/**
	 * Initialisiert den Graphen
	 *
	 * Es werden alle Node Labels gelöscht und ein frischer Graph erstellt.
	 *
	 * @param alg Der Algorithmus der benutzt werden soll
	 * @param mode Der Modus, in dem der Algorithmus ausgeführt werden soll
	 */
	private static void useAlgorithm(String alg, String mode, String secondAlg){
		List<Record> records = new ArrayList<>();

		switch (alg) {
			case "betweenness":
				Betweenness betweenness = new Betweenness(driver);
				records = betweenness.stream(GRAPH_NAME);
				break;
				
			case "degree":
				Degree degree = new Degree(driver);
				records = degree.stream(GRAPH_NAME);
				break;
				
			case "louvain":
				Louvain louvain = new Louvain(driver);
				records = executeCommunityAlgorithm(mode, louvain, secondAlg);
				alg += "-" + secondAlg;
				break;
				
			case "labelP":
				LabelPropagation labelP = new LabelPropagation(driver);
				records = executeCommunityAlgorithm(mode, labelP, secondAlg);
				alg += "-" + secondAlg;
				break;
	
			default:
				return;
		}

		exportToCSV(records, alg);
	}
	
	public static List<Record> executeCentralityAlgorithm(String centralityAlg, List<String> communities){

		System.out.println(centralityAlg);
		List<Record> list = new ArrayList<>();
		
		switch (centralityAlg) {
			case "betweenness":
				Betweenness betweenness = new Betweenness(driver);
				
				communities.forEach(com -> {
					list.addAll(betweenness.streamWithNodeLabel(GRAPH_NAME, com, Integer.parseInt(com.replaceAll("\\D+",""))));
					// getDiceCoefficient(Integer.parseInt(com.replaceAll("\\D+","")));
				});
				break;
				
			case "degree":
				Degree degree = new Degree(driver);

				communities.forEach(com -> {
					list.addAll(degree.streamWithNodeLabel(GRAPH_NAME, com, Integer.parseInt(com.replaceAll("\\D+",""))));
					
				});
				break;
	
			default:
				break;
				
		}
		return list;
	}

	public static List<Record> executeCommunityAlgorithm(String mode, CommunityDetectionImpl communityDetection, String secondAlg){

		switch (mode) {
			case "stream":
				communityDetection.stream(GRAPH_NAME);
				break;

			case "write":
				communityDetection.write(GRAPH_NAME);
				break;

			default:
				return new ArrayList<>();

		}

		List<Record> ids = communityDetection.getIds(GRAPH_NAME);

		List<String> communities = new ArrayList<>();

		ids.forEach(record -> {

			List<Record> communityNodes = getNodesByCommunityId(record.get("communityId").asInt());
			communityNodes.forEach(node -> {

				setNodeLabel(node.get(0).asString(), node.get(3).asInt());
				if(!communities.contains("Community" + node.get(3).asInt())) {
					communities.add("Community" + node.get(3).asInt());
				}

			});
		});

		recreateGraph(communities);

		System.out.println("Anzahl Communities: " + communities.size());

		List<Record> list;

		switch (secondAlg) {
			case "betweenness":
			case "degree":
				list = executeCentralityAlgorithm(secondAlg, communities);
				break;

			case "toOther":
			case "within":
				list = findRelationShips(secondAlg, communities,communityDetection);
				break;

			default:
				return new ArrayList<>();

		}
		//System.out.println(list);
		return list;
	}

	public static List<Record> findRelationShips(String type, List<String> communities, CommunityDetectionImpl communityDetection){

		List<Record> list = new ArrayList<>();

		switch (type) {
			case "within":
				communities.forEach(com -> {
					list.addAll(communityDetection.getNodesWithRelationshipsWithinCommunity(Integer.parseInt(com.replaceAll("\\D+",""))));
					// getDiceCoefficient(Integer.parseInt(com.replaceAll("\\D+","")));
				});
				break;

			case "toOther":
				communities.forEach(com -> {
					list.addAll(communityDetection.getNodesWithRelationshipsToOtherCommunities(Integer.parseInt(com.replaceAll("\\D+",""))));

				});
				break;

			default:
				break;

		}
		return list;
	}

	private static void getDiceCoefficient(int communityId){
		try (Session session = driver.session()) {
			List<Record> relationships = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n {communityId: " + communityId + "})-[r:IS_CONNECTED]->(c)\n"
								+ "RETURN r.dice as dice");
				return result.list();
			});
						
			double summe = 0.0f;
			
			for(int i =0; i< relationships.size(); i++) {
				summe += relationships.get(i).get("dice").asDouble();
			}
			
			System.out.println(summe);			
			//return nodePropertiesWritten.get("dice").asFloat();
		}
	}

	// ----------------- graph related methods --------------------- //

	private static void dropGraph(String graphName){
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("CALL gds.graph.drop('" + graphName + "');");
				return result;
			});
			System.out.println("Graph dropped.");
		}
	}
	
	private static void createGraph(String graphName){
		
		String exists = "FALSE";

		try (Session session = driver.session()) {
			exists = session.writeTransaction(tx -> {
				Result result = tx
						.run("RETURN gds.graph.exists('" + graphName + "')");
				return result.single().get(0).toString();
			});
			System.out.println("Graph 'ukraine' existiert bereits? " + exists);
		}
		
		if(exists!="TRUE") {
			try (Session session = driver.session()) {
				Object nodePropertiesWritten = session.writeTransaction(tx -> {
					Result result = tx
							.run("CALL gds.graph.create('" + graphName + "','SINGLE_NODE','IS_CONNECTED',{\n"
									+ "    relationshipProperties:['dice','cost']\n"
									+ "    })\n"
									+ "YIELD graphName, nodeCount, relationshipCount, createMillis;");
					return result;
				});
				System.out.println("Graph created.");
			}
		}
		
	}
	
	private static void recreateGraph(List<String> communities){
		dropGraph(GRAPH_NAME);
		System.out.println(communities);

		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("CALL gds.graph.create('ukraine', $communities,'IS_CONNECTED',{\n"
								+ "    relationshipProperties:['dice','cost']\n"
								+ "    })\n"
								+ "YIELD graphName, nodeCount, relationshipCount, createMillis;", Values.parameters( "communities", communities ) );
				return result;
			});
			//System.out.println(nodePropertiesWritten);
		}
	}

	// ----------------- Node related methods --------------------- //

	private static List<Record> getAllLabels() {
		try (Session session = driver.session()) {
			List<Record> labels = session.writeTransaction(tx -> {
				Result result = tx
						.run("CALL db.labels()");
				return result.list();
			});
			System.out.println("Existing labels: " + labels);
			return labels;
		}
	}

	private static void deleteAllLabels(List<Record> nodeLabels) {

		nodeLabels.forEach(label -> {
			String str = label.get("label").asString();
			try (Session session = driver.session()) {
				List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
					Result result = tx
							.run("MATCH (n)\n"
									+ "REMOVE n:"+ str + "\n"
									+ "RETURN n.name, labels(n)");
					return result.list();
				});
			}
		});
	}

	private static void setNodeLabelSingleNode() {
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n)\n"
								+ "SET n:SINGLE_NODE\n"
								+ "RETURN n.name, labels(n) AS labels");
				return result.list();
			});
			//System.out.println(nodePropertiesWritten);
		}
	}
	
	private static void setNodeLabel(String name, int id) {
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n {name: '" + name + "'})\n"
								+ "SET n:SINGLE_NODE:Community" + id + "\n"
								+ "RETURN n.name, labels(n) AS labels");
				return result.list();
			});
			//System.out.println(nodePropertiesWritten);
		}
	}

	private static void markNodes(List<String> nodeNames){
		// reset marked nodes
		try (Session session = driver.session()) {
			Object node = session.writeTransaction(tx -> {
				Result result = tx
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
					Result result = tx
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
				Result result = tx
						.run("MATCH (n) RETURN n.name, n.occur, n.marked LIMIT 50");
				return result.list();
			});
			System.out.println(nodes);
		}
	}
	
	private static List<Record> getNodesByCommunityId(int id){
		List<Record> nodes = new ArrayList<>();
		try (Session session = driver.session()) {
			nodes = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) WHERE n.communityId=" + id + " RETURN n.name, n.occur, n.marked, n.communityId");
				return result.list();
			});
			//System.out.println(nodes);
		}
		return nodes;
	}

	// -------- File System or neo4j related methods ------------ //

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
	
	private static void exportToCSV(List<Record> records, String fileName){
		File csvOutputFile = new File("output/" + fileName + ".csv");
	    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
			System.out.println(records);
			System.out.println(records.size());
	    	pw.println("name;score;marked;communityId;occur;relationships");
	        records.stream().forEach(record -> {
	        	pw.println( record.get("name") + ";" + record.get("score").toString().replace(".", ",") + ";" + record.get("marked") + ";" + record.get("communityId") + ";" + record.get("occur") + ";" + record.get("relationships"));
	        });
	    }
	    catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    assertTrue(csvOutputFile.exists());
	}

	/**
	 * Checks if the socket is alive.
	 * <p>
	 * Neo4j needs to be running to execute any of the algorithms. This method checks, if the specified socket
	 * is alive.
	 *
	 * @param hostName the host name on which neo4j is running. Usually localhost.
	 * @param port the port that is used for the neo4j browser
	 * @return isAlive
	 * @see Image
	 */
	public static Boolean isSocketAlive(String hostName, int port) {
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
