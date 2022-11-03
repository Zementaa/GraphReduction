package main.java.graphreduction.centrality;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

/**
 * The Degree Class extends the <a href="#{@link}">{@link CentralityImpl}</a>.
 * The Degree Centrality algorithm can be used to find popular nodes within a graph.
 * Degree centrality measures the number of incoming or outgoing (or both) relationships from a node,
 * depending on the orientation of a relationship projection.
 * <p>
 * Also see: <a href="https://neo4j.com/docs/graph-data-science/1.8/algorithms/degree-centrality/">Neo4j docs</a>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class Degree extends CentralityImpl {

	final Logger logger = LogManager.getLogger();
	
	public Degree(Driver driver) {
		super(driver);
	}

		// Verwenden - weighted
		@Override
		public List<Record> stream(String graphName) {

			try (Session session = this.getDriver().session()) {
				List<Record> list = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx.run("CALL gds.degree.stream($graphName,{\n"
							+ "    relationshipWeightProperty: 'cost',\n"
							+ "    orientation: 'UNDIRECTED'\n"
							+ "})\n"
							+ "YIELD nodeId, score\n"
							+ "RETURN gds.util.asNode(nodeId).name AS name, score, gds.util.asNode(nodeId).marked AS marked, gds.util.asNode(nodeId).occur AS occur\n"
							+ "ORDER BY score DESC, name ASC",
							Values.parameters( "graphName", graphName));
					return result.list();
				});
				logger.info(list);
				return list;

			}
		}
		
		@Override
		public List<Record> streamWithNodeLabel(String graphName, String community, int communityId) {

			try (Session session = this.getDriver().session()) {
				List<Record> list = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx.run("CALL gds.degree.stream($graphName, { relationshipWeightProperty: 'cost', orientation: 'UNDIRECTED', nodeLabels: [$community] })\n"
							+ "YIELD nodeId, score\n"
							+ "WHERE gds.util.asNode(nodeId).communityId=$communityId\n"
							//+ " WHERE score<>0\n"
							+ "RETURN gds.util.asNode(nodeId).name AS name, score, gds.util.asNode(nodeId).communityId AS communityId, gds.util.asNode(nodeId).marked AS marked, gds.util.asNode(nodeId).occur AS occur\n"
							+ "ORDER BY score DESC", Values.parameters( "graphName", graphName,  "community", community, "communityId", communityId ));
					return result.list();
				});
				logger.info(list);
				return list;

			}
		}

		@Override
		public void write(String graphName) {
			try (Session session = this.getDriver().session()) {
				List<Record> list = session.writeTransaction(tx -> {
					org.neo4j.driver.Result result = tx.run("CALL gds.degree.write($graphName,{\n"
							+ "    relationshipWeightProperty: 'cost',\n"
							+ "    orientation: 'UNDIRECTED', writeProperty: 'score'\n"
							+ "})\n"
							+ "YIELD centralityDistribution, nodePropertiesWritten\n" +
							"RETURN centralityDistribution.min AS minimumScore, centralityDistribution.mean AS meanScore, nodePropertiesWritten",
							Values.parameters( "graphName", graphName));
					return result.list();
				});
				logger.info(list);
			}
		}
}
