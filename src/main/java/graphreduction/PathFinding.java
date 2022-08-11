package main.java.graphreduction;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

public class PathFinding {

	private Driver driver;
	
	public PathFinding(Driver driver) {
		super();
		this.driver = driver;
	}
	
	public List<Record> randomWalk(String graphName) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.beta.randomWalk.stream('" + graphName + "',\n"
						+ "  {\n"
						+ "    walkLength: 3,\n"
						+ "    walksPerNode: 1,\n"
						+ "    randomSeed: 42,\n"
						+ "    concurrency: 1,\n"
						+ "    relationshipWeightProperty: 'cost'\n"
						+ "  }\n"
						+ ")\n"
						+ "YIELD nodeIds, path\n"
						+ "RETURN nodeIds, [node IN nodes(path) | node.name ] AS pages");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}
	
}
