package main.java.graphreduction;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class Centrality {
	private Driver driver;

	public Centrality(Driver driver) {
		super();
		this.driver = driver;
	}

	public List<Record> articleRank() {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.articleRank.stream('ukraine', {\n"
						+ "	relationshipWeightProperty: 'cost'\n" 
						+ "	})\n"
						+ "	YIELD nodeId, score\n" 
						+ "	RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "	ORDER BY score DESC, name ASC\n");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}

	// Verwenden - unweighted
	public List<Record> betweenness(String graphName) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.betweenness.stream('" + graphName + "')\n"
						+ "YIELD nodeId, score\n"
						//+ "WHERE score<>0\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "ORDER BY score DESC\n");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}
	
	public List<Record> betweennessWithNodeLabel(String graphName, String community, int communityId) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.betweenness.stream($graphname, { nodeLabels: [$community] })\n"
						+ "YIELD nodeId, score\n"
						+ "WHERE gds.util.asNode(nodeId).communityId=$communityId\n"
						//+ "WHERE score<>0\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score, gds.util.asNode(nodeId).communityId AS communityId, gds.util.asNode(nodeId).marked AS marked\n"
						+ "ORDER BY score DESC", Values.parameters( "graphname", graphName, "community", community, "communityId", communityId ));
				return result.list();
			});
			//System.out.println(list);
			return list;

		}
	}

	// Verwenden - weighted
	public List<Record> degreeCrentrality(String graphName, String weightProperty) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.degree.stream('" + graphName + "',{\n"
						+ "    relationshipWeightProperty: '" + weightProperty + "',\n"
						+ "    orientation: 'UNDIRECTED'\n"
						+ "})\n"
						+ "YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "ORDER BY score DESC, name ASC\n");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}

	public List<Record> eigenVector() {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.eigenvector.stream('ukraine',{\n"
						+ "    relationshipWeightProperty: 'cost'\n"
						+ "\n"
						+ "})\n"
						+ "YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "ORDER BY score DESC, name ASC\n");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}

	public List<Record> pageRank() {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.pageRank.stream('ukraine', {\n"
						+ "relationshipWeightProperty: 'cost'}) YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score ORDER BY score DESC, name ASC\n");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}

}
