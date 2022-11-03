package main.java.graphreduction.centrality;

import java.util.List;

import org.neo4j.driver.Record;

/**
 * The Centrality Interface specifies the methods that must be implemented by <a href="#{@link}">{@link CentralityImpl}</a>.
 * <p>
 * It provides methods for the subsequent neo4j modes:
 * <ul>
 *     <li> stream - reads from graph </li>
 *     <li> streamWithNodeLabel - reads only from nodes that have specific labels </li>
 *     <li> write - writes to graph </li>
 * </ul>
 *
 * <p>
 * Centrality algorithms are used to determine the importance of distinct nodes in a network.
 * <p>
 * Also see: <a href="https://neo4j.com/docs/graph-data-science/1.8/algorithms/centrality/">Neo4j docs</a>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public interface Centrality {

	/**
	 * The stream mode will return the results of the algorithm computation as Cypher result rows.
	 *
	 * @param graphName Name of the graph
	 * @return List of Records
	 */
	List<Record> stream(String graphName);

	/**
	 *
	 * @param graphName Name of the graph
	 * @param community Label
	 * @param communityId Attribute
	 * @return List of Records
	 */
	List<Record> streamWithNodeLabel(String graphName, String community, int communityId);

	/**
	 * The write mode will write the results of the algorithm computation back to the Neo4j database.
	 * The written data is a node property called 'score'.
	 *
	 * @param graphName Name of the graph
	 */
	void write(String graphName);
}
