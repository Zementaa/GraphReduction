/**
 * 
 */
package main.java.graphreduction.communitydetection;

import java.util.List;

import org.neo4j.driver.Record;

/**
 * @author kamir
 *
 */
public interface CommunityDetection {

	public List<Record> stream(String graphName);
	
	public void write(String graphName);
	
	
}
