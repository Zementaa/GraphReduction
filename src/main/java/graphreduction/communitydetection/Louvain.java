package main.java.graphreduction.communitydetection;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

/**
 * The Louvain Class extends the <a href="#{@link}">{@link CommunityDetectionImpl}</a>.
 * The Louvain method is an algorithm to detect communities in large networks.
 * It maximizes a modularity score for each community, where the modularity quantifies the quality of
 * an assignment of nodes to communities. 
 * <p>
 * Also see: <a href="https://neo4j.com/docs/graph-data-science/1.8/algorithms/louvain/">Neo4j docs</a>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class Louvain extends CommunityDetectionImpl {

	final Logger logger = LogManager.getLogger();
	
	public Louvain(Driver driver) {
		super(driver);
	}
	
	@Override
	public List<Record> stream(String graphName) {

		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.louvain.stream($graphName, {"
						+ "relationshipWeightProperty: 'cost', seedProperty: 'seed' })\n"
						+ "YIELD nodeId, communityId, intermediateCommunityIds\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, communityId\n"
						+ "ORDER BY communityId DESC, name",
						Values.parameters( "graphName", graphName));
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
				org.neo4j.driver.Result result = tx.run("CALL gds.louvain.write($graphName, {"
						+ "relationshipWeightProperty: 'cost', writeProperty: 'communityId', seedProperty: 'seed' })\n"
						+ "YIELD communityCount, modularity, modularities",
						Values.parameters( "graphName", graphName));
				return result.list();
			});
			logger.info(list);

		}
	}

}
