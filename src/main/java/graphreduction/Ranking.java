package main.java.graphreduction;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

public class Ranking {
	private Driver driver;

	public Ranking(Driver driver) {
		super();
		this.driver = driver;
	}

	public List<Record> articleRank() {

		try (Session session = this.driver.session()) {
			List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.articleRank.stream('ukraine', {\n"
						+ "	relationshipWeightProperty: 'cost'\n" 
						+ "	})\n"
						+ "	YIELD nodeId, score\n" 
						+ "	RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "	ORDER BY score DESC, name ASC\n" 
						+ "	LIMIT 50");
				return result.list();
			});
			System.out.println(nodePropertiesWritten);
			return nodePropertiesWritten;

		}
	}

	public List<Record> betweenness() {

		try (Session session = this.driver.session()) {
			List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.betweenness.stream('ukraine')\n"
						+ "YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "ORDER BY score DESC\n"
						+ "LIMIT 50");
				return result.list();
			});
			System.out.println(nodePropertiesWritten);
			return nodePropertiesWritten;

		}
	}

	public List<Record> degreeCrentrality() {

		try (Session session = this.driver.session()) {
			List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.degree.stream('ukraine',{\n"
						+ "    relationshipWeightProperty: 'cost'\n"
						+ "\n"
						+ "})\n"
						+ "YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "ORDER BY score DESC, name ASC\n"
						+ "LIMIT 50");
				return result.list();
			});
			System.out.println(nodePropertiesWritten);
			return nodePropertiesWritten;

		}
	}

	public List<Record> eigenVector() {

		try (Session session = this.driver.session()) {
			List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.eigenvector.stream('ukraine',{\n"
						+ "    relationshipWeightProperty: 'cost'\n"
						+ "\n"
						+ "})\n"
						+ "YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score\n"
						+ "ORDER BY score DESC, name ASC\n"
						+ "LIMIT 50");
				return result.list();
			});
			System.out.println(nodePropertiesWritten);
			return nodePropertiesWritten;

		}
	}

	public List<Record> pageRank() {

		try (Session session = this.driver.session()) {
			List<Record> nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.pageRank.stream('ukraine', {\n"
						+ "relationshipWeightProperty: 'cost'}) YIELD nodeId, score\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, score ORDER BY score DESC, name ASC\n"
						+ "LIMIT 50");
				return result.list();
			});
			System.out.println(nodePropertiesWritten);
			return nodePropertiesWritten;

		}
	}

}
