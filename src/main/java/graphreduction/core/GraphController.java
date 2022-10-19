package main.java.graphreduction.core;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;

public class GraphController {

	private Driver driver;

	public GraphController(Driver driver) {
		this.driver = driver;
	}

	private void dropGraph(String graphName){
		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("CALL gds.graph.drop('" + graphName + "');");
				return result;
			});
			System.out.println("Graph dropped.");
		}
	}

	void createGraph(String graphName){

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
							.run("CALL gds.graph.create('" + graphName + "','SINGLE_NODE'," +
									"{IS_CONNECTED:{properties:'cost'}},{\n"
									+ "    nodeProperties:['seed']\n"
									+ "    })\n"
									+ "YIELD graphName, nodeCount, relationshipCount, createMillis;");
					return result;
				});
				System.out.println("Graph created.");
			}
		}

	}

	void recreateGraph(List<String> communities, String graphName){
		dropGraph(graphName);
		System.out.println(communities);

		try (Session session = driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				Result result = tx
						.run("CALL gds.graph.create('" + graphName + "', $communities," +
								"{IS_CONNECTED:{properties:'cost'}},{\n"
								+ "    nodeProperties:['seed']\n"
								+ "    })\n"
								+ "YIELD graphName, nodeCount, relationshipCount, createMillis;", Values.parameters( "communities", communities ) );
				return result;
			});
			//System.out.println(nodePropertiesWritten);
		}
	}

	// ----------------- Node related methods --------------------- //

	List<Record> getAllLabels() {
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

	void deleteAllLabels(List<Record> nodeLabels) {

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

	void setNodeLabelSingleNode() {
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

	void setNodeLabel(String name, int id) {
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

	void markNodes(List<String> nodeNames){
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

	void showNodes(String graphName){

		try (Session session = driver.session()) {
			Object nodes = session.writeTransaction(tx -> {
				Result result = tx
						.run("MATCH (n) RETURN n.name, n.occur, n.marked LIMIT 50");
				return result.list();
			});
			System.out.println(nodes);
		}
	}

	List<Record> getNodesByCommunityId(int id){
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

	void deleteNodesThatAreCommunityID(int id) {
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n:Community" + id + ") DETACH DELETE n");
				return result.single().get(0);
			});
			System.out.println("Es wurden alle Knoten die zu Community" + id + " gehören gelöscht.");
		}
	}

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

	void deleteNodesByCriteria(String criteria, float threshold) {

		switch (criteria) {
			case "under_score":
				try (Session session = driver.session()) {
					session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n) WHERE n.score < " + threshold + " DETACH DELETE n");
						return result.list();
					});
					System.out.println("Es wurden alle Knoten gelöscht, die dem Kriterium " + criteria + ": " + threshold + " entsprechen.");
				}
				break;

			case "not_in_top_percent":

				int number_of_nodes = 0;
				try (Session session = driver.session()) {
					number_of_nodes = session.writeTransaction(tx -> {
						Result result = tx.run("MATCH (n) RETURN count(n)");
						return result.single().get(0).asInt();
					});
				}
				int top_percent = Math.round(number_of_nodes * threshold);

				List<Record> nodes = new ArrayList<>();
				try (Session session = driver.session()) {
					nodes = session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n)\n" +
								"RETURN n.score\n" +
								"ORDER BY n.score DESC\n" +
								"LIMIT " + top_percent );
						return result.list();
					});
				}
				Record node = nodes.get(nodes.size() - 1);

				String value = String.valueOf(node.get("n.score"));

				try (Session session = driver.session()) {
					session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n) WHERE n.score < " + value + " DETACH DELETE n");
						return result.list();
					});
					System.out.println("Es wurden alle Knoten gelöscht, die dem Kriterium " + criteria + ": " + threshold + ", also einem Score von weniger als " + value + " entsprechen.");
				}
				break;

			case "least_score_percent":
					List<Record> scores = new ArrayList<>();
				try (Session session = driver.session()) {
					scores = session.writeTransaction(tx -> {
						Result result = tx.run("MATCH (n) RETURN n.score");
						return result.list();
					});
				}
				double sumOfScores = 0;
				for (Record score : scores) {
					sumOfScores += score.get("n.score").asDouble();
				}

				List<Record> nodes2 = new ArrayList<>();
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

					if(tenPercentSum / sumOfScores > 0.1){
						break;
					}
					nodesToDelete.add(node2);
				}

				List<String> ids = new ArrayList<>();
				for (Record node3 : nodesToDelete) {
					ids.add(String.valueOf(node3.get("id").asInt()));
				}

				System.out.println(ids);

				try (Session session = driver.session()) {
					session.writeTransaction(tx -> {
						org.neo4j.driver.Result result = tx.run("MATCH (n) " +
								"WHERE id(n) in " + ids +
								" DETACH DELETE n");
						return result.list();
					});
					System.out.println("Es wurden alle Knoten gelöscht, die dem Kriterium " + criteria + ": " + threshold + " entsprechen.");
				}

				break;

			default:
		}
	}

	public void deleteScores() {
		try (Session session = driver.session()) {
			session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n)\n" +
						"REMOVE n.score\n" +
						"RETURN n.name");
				return result.list();
			});
			System.out.println("Score wurde bereinigt.");
		}
	}
}

