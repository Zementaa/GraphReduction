package main.java.graphreduction.core;

import main.java.graphreduction.centrality.Betweenness;
import main.java.graphreduction.centrality.Degree;
import main.java.graphreduction.communitydetection.CommunityDetectionImpl;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmController {

	private Driver driver;

	private String graphName;

	public AlgorithmController(Driver driver, String graphName) {
		this.driver = driver;
		this.graphName = graphName;
	}

	public List<Record> executeCentralityAlgorithm(String centralityAlg, List<String> communities){

		System.out.println(centralityAlg);
		List<Record> list = new ArrayList<>();

		switch (centralityAlg) {
			case "betweenness":
				Betweenness betweenness = new Betweenness(driver);

				communities.forEach(com -> {
					list.addAll(betweenness.streamWithNodeLabel(graphName, com, Integer.parseInt(com.replaceAll("\\D+",""))));
					// getDiceCoefficient(Integer.parseInt(com.replaceAll("\\D+","")));
				});
				break;

			case "degree":
				Degree degree = new Degree(driver);

				communities.forEach(com -> {
					list.addAll(degree.streamWithNodeLabel(graphName, com, Integer.parseInt(com.replaceAll("\\D+",""))));

				});
				break;

			default:
				break;

		}
		return list;
	}

	public List<Record> executeCommunityAlgorithm(String mode, CommunityDetectionImpl communityDetection, String secondAlg){

		switch (mode) {
			case "stream":
				communityDetection.stream(graphName);
				break;

			case "write":
				communityDetection.write(graphName);
				break;

			default:
				return new ArrayList<>();

		}

		List<Record> ids = communityDetection.getIds(graphName);

		List<String> communities = new ArrayList<>();

		GraphController controller = new GraphController(driver);

		ids.forEach(record -> {

			List<Record> communityNodes = controller.getNodesByCommunityId(record.get("communityId").asInt());
			communityNodes.forEach(node -> {

				controller.setNodeLabel(node.get(0).asString(), node.get(3).asInt());
				if(!communities.contains("Community" + node.get(3).asInt())) {
					communities.add("Community" + node.get(3).asInt());
				}

			});
		});

		controller.recreateGraph(communities, graphName);

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



	public List<Record> findRelationShips(String type, List<String> communities, CommunityDetectionImpl communityDetection){

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

	private void getDiceCoefficient(int communityId){
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
}