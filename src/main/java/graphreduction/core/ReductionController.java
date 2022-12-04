package main.java.graphreduction.core;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.neo4j.driver.*;

import main.java.graphreduction.centrality.Betweenness;
import main.java.graphreduction.centrality.Degree;
import main.java.graphreduction.communitydetection.LabelPropagation;
import main.java.graphreduction.communitydetection.Louvain;
import main.java.graphreduction.crawler.CrawlController;

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

	static final Logger logger = LogManager.getLogger();

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

		logger.info("Neo4j Console started: {}", isAlive);
		if (!isAlive) {
			logger.info("Neo4j console must be started.");
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
			String alg = ReductionConfig.Algorithms.BETWEENNESS.getText();
			// stream, write
			// must always be 'write' for community detection
			String mode = ReductionConfig.Modes.WRITE.getText();
			
			// If community algorithm is used, set centrality or degree calc algorithm here
			// centrality alg: betweenness, degree
			// hub node type: within, outside
			String secondAlg = ReductionConfig.Algorithms.WITHIN.getText();

			// Criteria by that the specified graph should be reduced
			// least_score_percent - for example least 10 % (0.1) of all scores
			// not_in_top_percent - for example not in top 15 % (0.15F)
			// under_score - for example under 500
			String reductionCriteria = ReductionConfig.ReductionCriteria.LEAST_SCORE.getText();
			float threshold = ReductionConfig.THRESHOLD;

			final long createdMillis = System.currentTimeMillis();
			logger.info("Before: Graph contains {} nodes." , graphController.getNumberOfNodesInGraph());
			useAlgorithm(alg, mode, secondAlg);

			long nowMillis = System.currentTimeMillis();
			logger.info("Algorithm took {} seconds / {} milliseconds for completion." ,
					((nowMillis - createdMillis) / 1000) ,(nowMillis - createdMillis));

			if(ReductionConfig.DELETION_ACTIVATED){
				graphController.deleteNodesByCriteria(reductionCriteria, threshold);
			}
			logger.info("After: Graph contains {} nodes." , graphController.getNumberOfNodesInGraph());

		}
	}
	/**
	 * Crawls the specified archive folder of the Evening Standard.
	 * <p>
	 * All articles on the Ukraine-Russia-conflict are downloadet.
	 * Only the four weeks after the Russian invasion are crawled.
	 * Other configurations can be made.
	 *
	 */
	private static void crawlNewspaper() throws Exception {

		CrawlController controller = new CrawlController();

		controller.crawl();
	}

	/**
	 * Initializes the graph
	 * <p>
	 * All node labels are deleted and a fresh graph is created.
	 *
	 */
	private static void init(GraphController graphController) {

		List<Record> nodeLabels = graphController.getAllLabels();

		graphController.deleteAllLabels(nodeLabels);

		graphController.setNodeLabelSingleNode();

		graphController.setCommunityIdToIdentity();

		graphController.deleteScores();
		
		// create graph if not exists
		graphController.createGraph();
	}

	/**
	 * Executes the specified algorithm in the specified mode.
	 *
	 * @param alg Algorithm that should be used
	 * @param mode Mode, that the algorithm is executed in (stream or write)
	 */
	private static void useAlgorithm(String alg, String mode, String secondAlg){
		List<Record> records;
		AlgorithmController algorithmController = new AlgorithmController(driver);

		switch (alg) {
			case "betweenness":
				// Calculates shortest paths -> decides if a node is needed to traverse many of those
				Betweenness betweenness = new Betweenness(driver);
				if(mode.equals("write")){
					betweenness.write(ReductionConfig.GRAPH_NAME);
				}
				records = betweenness.stream(ReductionConfig.GRAPH_NAME);
				break;
				
			case "degree":
				// Calculates in and out degree of nodes
				Degree degree = new Degree(driver);
				if(mode.equals("write")){
					degree.write(ReductionConfig.GRAPH_NAME);
				}
				records = degree.stream(ReductionConfig.GRAPH_NAME);
				break;
				
			case "louvain":
				// Maximizes a modularity score for each community -> deciding if a node is added or not
				Louvain louvain = new Louvain(driver);
				records = algorithmController.executeCommunityAlgorithm(mode, louvain, secondAlg);
				alg += "-" + secondAlg;
				break;
				
			case "labelP":
				// Propagates labels over the graph to find communities
				LabelPropagation labelP = new LabelPropagation(driver);
				records = algorithmController.executeCommunityAlgorithm(mode, labelP, secondAlg);
				alg += "-" + secondAlg;
				break;
	
			default:
				// no csv-export
				return;
		}

		FileSystemNetworkController fsnController = new FileSystemNetworkController();
		fsnController.exportToCSV(records, alg);
	}
}