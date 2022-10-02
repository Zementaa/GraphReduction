package main.java.graphreduction.communitydetection;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.neo4j.driver.*;

public abstract class CommunityDetectionImpl implements CommunityDetection{
	
private Driver driver;
private static Logger logger = LoggerFactory.getLogger(CommunityDetectionImpl.class);
	
	protected CommunityDetectionImpl(Driver driver) {
		super();
		this.setDriver(driver);
	}
	
	
	public List<Record> getIds(String graphName) {

		try (Session session = this.getDriver().session()) {
			List<Record> ids = session.writeTransaction(tx -> {
				Result result = tx.run("MATCH (n)\n"
						+ "WITH DISTINCT n.communityId AS communityId RETURN communityId");
				return result.list();
			});
			logger.info(ids.toString());
			return ids;

		}
	}
	
	public List<Record> getNodesWithRelationshipsToOtherCommunities(int communityId){
		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				Result result = tx.run("MATCH (a)-[:IS_CONNECTED]-(b)\n"
						+ "WHERE a.communityId=$communityId AND b.communityId<>$communityId\n"
						+ "RETURN a.name as name, a.marked as marked, a.communityId as communityId, a.occur as occur, COUNT(DISTINCT b.communityId) as relationships\n"
						+ "ORDER BY relationships DESC ", Values.parameters( "communityId", communityId ));
				return result.list();
			});
			logger.info(list.toString());
			return list;

		}
	}
	
	public List<Record> getNodesWithRelationshipsWithinCommunity(int communityId){
		try (Session session = this.getDriver().session()) {
			List<Record> list = session.writeTransaction(tx -> {
				Result result = tx.run("MATCH (a)-[:IS_CONNECTED]-(b)\n"
						+ "WHERE a.communityId=$communityId AND b.communityId=$communityId\n"
						+ "RETURN a.name as name, a.marked as marked, a.communityId as communityId, a.occur as occur, COUNT(b) as relationships\n"
						+ "ORDER BY relationships DESC ", Values.parameters( "communityId", communityId ));
				return result.list();
			});
			//System.out.println(list);
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
