/**
 * 
 */
package main.java.graphreduction.communitydetection;

import java.util.List;

import org.neo4j.driver.Record;

/**
 * The Community Detection Interface specifies the methods that must be implemented by <a href="#{@link}">{@link CommunityDetectionImpl}</a>.
 * <p>
 * It provides methods for the subsequent neo4j modes:
 * <ul>
 *     <li> stream - reads from graph </li>
 *     <li> write - writes to graph </li>
 * </ul>
 *
 * <p>
 * Community detection algorithms are used to evaluate how groups of nodes are clustered or partitioned,
 * as well as their tendency to strengthen or break apart.
 * <p>
 * Also see: <a href="https://neo4j.com/docs/graph-data-science/1.8/algorithms/community/">Neo4j docs</a>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public interface CommunityDetection {

	/**
	 * The stream mode will return the results of the algorithm computation as Cypher result rows.
	 *
	 * @param graphName Name of the graph
	 * @return List of Records
	 */
	List<Record> stream(String graphName);

	/**
	 * The write mode will write the results of the algorithm computation back to the Neo4j database.
	 * The written data is a node property called 'communityId'.
	 *
	 * @param graphName Name of the graph
	 */
	void write(String graphName);

}
