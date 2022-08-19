package main.java.graphreduction.communitydetection;

import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public abstract class CommunityDetectionImpl implements CommunityDetection{
	
private Driver driver;
	
	public CommunityDetectionImpl(Driver driver) {
		super();
		this.setDriver(driver);
	}
	
	
	public List<Record> getIds(String graphName) {

		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n)\n"
						+ "WITH DISTINCT n.communityId AS communityId RETURN communityId");
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}
	
	public List<Record> getNodesWithRelationshipsToOtherCommunities(int communityId){
		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (a)-[:IS_CONNECTED]-(b)\n"
						+ "WHERE a.communityId=$communityId AND b.communityId<>$comunityId\n"
						+ "RETURN a,  COLLECT(DISTINCT b.communityId) as relationships\n"
						+ "ORDER BY SIZE(relationships) DESC ", Values.parameters( "communityId", communityId ));
				return result.list();
			});
			System.out.println(list);
			return list;

		}
	}
	
	public List<Record> getNodesWithRelationshipsWithinCommunity(int communityId){
		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (a)-[:IS_CONNECTED]-(b)\n"
						+ "WHERE a.communityId=$communityId AND b.communityId=$comunityId\n"
						+ "RETURN a,  COLLECT(b) as relationships\n"
						+ "ORDER BY SIZE(relationships) DESC ", Values.parameters( "communityId", communityId ));
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
