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

import org.apache.commons.io.FileUtils;
import org.neo4j.driver.*;

import main.java.graphreduction.centrality.Betweenness;
import main.java.graphreduction.centrality.Degree;
import main.java.graphreduction.communitydetection.CommunityDetectionImpl;
import main.java.graphreduction.communitydetection.LabelPropagation;
import main.java.graphreduction.communitydetection.Louvain;
import main.java.graphreduction.crawler.CrawlController;
import main.java.graphreduction.crawler.CrawlConfig;

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

		boolean crawlFirst = false; // set crawl mode

		if(crawlFirst == true){
			FileUtils.cleanDirectory(new File("src/test/resources/crawler4j/papers/"));
			crawlNewspaper();
		}

		FileSystemNetworkController fsnController = new FileSystemNetworkController();

		boolean isAlive = fsnController.isSocketAlive("localhost", 7687);

		System.out.println("Neo4j Console wurde gestartet: " + isAlive);
		if (!isAlive) {
			System.out.println("Neo4j console muss zunächst gestartet werden.");
			return;
		}
		try (ReductionController controller = new ReductionController("bolt://localhost:7687", "neo4j", "password")) {

			/* Algorithms overview
			*
			* Centrality
			* 	betweenness - Betweenness Centrality
			* 	degree - Degree Centrality
			* 
			* Community Detection
			* 	louvain - Louvain Algorithmus
			* 	labelP - Label Propagation
			* 	
			*/
			
			init();
			
			// load "to be marked" node names from list that contains handpicked nodes
			List<String> nodes = fsnController.openList("input/marked_nodes.csv");

			GraphController graphController = new GraphController(driver);
			// mark all interesting nodes
			graphController.markNodes(nodes);
			
			// Set algorithm and mode
			String alg = "louvain";
			// stream, write
			String mode = "write";
			
			// If community algorithm is used, set centrality algorithm here
			// centrality alg: betweenness, degree
			// hub node type: within, toOther
			String secondAlg = "within";
			
			useAlgorithm(alg, mode, secondAlg);
			
		}
	}
	/**
	 * Crawlt den entsprechenden Archivbereich der Evening Standard.
	 * <p>
	 * Es werden alle Artikel heruntergeladen, die thematisch in den Ukraine-Russland-Konflikt passen.
	 * Es wird nur der Bereich vier Wochen nach dem Beginn des Konflikts gecrawlt.
	 *
	 */
	private static void crawlNewspaper() throws Exception {

		CrawlConfig crawlConfig = new CrawlConfig();
		CrawlController controller = new CrawlController(crawlConfig);

		controller.crawl();
	}

	/**
	 * Initialisiert den Graphen
	 * <p>
	 * Es werden alle Node Labels gelöscht und ein frischer Graph erstellt.
	 *
	 */
	private static void init() {

		GraphController graphController = new GraphController(driver);

		List<Record> nodeLabels = graphController.getAllLabels();

		graphController.deleteAllLabels(nodeLabels);

		graphController.setNodeLabelSingleNode();
		
		// create graph if not exists
		graphController.createGraph(GRAPH_NAME);
	}

	/**
	 * Führt den angegebenen Algorithmus im angegeben Modus aus.
	 *
	 * @param alg Der Algorithmus der benutzt werden soll
	 * @param mode Der Modus, in dem der Algorithmus ausgeführt werden soll (stream oder write)
	 */
	private static void useAlgorithm(String alg, String mode, String secondAlg){
		List<Record> records;
		AlgorithmController algorithmController = new AlgorithmController(driver, GRAPH_NAME);

		switch (alg) {
			// TODO kurze Beschreibung
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
				records = algorithmController.executeCommunityAlgorithm(mode, louvain, secondAlg);
				alg += "-" + secondAlg;
				break;
				
			case "labelP":
				LabelPropagation labelP = new LabelPropagation(driver);
				records = algorithmController.executeCommunityAlgorithm(mode, labelP, secondAlg);
				alg += "-" + secondAlg;
				break;
	
			default:
				return;
		}

		FileSystemNetworkController fsnController = new FileSystemNetworkController();
		fsnController.exportToCSV(records, alg);
	}


}