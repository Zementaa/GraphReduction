package main.java.graphreduction.communitydetection;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

public class LabelPropagation extends CommunityDetectionImpl {

	public LabelPropagation(Driver driver) {
		super(driver);
	}

	@Override
	public List<Record> stream(String graphName) {

		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.labelPropagation.stream('" + graphName + "', { relationshipWeightProperty: 'cost' })\n"
						+ "YIELD nodeId, communityId\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, communityId\n"
						+ "ORDER BY communityId DESC, name");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}
	
	@Override
	public void write(String graphName) {

		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.labelPropagation.write('ukraine', { relationshipWeightProperty: 'cost', writeProperty: 'communityId' })\n"
						+ "YIELD communityCount, ranIterations, didConverge");
				return result.list();
			});
			System.out.println(list);
		}
	}
}
