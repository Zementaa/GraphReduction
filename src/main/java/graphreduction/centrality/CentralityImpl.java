package main.java.graphreduction.centrality;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public abstract class CentralityImpl implements Centrality {
	private Driver driver;

	public CentralityImpl(Driver driver) {
		super();
		this.setDriver(driver);
	}

	public List<Record> articleRank() {

		try (Session session = this.getDriver().session()) {
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

	public List<Record> eigenVector() {

		try (Session session = this.getDriver().session()) {
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

		try (Session session = this.getDriver().session()) {
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

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

}
