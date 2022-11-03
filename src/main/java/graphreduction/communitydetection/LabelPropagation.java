package main.java.graphreduction.communitydetection;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

/**
 * The LabelPropagation Class extends the <a href="#{@link}">{@link CommunityDetectionImpl}</a>.
 * The Label Propagation algorithm (LPA) is a fast algorithm for finding communities in a graph.
 * LPA works by propagating labels throughout the network and forming communities based on this process of
 * label propagation.
 * <p>
 * Also see: <a href="https://neo4j.com/docs/graph-data-science/1.8/algorithms/label-propagation/">Neo4j docs</a>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public class LabelPropagation extends CommunityDetectionImpl {

	final Logger logger = LogManager.getLogger();

	public LabelPropagation(Driver driver) {
		super(driver);
	}

	@Override
	public List<Record> stream(String graphName) {

		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.labelPropagation.stream($graphName, { relationshipWeightProperty: 'cost' })\n"
						+ "YIELD nodeId, communityId\n"
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
				org.neo4j.driver.Result result = tx.run("CALL gds.labelPropagation.write($graphName, { relationshipWeightProperty: 'cost', writeProperty: 'communityId' })\n"
						+ "YIELD communityCount, ranIterations, didConverge",
						Values.parameters( "graphName", graphName));
				return result.list();
			});
			logger.info(list);
		}
	}
}
