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
}

