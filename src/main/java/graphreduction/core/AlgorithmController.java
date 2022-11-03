package main.java.graphreduction.core;

import main.java.graphreduction.centrality.Betweenness;
import main.java.graphreduction.centrality.Degree;
import main.java.graphreduction.communitydetection.CommunityDetectionImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The algorithm controller is in charge of all the operations containing algorithm execution.
 * This includes executing centrality and community detection algorithms and finding relationships.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class AlgorithmController {

	final Logger logger = LogManager.getLogger();
	
	private final Driver driver;
	

	public AlgorithmController(Driver driver) {
		this.driver = driver;
	}

	/**
	 * Executes a centrality algorithm.
	 * 
	 * @param centralityAlg Centrality algorithm
	 * @param communities List of communities
	 * @return List of records
	 */
	public List<Record> executeCentralityAlgorithm(String centralityAlg, List<String> communities){

		logger.info(centralityAlg);
		List<Record> list = new ArrayList<>();

		switch (centralityAlg) {
			case "betweenness":
				Betweenness betweenness = new Betweenness(driver);

				communities.forEach(com ->
					list.addAll(betweenness.streamWithNodeLabel(ReductionConfig.GRAPH_NAME, com, Integer.parseInt(com.replaceAll("\\D+",""))))
					// getDiceCoefficient(Integer.parseInt(com.replaceAll("\\D+","")));
				);
				break;

			case "degree":
				Degree degree = new Degree(driver);

				communities.forEach(com ->
					list.addAll(degree.streamWithNodeLabel(ReductionConfig.GRAPH_NAME, com, Integer.parseInt(com.replaceAll("\\D+",""))))
				);
				break;

			default:
				break;

		}
		return list;
	}

	/**
	 * First, executes a community detection algorithm in the specified mode. Then a second algorithm is executed:
	 * Either a centrality algorithm or degree calculation.
	 * 
	 * @param mode Mode is stream or write
	 * @param communityDetection Name of the community detection algorithm
	 * @param secondAlg Name of the second algorithm
	 * @return List of records
	 */
	public List<Record> executeCommunityAlgorithm(String mode, CommunityDetectionImpl communityDetection, String secondAlg){

		switch (mode) {
			case "stream":
				communityDetection.stream(ReductionConfig.GRAPH_NAME);
				break;

			case "write":
				communityDetection.write(ReductionConfig.GRAPH_NAME);
				break;

			default:
				return new ArrayList<>();

		}

		List<Record> ids = communityDetection.getIds();

		List<String> communities = new ArrayList<>();

		GraphController controller = new GraphController(driver);

		ids.forEach(rec -> {

			List<Record> communityNodes = controller.getNodesByCommunityId(rec.get("communityId").asInt());
			communityNodes.forEach(node -> {

				controller.setNodeLabel(node.get(0).asString(), node.get(3).asInt());
				if(!communities.contains("Community" + node.get(3).asInt())) {
					communities.add("Community" + node.get(3).asInt());
				}

			});
		});

		controller.recreateGraph(communities, ReductionConfig.GRAPH_NAME);

		logger.info("Anzahl Communities: {}", communities.size());

		List<Record> list;

		switch (secondAlg) {
			case "betweenness":
			case "degree":
				list = executeCentralityAlgorithm(secondAlg, communities);
				break;

			case "outside":
			case "within":
				list = findRelationShips(secondAlg, communities,communityDetection);
				break;

			default:
				return new ArrayList<>();

		}
		return list;
	}


	/**
	 * Finds relationships to nodes within the community or counts distinct relationships to nodes outside the community.
	 *
	 * @param type either within or outside
	 * @param communities List of community IDs
	 * @param communityDetection specified communityDetectionImpl
	 * @return List that contains all nodes that have relationships within or outside the community
	 */
	public List<Record> findRelationShips(String type, List<String> communities, CommunityDetectionImpl communityDetection){

		List<Record> list = new ArrayList<>();

		switch (type) {
			case "within":
				communities.forEach(com ->
					list.addAll(communityDetection.getNodesWithRelationshipsWithinCommunity(Integer.parseInt(com.replaceAll("\\D+",""))))
				);
				break;

			case "outside":
				communities.forEach(com ->
					list.addAll(communityDetection.getNodesWithRelationshipsToOtherCommunities(Integer.parseInt(com.replaceAll("\\D+",""))))

				);
				break;

			default:
				break;

		}
		return list;
	}

	/**
	 * Calculates the Dice coefficient for a specified community's relationships.
	 * 
	 * @param communityId Community ID
	 */
	private void getDiceCoefficient(int communityId){
		try (Session session = driver.session()) {
			List<Record> relationships = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n {communityId: $communityId})-[r:IS_CONNECTED]->(c)\n"
								+ "RETURN r.dice as dice",
								Values.parameters( "communityId", communityId));
				return result.list();
			});

			double summe = 0.0f;

			for (Record relationship : relationships) {
				summe += relationship.get("dice").asDouble();
			}

			logger.info(summe);
			//return nodePropertiesWritten.get("dice").asFloat();
		}
	}
}
