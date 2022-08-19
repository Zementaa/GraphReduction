package main.java.graphreduction.centrality;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class Degree extends CentralityImpl {

	public Degree(Driver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}

		// Verwenden - weighted
		@Override
		public List<Record> stream(String graphName) {

			try (Session session = this.getDriver().session()) {
				List<Record> list = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx.run("CALL gds.degree.stream('" + graphName + "',{\n"
							+ "    relationshipWeightProperty: 'cost',\n"
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
		
		@Override
		public List<Record> streamWithNodeLabel(String graphName, String community, int communityId) {

			try (Session session = this.getDriver().session()) {
				List<Record> list = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx.run("CALL gds.degree.stream($graphName, { relationshipWeightProperty: 'cost', nodeLabels: [$community] })\n"
							+ " YIELD nodeId, score\n"
							+ " WHERE gds.util.asNode(nodeId).communityId=$communityId\n"
							//+ " WHERE score<>0\n"
							+ "	RETURN gds.util.asNode(nodeId).name AS name, score, gds.util.asNode(nodeId).communityId AS communityId, gds.util.asNode(nodeId).marked AS marked, gds.util.asNode(nodeId).occur AS occur\n"
							+ "	ORDER BY score DESC", Values.parameters( "graphName", graphName,  "community", community, "communityId", communityId ));
					return result.list();
				});
				System.out.println(list);
				return list;

			}
		}

		@Override
		public void write(String graphName) {
			// TODO Auto-generated method stub
			
		}
}
