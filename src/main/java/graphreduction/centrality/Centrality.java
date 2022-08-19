package main.java.graphreduction.centrality;

import java.util.List;

import org.neo4j.driver.Record;

public interface Centrality {

	public List<Record> stream(String graphName);
	
	public List<Record> streamWithNodeLabel(String graphName, String community, int communityId);
	
	public void write(String graphName);
}
