package main.java.graphreduction;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

public class CommunityDetection {
	
private Driver driver;
	
	public CommunityDetection(Driver driver) {
		super();
		this.driver = driver;
	}
	
	public List<Record> louvain(String graphName) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.louvain.stream('ukraine', { relationshipWeightProperty: 'cost', includeIntermediateCommunities: true })\n"
						+ "YIELD nodeId, communityId, intermediateCommunityIds\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, communityId, intermediateCommunityIds\n"
						+ "ORDER BY communityId DESC, name");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}
	
	public List<Record> labelPropagation(String graphName) {

		try (Session session = this.driver.session()) {
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
	
	public List<Record> modularityOptimization(String graphName) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.beta.modularityOptimization.stream('ukraine', { relationshipWeightProperty: 'cost' })\n"
						+ "YIELD nodeId, communityId\n"
						+ "RETURN gds.util.asNode(nodeId).name AS name, communityId\n"
						+ "ORDER BY communityId DESC, name");
				return result.list();
			});
			System.out.println(list);
			return list;
		}

	}
	
	public void modularityOptimizationWrite(String graphName) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("CALL gds.beta.modularityOptimization.write('ukraine', { relationshipWeightProperty: 'cost', writeProperty: 'communityId' })\n"
						+ "YIELD nodes, communityCount, ranIterations, didConverge\n");
				return result.list();
			});
			System.out.println(list);

		}

	}
	
	public List<Record> getIds(String graphName) {

		try (Session session = this.driver.session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n)\n"
						+ "WITH DISTINCT n.communityId AS communityId RETURN communityId");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}

}
