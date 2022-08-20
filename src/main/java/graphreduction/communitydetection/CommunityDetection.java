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
 *     <li> stats - evaluates algorithm performance on graph </li>
 *     <li> mutate - writes and evaluates, useful when multiple algorithms are used in conjunction </li>
 * </ul>
 *
 * @author Catherine Camier
 * @version 0.1.0
 */
public interface CommunityDetection {

	public List<Record> stream(String graphName);
	
	public void write(String graphName);
	
	
}
