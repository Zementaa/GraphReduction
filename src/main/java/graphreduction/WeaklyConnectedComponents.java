package main.java.graphreduction;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

/**
 * 
 * The WCC algorithm finds sets of connected nodes in an undirected graph, where
 * all nodes in the same set form a connected component. WCC is often used early
 * in an analysis to understand the structure of a graph. Using WCC to
 * understand the graph structure enables running other algorithms independently
 * on an identified cluster. As a preprocessing step for directed graphs, it
 * helps quickly identify disconnected groups. *
 * 
 */
public class WeaklyConnectedComponents {
	
	private Driver driver;	
	

	public WeaklyConnectedComponents(Driver driver) {
		super();
		this.driver = driver;
	}

	class Cluster {
		public Cluster(int clusterId, int clusterSize) {
			this.clusterId = clusterId;
			this.clusterSize = clusterSize;
		}

		int clusterId;
		int clusterSize;
	}
	
	public void useWeaklyConnectedComponentsAlgorithm() {
		
		System.out.println("start wcc");
		
		// First assign every node a cluster ID
		giveNodesClusterID();

		// Then return the cluster IDs and their cluster size
		getClusterIDsSizeDESC();

		// To retain only the biggest cluster the cluster ID with the biggest cluster
		// size must be determined
		int max = getMaxSizeClusterID();

		// Afterall all clusters that don't belong to the biggest cluster are deleted
		// deleteClustersThatAreNotID(max);
		
		System.out.println("end wcc");

	}

	public void giveNodesClusterID() {
		try (Session session = this.driver.session()) {
			Object nodePropertiesWritten = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("call gds.wcc.write(\n" + "{\n" + "nodeQuery: 'match (n) return id(n) as id',\n"
								+ "relationshipQuery:'MATCH (a)-->(b) RETURN id(a) as source, id(b) as target',\n"
								+ "writeProperty:'group',\n" + "consecutiveIds:true\n" + "}\n" + ")\n"
								+ "YIELD nodePropertiesWritten\n" + "return nodePropertiesWritten;");
				return result.single().get(0);
			});
			System.out.println("Es wurde " + nodePropertiesWritten + " Knoten eine Cluster-ID vergeben.");
		}
	}


	public  int getMaxSizeClusterID() {
		try (Session session = this.driver.session()) {

			// get(0) liefert n.group, get(1) liefert group_size
			int max = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("match (n) return n.group, count(n) as group_size order by group_size desc limit 1");
				return result.single().get(0).asInt();
			});
			System.out.println("Das Cluster mit der ID " + max + " ist das größte Cluster.");
			return max;
		}
	}

	public  List<Object> getClusterIDsSizeDESC() {
		try (Session session = this.driver.session()) {
			List<Object> names = new ArrayList<>();
			Object resultStr = session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx
						.run("match (n)\n" + "return n.group, count(n) as group_size\n" + "order by group_size desc");
				while (result.hasNext()) {
					names.add(result.next().get(0).asObject());
				}
				return names;
			});
			System.out.println("Generierte Cluster-IDs: " + resultStr);
			return names;
		}
	}

	public  void deleteClustersThatAreNotID(int id) {
		try (Session session = this.driver.session()) {
			session.writeTransaction(tx -> {
				org.neo4j.driver.Result result = tx.run("MATCH (n) WHERE n.group <> " + id + " DETACH DELETE n");
				return result.single().get(0);
			});
			System.out.println("Es wurde alle Knoten die nicht zu Cluster " + id + " gehören gelöscht.");
		}
	}


	public Driver getDriver() {
		return driver;
	}


	public void setDriver(Driver driver) {
		this.driver = driver;
	}
}
