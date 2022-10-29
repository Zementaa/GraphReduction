package main.java.graphreduction.core;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.neo4j.driver.*;

import main.java.graphreduction.centrality.Betweenness;
import main.java.graphreduction.centrality.Degree;
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

	public ReductionController(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public static void main(String[] args) throws Exception {

		if(ReductionConfig.CRAWL_FIRST){
			FileUtils.cleanDirectory(new File(ReductionConfig.NEWSPAPER_DIRECTORY));
			crawlNewspaper();
		}

		FileSystemNetworkController fsnController = new FileSystemNetworkController();

		boolean isAlive = fsnController.isSocketAlive("localhost", ReductionConfig.NEO4J_PORT);

		System.out.println("Neo4j Console wurde gestartet: " + isAlive);
		if (!isAlive) {
			System.out.println("Neo4j console muss zunächst gestartet werden.");
			return;
		}
		try (ReductionController controller = new ReductionController(
				ReductionConfig.NEO4J_URI, ReductionConfig.NEO4J_USER, ReductionConfig.NEO4J_PWD)) {

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
			* Degree calculation
			*   within - Relationships to nodes within the community
			*   outside - Relationships to nodes outside the community (distinct)
			* 	
			*/

			GraphController graphController = new GraphController(driver);
			init(graphController);
			
			// load "to be marked" node names from list that contains handpicked nodes
			List<String> nodes = fsnController.openList(ReductionConfig.PATH_TO_MARKED_NODES_LIST);

			// mark all interesting nodes
			graphController.markNodes(nodes);
			
			// Set algorithm and mode
			String alg = ReductionConfig.Algorithms.LABEL_PROPAGATION.getText();
			// stream, write
			// must always be 'write' for community detection
			String mode = ReductionConfig.Modes.WRITE.getText();
			
			// If community algorithm is used, set centrality or degree calc algorithm here
			// centrality alg: betweenness, degree
			// hub node type: within, outside
			String secondAlg = ReductionConfig.Algorithms.BETWEENNESS.getText();

			// Criteria by that the specified graph should be reduced
			// least_score_percent - for example least 10 % (0.1) of all scores
			// not_in_top_percent - for example not in top 15 % (0.15F)
			// under_score - for example under 500
			String reductionCriteria = ReductionConfig.ReductionCriteria.LEAST_SCORE.getText();
			float threshold = ReductionConfig.THRESHOLD;

			final long createdMillis = System.currentTimeMillis();
			System.out.println("Vorher: Es befinden sich " + graphController.getNumberOfNodesInGraph() +
					" Knoten im Graph.");
			useAlgorithm(alg, mode, secondAlg);
			System.out.println("Nachher: Es befinden sich " + graphController.getNumberOfNodesInGraph() +
					" Knoten im Graph.");

			long nowMillis = System.currentTimeMillis();
			System.out.println("Algorithm took " + ((nowMillis - createdMillis) / 1000) + " seconds / "
					+ (nowMillis - createdMillis) + " milliseconds for completion.");

			if(ReductionConfig.DELETION_ACTIVATED){
				graphController.deleteNodesByCriteria(reductionCriteria, threshold);
			}
		}
	}
	/**
	 * Crawls the specified archive folder of the Evening Standard.
	 * <p>
	 * Es werden alle Artikel heruntergeladen, die thematisch in den Ukraine-Russland-Konflikt passen.
	 * Es wird nur der Bereich vier Wochen nach dem Beginn des Konflikts gecrawlt.
	 * Andere Konfigurationen können hinterlegt werden.
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
	private static void init(GraphController graphController) {

		List<Record> nodeLabels = graphController.getAllLabels();

		graphController.deleteAllLabels(nodeLabels);

		graphController.setNodeLabelSingleNode();

		graphController.setCommunityIdToIdentity();

		graphController.deleteScores();
		
		// create graph if not exists
		graphController.createGraph(ReductionConfig.GRAPH_NAME);
	}

	/**
	 * Führt den angegebenen Algorithmus im angegeben Modus aus.
	 *
	 * @param alg Der Algorithmus der benutzt werden soll
	 * @param mode Der Modus, in dem der Algorithmus ausgeführt werden soll (stream oder write)
	 */
	private static void useAlgorithm(String alg, String mode, String secondAlg){
		List<Record> records;
		AlgorithmController algorithmController = new AlgorithmController(driver, ReductionConfig.GRAPH_NAME);

		switch (alg) {
			// TODO kurze Beschreibung
			case "betweenness":
				Betweenness betweenness = new Betweenness(driver);

				if(mode.equals("write")){
					betweenness.write(ReductionConfig.GRAPH_NAME);
				}
				records = betweenness.stream(ReductionConfig.GRAPH_NAME);
				break;
				
			case "degree":
				Degree degree = new Degree(driver);

				if(mode.equals("write")){
					degree.write(ReductionConfig.GRAPH_NAME);
				}
				records = degree.stream(ReductionConfig.GRAPH_NAME);
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