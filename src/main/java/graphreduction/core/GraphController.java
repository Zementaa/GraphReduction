package main.java.graphreduction.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The graph controller is in charge of all the operations that are made on the graph (database).
 * This includes operations like creating, dropping and altering the graph.
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class GraphController {

	final Logger logger = LogManager.getLogger();

	private Driver driver;

	public GraphController(Driver driver) {
		this.driver = driver;
	}

	/**
	 *
	 * @param graphName Name of the graph
	 */
	private void dropGraph(String graphName){
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> tx
					.run("CALL gds.graph.drop($graphName);",
							Values.parameters( "graphName", graphName)));
			logger.info("Graph dropped. {}", nodePropertiesWritten);
		}
	}

	/**
	 * Creates a new graph if not already exists.
	 */
	void createGraph(){

		String exists;

		try (Session session = driver.session()) {
			exists = session.writeTransaction(tx -> {
				Result result = tx
						.run("RETURN gds.graph.exists($graphName)",
								Values.parameters( "graphName", ReductionConfig.GRAPH_NAME));
				return result.single().get(0).toString();
			});
			logger.info("Graph 'ukraine' already exists? {}", exists);
		}

		if(!exists.equals("TRUE")) {
			try (Session session = driver.session()) {
				Object nodePropertiesWritten = session.writeTransaction(tx -> tx
						.run("CALL gds.graph.create($graphName,'SINGLE_NODE'," +
								"{IS_CONNECTED:{properties:'cost'}},{\n"
								+ "    nodeProperties:['seed']\n"
								+ "    })\n"
								+ "YIELD graphName, nodeCount, relationshipCount, createMillis;",
								Values.parameters( "graphName", ReductionConfig.GRAPH_NAME)));
				logger.info("Graph created. {}", nodePropertiesWritten );
			}
		}

	}

	/**
	 * Recreates the graph and takes community IDs into consideration.
	 *
	 * @param communities List containing the communities
	 * @param graphName Name of the graph
	 */
	void recreateGraph(List<String> communities, String graphName){
		dropGraph(graphName);
		logger.info(communities);

		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> tx
						.run("CALL gds.graph.create($graphName, $communities," +
								"{IS_CONNECTED:{properties:'cost'}},{\n"
								+ "    nodeProperties:['seed']\n"
								+ "    })\n"
								+ "YIELD graphName, nodeCount, relationshipCount, createMillis;",
								Values.parameters( "graphName", graphName, "communities", communities ))
				);
			logger.info(nodePropertiesWritten);
		}
	}

	// ----------------- Node related methods --------------------- //

	/**
	 *  Gets all existing labels.
	 *
	 * @return List of records
	 */
	List<Record> getAllLabels() {
		try (Session session = driver.session()) {
			List<Record> labels = session.writeTransaction(tx -> {
				Result result = tx
						.run("CALL db.labels()");
				return result.list();
			});
			logger.info("Existing labels: {}", labels);
			return labels;
		}
	}

	/**
	 * Deletes all the existing node labels on the graph.
	 *
	 * @param nodeLabels Node labels
	 */
	void deleteAllLabels(List<Record> nodeLabels) {

		nodeLabels.forEach(label -> {
			String str = label.get("label").asString();
			try (Session session = driver.session()) {
				List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
					Result result = tx
							.run("MATCH (n)\n"
									+ "REMOVE n:" + str + "\n"
									+ "RETURN n.name, labels(n)");
					return result.list();
				});
			}
		});
	}

	/**
	 * Sets the original SINGLE_NODE-Label.
	 */
	void setNodeLabelSingleNode() {
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n)\n"
								+ "SET n:SINGLE_NODE\n"
								+ "RETURN n.name, labels(n) AS labels");
				return result.list();
			});
			logger.info("Node Label SINGLE_NODE is set.");
		}
	}

	/**
	 * Sets the Label of the specified node to the specified community ID.
	 *
	 * @param name Name of the node
	 * @param id ID of the community
	 */
	void setNodeLabel(String name, int id) {
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n {name:$name})\n"
								+ "SET n:SINGLE_NODE:Community" + id + "\n"
								+ "RETURN n.name, labels(n) AS labels",
								Values.parameters( "name", name));
				return result.list();
			});
			//logger.info(nodePropertiesWritten);
		}
	}

	/**
	 * Marks the nodes, that the specified list contains.
	 *
	 * @param nodeNames List of the nodes that should be marked
	 */
	void markNodes(List<String> nodeNames){
		// reset marked nodes
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> tx
						.run("MATCH (n)\n"
								+ "SET n.marked = false\n"
								+ "RETURN n.name"));
		}

		// mark new nodes
		for (String rec : nodeNames) {
			try (Session session = driver.session()) {
				Object node = session.writeTransaction(tx -> {
					Result result = tx
							.run("MATCH (n {name: $rec})\n"
									+ "SET n.marked = true\n"
									+ "RETURN n.name",
									Values.parameters( "rec", rec));
					return result.single().get(0);

				});
				logger.info("Node marked: {}", node);
			}
		}
	}

	/**
	 * Sets the initial community ID of a node to its technical ID.
	 */
	void setCommunityIdToIdentity(){
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) REMOVE n.communityId RETURN n.name");
				return result.list();
			});
		}

		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) SET n.seed=id(n) RETURN n.seed");
				return result.list();
			});
		}
	}

	/**
	 * Logs 50 random nodes to the console.
	 */
	void showNodes(){

		try (Session session = driver.session()) {
			Object nodes = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) RETURN n.name, n.occur, n.marked LIMIT 50");
				return result.list();
			});
			logger.info("Nodes: {}", nodes);
		}
	}

	/**
	 * Gets all nodes that belong to the specified community.
	 *
	 * @param id Community ID
	 * @return List of records
	 */
	List<Record> getNodesByCommunityId(int id){
		List<Record> nodes;
		try (Session session = driver.session()) {
			nodes = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) WHERE n.communityId=$id RETURN n.name, n.occur, n.marked, n.communityId",
								Values.parameters( "id", id));
				return result.list();
			});
			//logger.info(nodes);
		}
		return nodes;
	}

	/**
	 * Deletes all nodes of a specified community.
	 *
	 * @param id Community ID
	 */
	void deleteNodesThatAreCommunityID(int id) {
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n:Community"+ id + ") DETACH DELETE n");
				return result.single().get(0);
			});
			logger.info("All nodes that belong to community {} were deleted.", id);
		}
	}

	/**
	 * Counts all the nodes in the graph and returns the number.
	 *
	 * @return Number of nodes in graph (int)
	 */
	int getNumberOfNodesInGraph(){
		int number = 0;
		try (Session session = driver.session()) {
			number = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) Return count(n)");
				return result.single().get(0).asInt();
			});
		}
		return number;
	}

	/**
	 * Deletes all nodes that match a certain criteria.
	 *
	 * @param criteria Criteria [under_score, not_in_top_percent, least_score_percent]
	 * @param threshold Threshold for the criteria
	 */
	void deleteNodesByCriteria(String criteria, float threshold) {

		switch (criteria) {
			case "under_score":
				try (Session session = driver.session()) {
					session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n) WHERE n.score < $threshold DETACH DELETE n",
								Values.parameters( "threshold", threshold));
						return result.list();
					});
					logger.info("All nodes deleted that match the criteria {}:{}.", criteria, threshold);
				}
				break;

			case "not_in_top_percent":

				int numberOfNodes;
				try (Session session = driver.session()) {
					numberOfNodes = session.writeTransaction(tx -> {
						Result result = tx.run("MATCH (n) RETURN count(n)");
						return result.single().get(0).asInt();
					});
				}
				int topPercent = Math.round(numberOfNodes * threshold);

				List<Record> nodes;
				try (Session session = driver.session()) {
					nodes = session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n)\n" +
								"RETURN n.score\n" +
								"ORDER BY n.score DESC\n" +
								"LIMIT $topPercent", Values.parameters( "topPercent", topPercent));
						return result.list();
					});
				}
				Record node = nodes.get(nodes.size() - 1);

				String value = String.valueOf(node.get("n.score"));

				try (Session session = driver.session()) {
					session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n) WHERE n.score < $value DETACH DELETE n",
								Values.parameters( "value", value));
						return result.list();
					});
					logger.info(
							"All nodes deleted that match the criteria {}:{}, meaning a score less than {}",
							criteria, threshold, value);
				}
				break;

			case "least_score_percent":
					List<Record> scores;
				try (Session session = driver.session()) {
					scores = session.writeTransaction(tx -> {
						Result result = tx.run("MATCH (n) RETURN n.score");
						return result.list();
					});
				}
				double sumOfScores = 0.0F;
				for (Record score : scores) {
					sumOfScores += score.get("n.score").asDouble();
				}

				List<Record> nodes2;
				try (Session session = driver.session()) {
					nodes2 = session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n)\n" +
								"RETURN n.score, id(n) AS id\n" +
								"ORDER BY n.score ASC");
						return result.list();
					});
				}

				List<Record> nodesToDelete = new ArrayList<>();
				double tenPercentSum = 0.0F;
				for (Record node2 : nodes2) {
					tenPercentSum += node2.get("n.score").asDouble();

					if(sumOfScores!=0 && tenPercentSum / sumOfScores > 0.1){
						break;
					}
					nodesToDelete.add(node2);
				}

				List<String> ids = new ArrayList<>();
				for (Record node3 : nodesToDelete) {
					ids.add(String.valueOf(node3.get("id").asInt()));
				}

				logger.info(ids);

				try (Session session = driver.session()) {
					session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n) " +
								"WHERE id(n) in " + ids +
								" DETACH DELETE n");
						return result.list();
					});
					logger.info("All nodes deleted that match the criteria {}:{}.", criteria, threshold);
				}

				break;

			default:
		}
	}

	/**
	 * Deletes the score of every node.
	 */
	public void deleteScores() {
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n)\n" +
						"REMOVE n.score\n" +
						"RETURN n.name");
				return result.list();
			});
			logger.info("Score on every node deleted.");
		}
	}
}

